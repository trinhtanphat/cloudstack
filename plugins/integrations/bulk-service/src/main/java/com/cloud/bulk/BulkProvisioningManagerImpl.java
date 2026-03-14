// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.bulk;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.apache.cloudstack.api.command.user.bulk.BulkAllocatePublicIpsCmd;
import org.apache.cloudstack.api.command.user.bulk.BulkDeployVMsCmd;
import org.apache.cloudstack.api.command.user.bulk.ListBulkJobsCmd;
import org.apache.cloudstack.api.response.BulkJobItemResponse;
import org.apache.cloudstack.api.response.BulkJobResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.ConfigKey;

import com.cloud.bulk.dao.BulkJobDao;
import com.cloud.bulk.dao.BulkJobVO;
import com.cloud.dc.DataCenter;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.network.IpAddress;
import com.cloud.network.NetworkService;
import com.cloud.network.firewall.FirewallService;
import com.cloud.network.rules.PortForwardingRule;
import com.cloud.network.rules.RulesService;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.uservm.UserVm;
import com.cloud.utils.net.Ip;
import com.cloud.vm.UserVmService;
import com.cloud.utils.component.ManagerBase;

import com.google.gson.Gson;

public class BulkProvisioningManagerImpl extends ManagerBase implements BulkProvisioningService {

    private static final int MAX_VMS_PER_REQUEST = 10000;
    private static final int MAX_IPS_PER_REQUEST = 10000;
    private static final int IP_ALLOC_BATCH_SIZE = 50;
    private static final int PORT_RANGE_MIN = 10000;
    private static final int PORT_RANGE_MAX = 65000;
    private static final int MAX_VMS_PER_SHARED_IP = 500;
    private static final String IP_MODE_STATIC_NAT = "STATIC_NAT";
    private static final String IP_MODE_PORT_FORWARD = "PORT_FORWARD";
    private static final java.util.regex.Pattern NAME_PREFIX_PATTERN =
        java.util.regex.Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9\\-]{0,62}$");
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Gson gson = new Gson();

    @Inject private BulkJobDao bulkJobDao;
    @Inject private UserVmService userVmService;
    @Inject private NetworkService networkService;
    @Inject private RulesService rulesService;
    @Inject private FirewallService firewallService;
    @Inject private AccountManager accountManager;
    @Inject private DataCenterDao dataCenterDao;
    @Inject private ServiceOfferingDao serviceOfferingDao;
    @Inject private VMTemplateDao templateDao;

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    // ──────────────────────────────────────────────────────────────────
    //  BULK DEPLOY VMs
    // ──────────────────────────────────────────────────────────────────

    @Override
    public BulkJobResponse bulkDeployVMs(BulkDeployVMsCmd cmd) {
        int count = cmd.getCount();
        if (count < 1 || count > MAX_VMS_PER_REQUEST) {
            throw new IllegalArgumentException("Count must be between 1 and " + MAX_VMS_PER_REQUEST);
        }

        // Input validation: namePrefix must be alphanumeric + hyphens
        String namePrefix = cmd.getNamePrefix();
        if (namePrefix == null || !NAME_PREFIX_PATTERN.matcher(namePrefix).matches()) {
            throw new IllegalArgumentException(
                "Name prefix must be 1-63 characters, alphanumeric and hyphens only, starting with alphanumeric");
        }

        // Validate IP mode
        String ipMode = cmd.getIpMode();
        if (!IP_MODE_STATIC_NAT.equals(ipMode) && !IP_MODE_PORT_FORWARD.equals(ipMode)) {
            throw new IllegalArgumentException("ipmode must be STATIC_NAT or PORT_FORWARD");
        }

        // Validate private ports if PORT_FORWARD mode
        int[] privatePorts = null;
        if (IP_MODE_PORT_FORWARD.equals(ipMode) && cmd.getAssignPublicIp()) {
            privatePorts = parseAndValidatePorts(cmd.getPrivatePorts());
            // Check if we have enough port range for all VMs
            int portsPerVm = privatePorts.length;
            int availablePorts = PORT_RANGE_MAX - PORT_RANGE_MIN;
            if (count * portsPerVm > availablePorts) {
                throw new IllegalArgumentException(
                    "Not enough port range for " + count + " VMs x " + portsPerVm + " ports. " +
                    "Max: " + (availablePorts / portsPerVm) + " VMs in PORT_FORWARD mode");
            }
        }

        CallContext ctx = CallContext.current();
        Account caller = ctx.getCallingAccount();

        DataCenter zone = dataCenterDao.findById(cmd.getZoneId());
        if (zone == null) {
            throw new IllegalArgumentException("Invalid zone ID: " + cmd.getZoneId());
        }
        ServiceOffering offering = serviceOfferingDao.findById(cmd.getServiceOfferingId());
        if (offering == null) {
            throw new IllegalArgumentException("Invalid service offering ID: " + cmd.getServiceOfferingId());
        }
        VirtualMachineTemplate template = templateDao.findById(cmd.getTemplateId());
        if (template == null) {
            throw new IllegalArgumentException("Invalid template ID: " + cmd.getTemplateId());
        }

        BulkJobVO job = new BulkJobVO();
        job.setAccountId(caller.getId());
        job.setDomainId(caller.getDomainId());
        job.setJobType(BulkJobVO.JobType.DEPLOY_VMS);
        job.setTotalCount(count);
        job.setZoneId(cmd.getZoneId());
        job.setServiceOfferingId(cmd.getServiceOfferingId());
        job.setTemplateId(cmd.getTemplateId());
        job.setNetworkId(cmd.getNetworkId());
        job.setNamePrefix(cmd.getNamePrefix());
        job.setAssignPublicIp(cmd.getAssignPublicIp());
        job.setBatchSize(cmd.getBatchSize());
        job = bulkJobDao.persist(job);

        final long jobId = job.getId();
        final String jobUuid = job.getUuid();
        final long callerAccountId = caller.getId();

        executor.submit(() -> executeBulkDeploy(jobId, cmd, callerAccountId));

        BulkJobResponse response = new BulkJobResponse();
        response.setJobId(jobUuid);
        response.setJobType("DEPLOY_VMS");
        response.setStatus("PENDING");
        response.setTotalCount(count);
        response.setCompletedCount(0);
        response.setProgress(0);
        response.setCreated(job.getCreated());
        return response;
    }

    private void executeBulkDeploy(long jobId, BulkDeployVMsCmd cmd, long callerAccountId) {
        BulkJobVO job = bulkJobDao.findById(jobId);
        job.setStatus(BulkJobVO.Status.RUNNING);
        bulkJobDao.update(jobId, job);

        int count = cmd.getCount();
        int batchSize = cmd.getBatchSize();
        String namePrefix = cmd.getNamePrefix();
        String ipMode = cmd.getIpMode();
        boolean isPortForward = IP_MODE_PORT_FORWARD.equals(ipMode);
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        List<BulkJobItemResponse> results = Collections.synchronizedList(new ArrayList<>());
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        Account callerAccount = accountManager.getAccount(callerAccountId);

        // PORT_FORWARD mode: allocate shared IPs and track assigned ports
        IpAddress sharedIp = null;
        final Set<Integer> usedPorts = Collections.synchronizedSet(new HashSet<>());
        int[] privatePorts = isPortForward ? parseAndValidatePorts(cmd.getPrivatePorts()) : null;

        if (isPortForward && cmd.getAssignPublicIp()) {
            try {
                sharedIp = networkService.allocateIP(callerAccount, cmd.getZoneId(), cmd.getNetworkId(), null, null);
                if (sharedIp == null) {
                    logger.error("Failed to allocate shared IP for PORT_FORWARD mode");
                }
            } catch (Exception e) {
                logger.error("Failed to allocate shared IP: " + e.getMessage(), e);
            }
        }
        final IpAddress finalSharedIp = sharedIp;

        for (int batchStart = 0; batchStart < count; batchStart += batchSize) {
            int batchEnd = Math.min(batchStart + batchSize, count);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = batchStart; i < batchEnd; i++) {
                final int vmIndex = i;
                final String vmName = String.format("%s-%04d", namePrefix, vmIndex + 1);

                futures.add(executor.submit(() -> {
                    try {
                        DataCenter zone = dataCenterDao.findById(cmd.getZoneId());
                        ServiceOffering offering = serviceOfferingDao.findById(cmd.getServiceOfferingId());
                        VirtualMachineTemplate template = templateDao.findById(cmd.getTemplateId());
                        List<Long> networkIds = cmd.getNetworkId() != null ?
                            List.of(cmd.getNetworkId()) : new ArrayList<>();

                        UserVm vm = userVmService.createAdvancedVirtualMachine(
                            zone, offering, template,
                            networkIds, callerAccount,
                            vmName, vmName,
                            null, null, null,
                            null, null, null,
                            null, null, null,
                            null, null, null,
                            null, null, null,
                            null, null, null,
                            null, null, true,
                            null, null, null, null
                        );

                        BulkJobItemResponse item = new BulkJobItemResponse();
                        item.setIndex(vmIndex + 1);
                        item.setResourceId(vm.getUuid());
                        item.setResourceType("VirtualMachine");
                        item.setName(vmName);
                        item.setStatus("CREATED");

                        if (cmd.getAssignPublicIp()) {
                            if (isPortForward && finalSharedIp != null) {
                                // PORT_FORWARD mode: create port forwarding rules on shared IP
                                assignPortForwardingWithRollback(
                                    callerAccount, cmd.getZoneId(), cmd.getNetworkId(),
                                    finalSharedIp, vm, vmName, privatePorts, usedPorts, item);
                            } else if (!isPortForward) {
                                // STATIC_NAT mode: 1 IP per VM (existing behavior)
                                assignPublicIpWithRollback(callerAccount, cmd.getZoneId(),
                                    cmd.getNetworkId(), networkIds, vm, vmName, item);
                            }
                        }

                        results.add(item);
                        completed.incrementAndGet();
                    } catch (Exception e) {
                        logger.error("Failed to deploy VM " + vmName, e);
                        failed.incrementAndGet();
                        errors.add("VM " + vmName + ": " + e.getMessage());
                    }
                }));
            }

            for (Future<?> future : futures) {
                try { future.get(); } catch (Exception e) { logger.error("Batch execution error", e); }
            }

            // Apply port forwarding rules after each batch (if PORT_FORWARD mode)
            if (isPortForward && finalSharedIp != null) {
                try {
                    rulesService.applyPortForwardingRules(finalSharedIp.getId(), callerAccount);
                } catch (Exception e) {
                    logger.warn("Failed to apply port forwarding rules for batch: " + e.getMessage());
                }
            }

            job = bulkJobDao.findById(jobId);
            job.setCompletedCount(completed.get());
            job.setFailedCount(failed.get());
            bulkJobDao.update(jobId, job);
        }

        job = bulkJobDao.findById(jobId);
        job.setCompletedCount(completed.get());
        job.setFailedCount(failed.get());
        job.setResultData(gson.toJson(results));
        job.setErrorData(gson.toJson(errors));
        job.setCompleted(new Date());
        if (failed.get() == 0) {
            job.setStatus(BulkJobVO.Status.COMPLETED);
        } else if (completed.get() == 0) {
            job.setStatus(BulkJobVO.Status.FAILED);
        } else {
            job.setStatus(BulkJobVO.Status.PARTIAL);
        }
        bulkJobDao.update(jobId, job);
        logger.info("Bulk deploy job " + job.getUuid() + " finished: " +
                     completed.get() + " success, " + failed.get() + " failed out of " + count);
    }

    /**
     * Allocate a public IP and enable static NAT for a VM.
     * If NAT enablement fails, the allocated IP is released back to the pool
     * to prevent orphaned IPs.
     */
    private void assignPublicIpWithRollback(Account owner, long zoneId, Long cmdNetworkId,
                                            List<Long> networkIds, UserVm vm, String vmName,
                                            BulkJobItemResponse item) {
        IpAddress ip = null;
        try {
            ip = networkService.allocateIP(owner, zoneId, cmdNetworkId, null, null);
            if (ip == null) {
                item.setIpAddress("POOL_EXHAUSTED");
                item.setError("No public IP available in zone");
                return;
            }

            long natNetworkId = cmdNetworkId != null ? cmdNetworkId :
                (networkIds != null && !networkIds.isEmpty() ? networkIds.get(0) : 0L);

            rulesService.enableStaticNat(ip.getId(), vm.getId(), natNetworkId, null);
            item.setIpAddress(ip.getAddress().addr());

        } catch (Exception natEx) {
            logger.warn("Failed to assign public IP to VM " + vmName + ": " + natEx.getMessage());
            item.setError("IP assign failed: " + natEx.getMessage());

            // ROLLBACK: release the allocated IP so it doesn't leak
            if (ip != null) {
                try {
                    networkService.releaseIpAddress(ip.getId());
                    logger.info("Rolled back IP " + ip.getAddress().addr()
                        + " after NAT failure for VM " + vmName);
                    item.setIpAddress("NAT_FAILED_IP_RELEASED");
                } catch (Exception releaseEx) {
                    logger.error("CRITICAL: Failed to release IP " + ip.getAddress().addr()
                        + " after NAT failure for VM " + vmName, releaseEx);
                    item.setIpAddress("NAT_FAILED_IP_LEAK:" + ip.getAddress().addr());
                }
            } else {
                item.setIpAddress("ALLOCATION_FAILED");
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────
    //  PORT_FORWARD MODE: Shared IP + random high ports
    // ──────────────────────────────────────────────────────────────────

    /**
     * Create port forwarding rules on a shared public IP for a VM.
     * Assigns random high ports (10000-65000) for each private port.
     * Thread-safe: usedPorts set prevents port conflicts across parallel threads.
     */
    private void assignPortForwardingWithRollback(Account owner, long zoneId, Long networkId,
                                                   IpAddress sharedIp, UserVm vm, String vmName,
                                                   int[] privatePorts, Set<Integer> usedPorts,
                                                   BulkJobItemResponse item) {
        List<Long> createdRuleIds = new ArrayList<>();
        try {
            long natNetworkId = networkId != null ? networkId : 0L;
            StringBuilder portMappings = new StringBuilder();

            for (int privatePort : privatePorts) {
                int publicPort = allocateRandomPort(usedPorts);

                PortForwardingRuleAdapter ruleAdapter = new PortForwardingRuleAdapter(
                    sharedIp.getId(), publicPort, privatePort,
                    "tcp", natNetworkId, owner.getId(), owner.getDomainId(), vm.getId());

                PortForwardingRule rule = rulesService.createPortForwardingRule(
                    ruleAdapter, vm.getId(), null, true, true);

                if (rule != null) {
                    createdRuleIds.add(rule.getId());
                    if (portMappings.length() > 0) portMappings.append(",");
                    portMappings.append(publicPort).append("->").append(privatePort);
                } else {
                    usedPorts.remove(publicPort);
                    logger.warn("Port forwarding rule creation returned null for VM " + vmName);
                }
            }

            item.setIpAddress(sharedIp.getAddress().addr());
            item.setPublicPort(privatePorts.length == 1
                ? Integer.valueOf(portMappings.toString().split("->")[0])
                : null);
            item.setStatus("CREATED");
            logger.info("Port forwarding for VM " + vmName + ": " +
                        sharedIp.getAddress().addr() + " [" + portMappings + "]");

        } catch (NetworkRuleConflictException conflict) {
            logger.warn("Port conflict for VM " + vmName + ": " + conflict.getMessage());
            item.setError("Port conflict: " + conflict.getMessage());
            rollbackPortForwardingRules(createdRuleIds, vmName);
        } catch (Exception e) {
            logger.warn("Port forwarding failed for VM " + vmName + ": " + e.getMessage());
            item.setError("PF failed: " + e.getMessage());
            rollbackPortForwardingRules(createdRuleIds, vmName);
        }
    }

    private void rollbackPortForwardingRules(List<Long> ruleIds, String vmName) {
        for (Long ruleId : ruleIds) {
            try {
                rulesService.revokePortForwardingRule(ruleId, true);
            } catch (Exception e) {
                logger.error("Failed to rollback PF rule " + ruleId + " for VM " + vmName, e);
            }
        }
    }

    /**
     * Allocate a unique random port in range [PORT_RANGE_MIN, PORT_RANGE_MAX].
     * Uses SecureRandom to avoid predictable port assignments.
     */
    private int allocateRandomPort(Set<Integer> usedPorts) {
        int range = PORT_RANGE_MAX - PORT_RANGE_MIN;
        int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int port = PORT_RANGE_MIN + secureRandom.nextInt(range);
            if (usedPorts.add(port)) {
                return port;
            }
        }
        // Fallback: linear scan for available port
        for (int port = PORT_RANGE_MIN; port < PORT_RANGE_MAX; port++) {
            if (usedPorts.add(port)) {
                return port;
            }
        }
        throw new RuntimeException("Port range exhausted: all ports " +
            PORT_RANGE_MIN + "-" + PORT_RANGE_MAX + " are in use");
    }

    /**
     * Parse and validate comma-separated port list.
     */
    static int[] parseAndValidatePorts(String portString) {
        if (portString == null || portString.trim().isEmpty()) {
            return new int[]{22};
        }
        String[] parts = portString.split(",");
        int[] ports = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            int port;
            try {
                port = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number: " + parts[i].trim());
            }
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port must be 1-65535, got: " + port);
            }
            ports[i] = port;
        }
        return ports;
    }

    // ──────────────────────────────────────────────────────────────────
    //  PortForwardingRule adapter (lightweight impl for programmatic use)
    // ──────────────────────────────────────────────────────────────────

    /**
     * Minimal implementation of PortForwardingRule for creating rules
     * programmatically without using API command objects.
     */
    static class PortForwardingRuleAdapter implements PortForwardingRule {
        private final long sourceIpAddressId;
        private final int publicPort;
        private final int privatePort;
        private final String protocol;
        private final long networkId;
        private final long accountId;
        private final long domainId;
        private final long virtualMachineId;

        PortForwardingRuleAdapter(long sourceIpAddressId, int publicPort, int privatePort,
                                   String protocol, long networkId, long accountId,
                                   long domainId, long virtualMachineId) {
            this.sourceIpAddressId = sourceIpAddressId;
            this.publicPort = publicPort;
            this.privatePort = privatePort;
            this.protocol = protocol;
            this.networkId = networkId;
            this.accountId = accountId;
            this.domainId = domainId;
            this.virtualMachineId = virtualMachineId;
        }

        @Override public long getId() { return -1L; }
        @Override public String getUuid() { return null; }
        @Override public String getXid() { return null; }
        @Override public Long getSourceIpAddressId() { return sourceIpAddressId; }
        @Override public Integer getSourcePortStart() { return publicPort; }
        @Override public Integer getSourcePortEnd() { return publicPort; }
        @Override public String getProtocol() { return protocol; }
        @Override public Purpose getPurpose() { return Purpose.PortForwarding; }
        @Override public State getState() { return State.Add; }
        @Override public long getNetworkId() { return networkId; }
        @Override public long getAccountId() { return accountId; }
        @Override public long getDomainId() { return domainId; }
        @Override public Integer getIcmpCode() { return null; }
        @Override public Integer getIcmpType() { return null; }
        @Override public List<String> getSourceCidrList() { return null; }
        @Override public List<String> getDestinationCidrList() { return null; }
        @Override public Long getRelated() { return null; }
        @Override public FirewallRuleType getType() { return FirewallRuleType.User; }
        @Override public TrafficType getTrafficType() { return TrafficType.Ingress; }
        @Override public boolean isDisplay() { return true; }
        @Override public Class<?> getEntityType() { return PortForwardingRule.class; }
        @Override public String getName() { return null; }
        @Override public Ip getDestinationIpAddress() { return null; }
        @Override public void setDestinationIpAddress(Ip ip) { }
        @Override public int getDestinationPortStart() { return privatePort; }
        @Override public int getDestinationPortEnd() { return privatePort; }
        @Override public long getVirtualMachineId() { return virtualMachineId; }
    }

    // ──────────────────────────────────────────────────────────────────
    //  BULK ALLOCATE PUBLIC IPs — now parallel with batching
    // ──────────────────────────────────────────────────────────────────

    @Override
    public BulkJobResponse bulkAllocatePublicIps(BulkAllocatePublicIpsCmd cmd) {
        int count = cmd.getCount();
        if (count < 1 || count > MAX_IPS_PER_REQUEST) {
            throw new IllegalArgumentException("Count must be between 1 and " + MAX_IPS_PER_REQUEST);
        }

        CallContext ctx = CallContext.current();
        Account caller = ctx.getCallingAccount();

        DataCenter zone = dataCenterDao.findById(cmd.getZoneId());
        if (zone == null) {
            throw new IllegalArgumentException("Invalid zone ID: " + cmd.getZoneId());
        }

        BulkJobVO job = new BulkJobVO();
        job.setAccountId(caller.getId());
        job.setDomainId(caller.getDomainId());
        job.setJobType(BulkJobVO.JobType.ALLOCATE_IPS);
        job.setTotalCount(count);
        job.setZoneId(cmd.getZoneId());
        job.setNetworkId(cmd.getNetworkId());
        job = bulkJobDao.persist(job);

        final long jobId = job.getId();
        final long callerAccountId = caller.getId();
        executor.submit(() -> executeBulkIpAllocation(jobId, cmd, callerAccountId));

        BulkJobResponse response = new BulkJobResponse();
        response.setJobId(job.getUuid());
        response.setJobType("ALLOCATE_IPS");
        response.setStatus("PENDING");
        response.setTotalCount(count);
        response.setCompletedCount(0);
        response.setProgress(0);
        response.setCreated(job.getCreated());
        return response;
    }

    /**
     * FIX #3: Parallel IP allocation with batching.
     * IPs are allocated in batches of IP_ALLOC_BATCH_SIZE using the thread pool,
     * dramatically improving throughput for 1000+ IPs.
     * FIX #5: Supports startIpAddress for sequential allocation from a specific range.
     */
    private void executeBulkIpAllocation(long jobId, BulkAllocatePublicIpsCmd cmd, long callerAccountId) {
        BulkJobVO job = bulkJobDao.findById(jobId);
        job.setStatus(BulkJobVO.Status.RUNNING);
        bulkJobDao.update(jobId, job);

        int count = cmd.getCount();
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        List<BulkJobItemResponse> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Account caller = accountManager.getAccount(callerAccountId);

        // FIX #5: If startIpAddress specified, allocate sequentially from that IP
        String nextIp = cmd.getStartIpAddress();

        for (int batchStart = 0; batchStart < count; batchStart += IP_ALLOC_BATCH_SIZE) {
            int batchEnd = Math.min(batchStart + IP_ALLOC_BATCH_SIZE, count);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = batchStart; i < batchEnd; i++) {
                final int ipIndex = i;
                final String requestedIp = nextIp;

                // Increment IP for next iteration if using sequential allocation
                if (nextIp != null) {
                    nextIp = incrementIpAddress(nextIp);
                }

                futures.add(executor.submit(() -> {
                    try {
                        IpAddress ip = networkService.allocateIP(caller, cmd.getZoneId(),
                            cmd.getNetworkId(), null, requestedIp);

                        BulkJobItemResponse item = new BulkJobItemResponse();
                        item.setIndex(ipIndex + 1);
                        item.setResourceId(ip.getUuid());
                        item.setResourceType("PublicIpAddress");
                        item.setIpAddress(ip.getAddress().addr());
                        item.setStatus("ALLOCATED");
                        synchronized (results) { results.add(item); }
                        completed.incrementAndGet();
                    } catch (Exception e) {
                        failed.incrementAndGet();
                        synchronized (errors) { errors.add("IP #" + (ipIndex + 1) + ": " + e.getMessage()); }
                        logger.warn("Failed to allocate IP #" + (ipIndex + 1), e);
                    }
                }));
            }

            // Wait for batch to complete before proceeding
            for (Future<?> future : futures) {
                try { future.get(); } catch (Exception e) { logger.error("IP batch error", e); }
            }

            // Update progress after each batch
            job = bulkJobDao.findById(jobId);
            job.setCompletedCount(completed.get());
            job.setFailedCount(failed.get());
            bulkJobDao.update(jobId, job);
        }

        job = bulkJobDao.findById(jobId);
        job.setCompletedCount(completed.get());
        job.setFailedCount(failed.get());
        job.setResultData(gson.toJson(results));
        job.setErrorData(gson.toJson(errors));
        job.setCompleted(new Date());
        job.setStatus(failed.get() == 0 ? BulkJobVO.Status.COMPLETED :
                       completed.get() == 0 ? BulkJobVO.Status.FAILED : BulkJobVO.Status.PARTIAL);
        bulkJobDao.update(jobId, job);
        logger.info("Bulk IP allocation job " + job.getUuid() + " finished: " +
                     completed.get() + " allocated, " + failed.get() + " failed");
    }

    /**
     * Increment an IPv4 address string by 1.
     * Example: "10.0.0.254" → "10.0.1.0"
     */
    private static String incrementIpAddress(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) return null;

        long ip = 0;
        for (String part : parts) {
            ip = (ip << 8) | (Integer.parseInt(part) & 0xFF);
        }
        ip++;

        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF, (ip >> 16) & 0xFF, (ip >> 8) & 0xFF, ip & 0xFF);
    }

    // ──────────────────────────────────────────────────────────────────
    //  LIST JOBS
    // ──────────────────────────────────────────────────────────────────

    @Override
    public List<BulkJobResponse> listBulkJobs(ListBulkJobsCmd cmd) {
        CallContext ctx = CallContext.current();
        long accountId = ctx.getCallingAccountId();

        List<BulkJobVO> jobs;
        if (cmd.getJobId() != null) {
            BulkJobVO job = bulkJobDao.findByUuid(cmd.getJobId());
            jobs = (job != null) ? List.of(job) : new ArrayList<>();
        } else {
            jobs = bulkJobDao.listByAccountId(accountId);
        }

        List<BulkJobResponse> responses = new ArrayList<>();
        for (BulkJobVO job : jobs) {
            if (cmd.getStatus() != null && !job.getStatus().name().equals(cmd.getStatus())) continue;
            if (cmd.getJobType() != null && !job.getJobType().name().equals(cmd.getJobType())) continue;

            BulkJobResponse r = new BulkJobResponse();
            r.setJobId(job.getUuid());
            r.setJobType(job.getJobType().name());
            r.setStatus(job.getStatus().name());
            r.setTotalCount(job.getTotalCount());
            r.setCompletedCount(job.getCompletedCount());
            r.setFailedCount(job.getFailedCount());
            r.setProgress(job.getTotalCount() > 0
                ? (int) ((job.getCompletedCount() + job.getFailedCount()) * 100 / job.getTotalCount()) : 0);
            r.setCreated(job.getCreated());
            r.setCompleted(job.getCompleted());
            responses.add(r);
        }
        return responses;
    }

    @Override
    public List<Class<?>> getCommands() {
        List<Class<?>> commands = new ArrayList<>();
        commands.add(BulkDeployVMsCmd.class);
        commands.add(BulkAllocatePublicIpsCmd.class);
        commands.add(ListBulkJobsCmd.class);
        return commands;
    }

    @Override
    public String getConfigComponentName() {
        return BulkProvisioningManagerImpl.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[] {};
    }
}
