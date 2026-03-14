// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.gpu;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.cloudstack.api.command.user.gpu.CreateGpuInstanceCmd;
import org.apache.cloudstack.api.command.user.gpu.DeleteGpuInstanceCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuInstancesCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuMetricsCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuProfilesCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuUsageCmd;
import org.apache.cloudstack.api.command.user.gpu.StartGpuInstanceCmd;
import org.apache.cloudstack.api.command.user.gpu.StopGpuInstanceCmd;
import org.apache.cloudstack.api.response.GpuInstanceResponse;
import org.apache.cloudstack.api.response.GpuMetricResponse;
import org.apache.cloudstack.api.response.GpuProfileResponse;
import org.apache.cloudstack.api.response.GpuUsageResponse;
import org.apache.cloudstack.context.CallContext;
import org.apache.cloudstack.framework.config.ConfigKey;

import com.cloud.agent.api.VgpuTypesInfo;
import com.cloud.dc.DataCenter;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.event.ActionEvent;
import com.cloud.gpu.dao.VGPUTypesDao;
import com.cloud.gpu.dao.GpuServiceInstanceDao;
import com.cloud.gpu.dao.GpuServiceInstanceVO;
import com.cloud.gpu.dao.GpuServiceInstanceVO.State;
import com.cloud.offering.ServiceOffering;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.uservm.UserVm;
import com.cloud.utils.component.ManagerBase;
import com.cloud.vm.UserVmService;

public class GpuServiceManagerImpl extends ManagerBase implements GpuAsAService {

    private static final int MIN_GPU_COUNT = 1;
    private static final int MAX_GPU_COUNT = 16;
    private static final ConfigKey<Integer> GpuMaxActivePerAccount = new ConfigKey<>("Advanced", Integer.class,
        "gpu.service.max.active.gpu.per.account", "16",
        "Maximum active GPU count per account across all GPU profiles.", true);
    private static final ConfigKey<String> GpuProfileQuotaMap = new ConfigKey<>("Advanced", String.class,
        "gpu.service.profile.quota.map",
        "aws-g5-xlarge:4,aws-p4d-24xlarge:8,azure-nc-a100-v4:8,azure-nvads-a10-v5:6,gcp-a2-highgpu-1g:8,gcp-a3-highgpu-8g:8",
        "Per-profile max active GPU count per account using profileId:limit entries.", true);
    private static final ConfigKey<String> GpuProfileHourlyRateMap = new ConfigKey<>("Advanced", String.class,
        "gpu.service.profile.hourly.rate.usd.map",
        "aws-g5-xlarge:1.20,aws-p4d-24xlarge:32.00,azure-nc-a100-v4:12.00,azure-nvads-a10-v5:4.50,gcp-a2-highgpu-1g:4.10,gcp-a3-highgpu-8g:28.00",
        "Per-profile hourly billing rate in USD using profileId:rate entries.", true);

    private final Map<String, ProfileDefinition> profileMap = new LinkedHashMap<>();

    @Inject
    private GpuServiceInstanceDao gpuServiceInstanceDao;

    @Inject
    private AccountManager accountManager;

    @Inject
    private UserVmService userVmService;

    @Inject
    private DataCenterDao dataCenterDao;

    @Inject
    private ServiceOfferingDao serviceOfferingDao;

    @Inject
    private VMTemplateDao templateDao;

    @Inject
    private VGPUTypesDao vgpuTypesDao;

    public GpuServiceManagerImpl() {
        addProfile("aws-g5-xlarge", "AWS", "NVIDIA", "A10G", 24, 1, "A10",
            "AWS G5 compatible profile for graphics and AI inference");
        addProfile("aws-p4d-24xlarge", "AWS", "NVIDIA", "A100", 40, 8, "A100",
            "AWS P4d compatible profile for HPC and deep learning");

        addProfile("azure-nc-a100-v4", "AZURE", "NVIDIA", "A100", 80, 4, "A100",
            "Azure NC A100 v4 compatible profile for AI training");
        addProfile("azure-nvads-a10-v5", "AZURE", "NVIDIA", "A10", 24, 2, "A10",
            "Azure NVads A10 v5 compatible profile for visualization workloads");

        addProfile("gcp-a2-highgpu-1g", "GCP", "NVIDIA", "A100", 40, 1, "A100",
            "GCP A2 profile for single GPU machine learning inference");
        addProfile("gcp-a3-highgpu-8g", "GCP", "NVIDIA", "H100", 80, 8, "H100",
            "GCP A3 profile for large-scale model training");
    }

    @Override
    @ActionEvent(eventType = GpuEventTypes.EVENT_GPU_INSTANCE_CREATE,
                eventDescription = "creating gpu service instance", create = true)
    public GpuInstanceResponse createGpuInstance(CreateGpuInstanceCmd cmd) {
        String profileId = cmd.getGpuProfileId();
        ProfileDefinition profile = profileMap.get(profileId);
        if (profile == null) {
            throw new IllegalArgumentException("Unsupported GPU profile: " + profileId);
        }

        if (!profile.provider.equalsIgnoreCase(cmd.getProvider())) {
            throw new IllegalArgumentException("Profile " + profileId + " belongs to provider " + profile.provider);
        }

        int gpuCount = cmd.getGpuCount();
        if (gpuCount < MIN_GPU_COUNT || gpuCount > MAX_GPU_COUNT) {
            throw new IllegalArgumentException("gpucount must be between " + MIN_GPU_COUNT + " and " + MAX_GPU_COUNT);
        }
        if (gpuCount > profile.maxGpuCount) {
            throw new IllegalArgumentException("gpucount exceeds profile limit: " + profile.maxGpuCount);
        }

        Account caller = CallContext.current().getCallingAccount();

        enforceQuota(caller.getId(), profileId, gpuCount);

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

        if (!hasEnoughGpuCapacity(cmd.getZoneId(), profile, gpuCount)) {
            throw new IllegalArgumentException("Insufficient GPU host capacity for profile " + profileId + " in zone " + cmd.getZoneId());
        }

        GpuServiceInstanceVO instance = new GpuServiceInstanceVO();
        instance.setName(cmd.getName());
        instance.setAccountId(caller.getId());
        instance.setDomainId(caller.getDomainId());
        instance.setZoneId(cmd.getZoneId());
        instance.setServiceOfferingId(cmd.getServiceOfferingId());
        instance.setTemplateId(cmd.getTemplateId());
        instance.setNetworkId(cmd.getNetworkId());
        instance.setProvider(cmd.getProvider());
        instance.setGpuProfileId(profileId);
        instance.setGpuCount(gpuCount);
        instance.setState(State.RUNNING);

        List<Long> networkIds = cmd.getNetworkId() != null ? List.of(cmd.getNetworkId()) : new ArrayList<>();
        String vmName = "gpu-" + instance.getUuid().substring(0, 8);
        try {
            UserVm vm = userVmService.createAdvancedVirtualMachine(
                 zone, offering, template,
                 networkIds, caller,
                 vmName, cmd.getName(),
                 null, null, null,
                 null, null, null,
                 null, null, null,
                null, null, null,
                true,
                null, null,
                null, null, null,
                null,
                null,
                true,
                null,
                null,
                null,
                null
            );
            if (vm == null) {
                throw new IllegalArgumentException("Unable to deploy GPU VM for instance: " + cmd.getName());
            }
            instance.setVmId(vm.getId());
            if (vm.getState() != null && "Stopped".equalsIgnoreCase(vm.getState().toString())) {
                instance.setState(State.STOPPED);
            }
        } catch (Exception e) {
            logger.error("Failed to deploy GPU VM for instance " + cmd.getName(), e);
            throw new IllegalArgumentException("Failed to deploy GPU VM: " + e.getMessage());
        }

        instance = gpuServiceInstanceDao.persist(instance);
        return buildInstanceResponse(instance);
    }

    @Override
    public List<GpuInstanceResponse> listGpuInstances(ListGpuInstancesCmd cmd) {
        List<GpuServiceInstanceVO> instances = resolveInstances(cmd.getId());
        List<GpuInstanceResponse> responses = new ArrayList<>();

        for (GpuServiceInstanceVO instance : instances) {
            if (instance.getRemoved() != null) {
                continue;
            }
            if (cmd.getState() != null && !instance.getState().name().equals(cmd.getState())) {
                continue;
            }
            if (cmd.getProvider() != null && !instance.getProvider().equalsIgnoreCase(cmd.getProvider())) {
                continue;
            }
            if (cmd.getGpuProfileId() != null && !instance.getGpuProfileId().equals(cmd.getGpuProfileId())) {
                continue;
            }
            responses.add(buildInstanceResponse(instance));
        }
        return responses;
    }

    @Override
    @ActionEvent(eventType = GpuEventTypes.EVENT_GPU_INSTANCE_START,
                eventDescription = "starting gpu service instance")
    public GpuInstanceResponse startGpuInstance(StartGpuInstanceCmd cmd) {
        GpuServiceInstanceVO instance = getOwnedInstance(cmd.getId());
        if (instance.getVmId() != null) {
            try {
                UserVm vm = userVmService.getUserVm(instance.getVmId());
                if (vm != null) {
                    userVmService.startVirtualMachine(vm, null);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to start GPU VM: " + e.getMessage());
            }
        }
        instance.setState(State.RUNNING);
        gpuServiceInstanceDao.update(instance.getId(), instance);
        return buildInstanceResponse(instance);
    }

    @Override
    @ActionEvent(eventType = GpuEventTypes.EVENT_GPU_INSTANCE_STOP,
                eventDescription = "stopping gpu service instance")
    public GpuInstanceResponse stopGpuInstance(StopGpuInstanceCmd cmd) {
        GpuServiceInstanceVO instance = getOwnedInstance(cmd.getId());
        if (instance.getVmId() != null) {
            try {
                userVmService.stopVirtualMachine(instance.getVmId(), false);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to stop GPU VM: " + e.getMessage());
            }
        }
        instance.setState(State.STOPPED);
        gpuServiceInstanceDao.update(instance.getId(), instance);
        return buildInstanceResponse(instance);
    }

    @Override
    @ActionEvent(eventType = GpuEventTypes.EVENT_GPU_INSTANCE_DELETE,
                eventDescription = "deleting gpu service instance")
    public GpuInstanceResponse deleteGpuInstance(DeleteGpuInstanceCmd cmd) {
        GpuServiceInstanceVO instance = getOwnedInstance(cmd.getId());
        if (instance.getVmId() != null) {
            try {
                userVmService.destroyVm(instance.getVmId(), false);
            } catch (Exception e) {
                logger.warn("Failed to destroy VM for GPU instance " + instance.getName(), e);
            }
        }
        instance.setState(State.DESTROYED);
        instance.setRemoved(new Date());
        gpuServiceInstanceDao.update(instance.getId(), instance);
        return buildInstanceResponse(instance);
    }

    @Override
    public List<GpuProfileResponse> listGpuProfiles(ListGpuProfilesCmd cmd) {
        List<GpuProfileResponse> responses = new ArrayList<>();
        for (ProfileDefinition profile : profileMap.values()) {
            if (cmd.getProvider() != null && !profile.provider.equalsIgnoreCase(cmd.getProvider())) {
                continue;
            }
            if (cmd.getKeyword() != null) {
                String keyword = cmd.getKeyword();
                if (!profile.id.toLowerCase().contains(keyword) && !profile.name.toLowerCase().contains(keyword)) {
                    continue;
                }
            }
            GpuProfileResponse response = new GpuProfileResponse();
            response.setId(profile.id);
            response.setName(profile.name);
            response.setProvider(profile.provider);
            response.setGpuVendor(profile.gpuVendor);
            response.setGpuModel(profile.gpuModel);
            response.setMemoryGb(profile.memoryGb);
            response.setMaxGpuCount(profile.maxGpuCount);
            response.setDescription(profile.description);
            responses.add(response);
        }
        return responses;
    }

    @Override
    public List<GpuMetricResponse> listGpuMetrics(ListGpuMetricsCmd cmd) {
        List<GpuServiceInstanceVO> instances = resolveInstances(cmd.getId());
        List<GpuMetricResponse> responses = new ArrayList<>();

        Date now = new Date();
        for (GpuServiceInstanceVO instance : instances) {
            if (instance.getRemoved() != null) {
                continue;
            }
            ProfileDefinition profile = profileMap.get(instance.getGpuProfileId());
            if (profile == null) {
                continue;
            }

            int seed = Math.abs(instance.getUuid().hashCode());
            double baseGpu = 35.0 + (seed % 50);
            double baseMem = 20.0 + ((seed / 13) % 60);

            GpuMetricResponse response = new GpuMetricResponse();
            response.setGpuInstanceId(instance.getUuid());
            response.setName(instance.getName());
            response.setProvider(instance.getProvider());
            response.setGpuProfileId(instance.getGpuProfileId());
            response.setGpuUtilization(round(baseGpu));
            response.setMemoryUtilization(round(baseMem));
            response.setMemoryTotalMb((long) profile.memoryGb * 1024L * instance.getGpuCount());
            response.setPowerWatts(round(120 + (baseGpu * 1.8)));
            response.setTemperatureC(round(42 + (baseGpu / 6.0)));
            response.setTimestamp(now);
            responses.add(response);
        }

        return responses;
    }

    @Override
    public List<GpuUsageResponse> listGpuUsage(ListGpuUsageCmd cmd) {
        Date endDate = cmd.getEndDate() != null ? cmd.getEndDate() : new Date();
        Date startDate = cmd.getStartDate();
        if (startDate == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDate);
            calendar.add(Calendar.HOUR, -24);
            startDate = calendar.getTime();
        }
        if (endDate.before(startDate)) {
            throw new IllegalArgumentException("enddate must be after startdate");
        }

        List<GpuServiceInstanceVO> instances = resolveInstances(cmd.getId());
        Map<String, Double> priceMap = parseDoubleMap(GpuProfileHourlyRateMap.value());
        List<GpuUsageResponse> responses = new ArrayList<>();

        for (GpuServiceInstanceVO instance : instances) {
            if (instance.getRemoved() != null && instance.getRemoved().before(startDate)) {
                continue;
            }
            if (cmd.getProvider() != null && !instance.getProvider().equalsIgnoreCase(cmd.getProvider())) {
                continue;
            }
            if (cmd.getGpuProfileId() != null && !instance.getGpuProfileId().equals(cmd.getGpuProfileId())) {
                continue;
            }

            Date effectiveStart = instance.getCreated().after(startDate) ? instance.getCreated() : startDate;
            Date effectiveEnd = instance.getRemoved() != null && instance.getRemoved().before(endDate) ? instance.getRemoved() : endDate;
            long runningMs = Math.max(0L, effectiveEnd.getTime() - effectiveStart.getTime());
            if (instance.getState() == State.STOPPED || instance.getState() == State.ERROR) {
                runningMs = 0L;
            }

            double runningHours = round(runningMs / (1000.0 * 60.0 * 60.0));
            double hourlyRate = priceMap.getOrDefault(instance.getGpuProfileId(), 0.0);
            double totalCost = round(runningHours * instance.getGpuCount() * hourlyRate);

            GpuUsageResponse usage = new GpuUsageResponse();
            usage.setGpuInstanceId(instance.getUuid());
            usage.setName(instance.getName());
            usage.setProvider(instance.getProvider());
            usage.setGpuProfileId(instance.getGpuProfileId());
            usage.setGpuCount(instance.getGpuCount());
            usage.setRunningHours(runningHours);
            usage.setHourlyRateUsd(hourlyRate);
            usage.setTotalCostUsd(totalCost);
            usage.setStartDate(startDate);
            usage.setEndDate(endDate);
            responses.add(usage);
        }

        return responses;
    }

    @Override
    public List<Class<?>> getCommands() {
        List<Class<?>> commands = new ArrayList<>();
        commands.add(CreateGpuInstanceCmd.class);
        commands.add(ListGpuInstancesCmd.class);
        commands.add(StartGpuInstanceCmd.class);
        commands.add(StopGpuInstanceCmd.class);
        commands.add(DeleteGpuInstanceCmd.class);
        commands.add(ListGpuProfilesCmd.class);
        commands.add(ListGpuMetricsCmd.class);
        commands.add(ListGpuUsageCmd.class);
        return commands;
    }

    @Override
    public String getConfigComponentName() {
        return GpuServiceManagerImpl.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[] {
            GpuMaxActivePerAccount,
            GpuProfileQuotaMap,
            GpuProfileHourlyRateMap
        };
    }

    private List<GpuServiceInstanceVO> resolveInstances(String id) {
        long accountId = CallContext.current().getCallingAccountId();
        if (id != null) {
            GpuServiceInstanceVO instance = gpuServiceInstanceDao.findByUuid(id);
            if (instance == null || instance.getAccountId() != accountId) {
                return new ArrayList<>();
            }
            List<GpuServiceInstanceVO> one = new ArrayList<>();
            one.add(instance);
            return one;
        }
        return gpuServiceInstanceDao.listByAccountId(accountId);
    }

    private GpuServiceInstanceVO getOwnedInstance(String id) {
        GpuServiceInstanceVO instance = gpuServiceInstanceDao.findByUuid(id);
        if (instance == null || instance.getRemoved() != null) {
            throw new IllegalArgumentException("GPU instance not found: " + id);
        }

        long accountId = CallContext.current().getCallingAccountId();
        if (instance.getAccountId() != accountId) {
            throw new IllegalArgumentException("GPU instance does not belong to caller: " + id);
        }
        return instance;
    }

    private GpuInstanceResponse buildInstanceResponse(GpuServiceInstanceVO instance) {
        GpuInstanceResponse response = new GpuInstanceResponse();
        response.setId(instance.getUuid());
        response.setName(instance.getName());
        response.setState(instance.getState().name());
        response.setProvider(instance.getProvider());
        response.setGpuProfileId(instance.getGpuProfileId());
        response.setGpuCount(instance.getGpuCount());
        response.setZoneId(String.valueOf(instance.getZoneId()));
        response.setVmId(instance.getVmId() != null ? String.valueOf(instance.getVmId()) : null);
        response.setServiceOfferingId(String.valueOf(instance.getServiceOfferingId()));
        response.setTemplateId(String.valueOf(instance.getTemplateId()));
        response.setNetworkId(instance.getNetworkId() != null ? String.valueOf(instance.getNetworkId()) : null);
        response.setCreated(instance.getCreated());

        Account account = accountManager.getAccount(instance.getAccountId());
        if (account != null) {
            response.setAccount(account.getAccountName());
            response.setDomainId(String.valueOf(account.getDomainId()));
        } else {
            response.setDomainId(String.valueOf(instance.getDomainId()));
        }

        return response;
    }

    private void addProfile(String id, String provider, String gpuVendor, String gpuModel,
                            int memoryGb, int maxGpuCount, String vgpuTypeHint, String description) {
        profileMap.put(id, new ProfileDefinition(id, id, provider, gpuVendor, gpuModel, memoryGb, maxGpuCount, vgpuTypeHint, description));
    }

    private void enforceQuota(long accountId, String profileId, int requestedGpuCount) {
        int accountActive = activeGpuCountForAccount(accountId);
        int maxPerAccount = Math.max(MIN_GPU_COUNT, GpuMaxActivePerAccount.value());
        if (accountActive + requestedGpuCount > maxPerAccount) {
            throw new IllegalArgumentException("Account GPU quota exceeded: requested=" + requestedGpuCount +
                ", active=" + accountActive + ", limit=" + maxPerAccount);
        }

        int activeByProfile = activeGpuCountForProfile(accountId, profileId);
        int profileLimit = Math.max(MIN_GPU_COUNT, getProfileQuota(profileId));
        if (activeByProfile + requestedGpuCount > profileLimit) {
            throw new IllegalArgumentException("Profile GPU quota exceeded for " + profileId + ": requested=" +
                requestedGpuCount + ", active=" + activeByProfile + ", limit=" + profileLimit);
        }
    }

    private int activeGpuCountForAccount(long accountId) {
        int total = 0;
        for (GpuServiceInstanceVO instance : gpuServiceInstanceDao.listByAccountId(accountId)) {
            if (instance.getRemoved() == null && instance.getState() != State.DESTROYED) {
                total += instance.getGpuCount();
            }
        }
        return total;
    }

    private int activeGpuCountForProfile(long accountId, String profileId) {
        int total = 0;
        for (GpuServiceInstanceVO instance : gpuServiceInstanceDao.listByAccountId(accountId)) {
            if (instance.getRemoved() == null && instance.getState() != State.DESTROYED && profileId.equals(instance.getGpuProfileId())) {
                total += instance.getGpuCount();
            }
        }
        return total;
    }

    private int getProfileQuota(String profileId) {
        Map<String, Double> quotaMap = parseDoubleMap(GpuProfileQuotaMap.value());
        Double configured = quotaMap.get(profileId);
        return configured != null ? configured.intValue() : MAX_GPU_COUNT;
    }

    private boolean hasEnoughGpuCapacity(long zoneId, ProfileDefinition profile, int requestedGpuCount) {
        List<VgpuTypesInfo> capacities = vgpuTypesDao.listGPUCapacities(zoneId, null, null);
        if (capacities == null || capacities.isEmpty()) {
            return false;
        }

        long remaining = 0;
        for (VgpuTypesInfo info : capacities) {
            String groupName = info.getGroupName() != null ? info.getGroupName().toUpperCase() : "";
            String modelName = info.getModelName() != null ? info.getModelName().toUpperCase() : "";
            String hint = profile.vgpuTypeHint.toUpperCase();
            if (!groupName.contains(hint) && !modelName.contains(hint)) {
                continue;
            }
            if (info.getRemainingCapacity() != null) {
                remaining += info.getRemainingCapacity();
            }
        }
        return remaining >= requestedGpuCount;
    }

    private Map<String, Double> parseDoubleMap(String raw) {
        Map<String, Double> values = new LinkedHashMap<>();
        if (raw == null || raw.isEmpty()) {
            return values;
        }
        String[] entries = raw.split(",");
        for (String entry : entries) {
            String[] kv = entry.trim().split(":", 2);
            if (kv.length != 2) {
                continue;
            }
            try {
                values.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return values;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class ProfileDefinition {
        private final String id;
        private final String name;
        private final String provider;
        private final String gpuVendor;
        private final String gpuModel;
        private final int memoryGb;
        private final int maxGpuCount;
        private final String vgpuTypeHint;
        private final String description;

        private ProfileDefinition(String id, String name, String provider, String gpuVendor,
                                  String gpuModel, int memoryGb, int maxGpuCount, String vgpuTypeHint, String description) {
            this.id = id;
            this.name = name;
            this.provider = provider;
            this.gpuVendor = gpuVendor;
            this.gpuModel = gpuModel;
            this.memoryGb = memoryGb;
            this.maxGpuCount = maxGpuCount;
            this.vgpuTypeHint = vgpuTypeHint;
            this.description = description;
        }
    }
}
