// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.bulk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.cloudstack.api.command.user.bulk.BulkDeployVMsCmd;
import org.apache.cloudstack.api.command.user.bulk.ListBulkJobsCmd;
import org.apache.cloudstack.api.response.BulkJobResponse;
import org.apache.cloudstack.context.CallContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.cloud.bulk.dao.BulkJobDao;
import com.cloud.bulk.dao.BulkJobVO;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.network.NetworkService;
import com.cloud.network.firewall.FirewallService;
import com.cloud.network.rules.RulesService;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.vm.UserVmService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BulkProvisioningManagerImplTest {

    @InjectMocks
    private BulkProvisioningManagerImpl manager;

    @Mock private BulkJobDao bulkJobDao;
    @Mock private DataCenterDao dataCenterDao;
    @Mock private ServiceOfferingDao serviceOfferingDao;
    @Mock private VMTemplateDao templateDao;
    @Mock private UserVmService userVmService;
    @Mock private NetworkService networkService;
    @Mock private RulesService rulesService;
    @Mock private FirewallService firewallService;
    @Mock private AccountManager accountManager;

    private MockedStatic<CallContext> callContextMock;
    private AccountVO testAccount;

    @Before
    public void setUp() throws Exception {
        testAccount = new AccountVO("testuser", 1L, "", Account.Type.NORMAL, UUID.randomUUID().toString());

        Field idField = AccountVO.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testAccount, 1L);

        CallContext ctx = mock(CallContext.class);
        when(ctx.getCallingAccount()).thenReturn(testAccount);
        when(ctx.getCallingAccountId()).thenReturn(1L);

        UserVO testUser = mock(UserVO.class);
        when(testUser.getId()).thenReturn(1L);
        when(ctx.getCallingUser()).thenReturn(testUser);

        callContextMock = Mockito.mockStatic(CallContext.class);
        callContextMock.when(CallContext::current).thenReturn(ctx);
    }

    @After
    public void tearDown() {
        if (callContextMock != null) {
            callContextMock.close();
        }
    }

    // ──────────────────────────────────────────────────────────────────
    //  EXISTING TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void testListBulkJobs_empty() {
        when(bulkJobDao.listByAccountId(1L)).thenReturn(new ArrayList<>());

        ListBulkJobsCmd cmd = new ListBulkJobsCmd();
        List<BulkJobResponse> jobs = manager.listBulkJobs(cmd);

        assertNotNull(jobs);
        assertTrue(jobs.isEmpty());
    }

    @Test
    public void testListBulkJobs_withResults() {
        BulkJobVO job1 = createMockJob(1L, BulkJobVO.JobType.DEPLOY_VMS, BulkJobVO.Status.COMPLETED, 10, 10, 0);
        BulkJobVO job2 = createMockJob(2L, BulkJobVO.JobType.ALLOCATE_IPS, BulkJobVO.Status.RUNNING, 100, 50, 5);

        when(bulkJobDao.listByAccountId(1L)).thenReturn(List.of(job1, job2));

        ListBulkJobsCmd cmd = new ListBulkJobsCmd();
        List<BulkJobResponse> jobs = manager.listBulkJobs(cmd);

        assertNotNull(jobs);
        assertEquals(2, jobs.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_invalidCount() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(0);

        manager.bulkDeployVMs(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_tooMany() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(10001);

        manager.bulkDeployVMs(cmd);
    }

    @Test
    public void testBulkDeployVMs_validRequest() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(5);
        when(cmd.getNamePrefix()).thenReturn("test-vm");
        when(cmd.getZoneId()).thenReturn(1L);
        when(cmd.getServiceOfferingId()).thenReturn(1L);
        when(cmd.getTemplateId()).thenReturn(1L);
        when(cmd.getBatchSize()).thenReturn(50);
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");
        when(cmd.getAssignPublicIp()).thenReturn(false);

        DataCenterVO zone = mock(DataCenterVO.class);
        when(zone.getName()).thenReturn("zone-1");
        when(dataCenterDao.findById(1L)).thenReturn(zone);

        ServiceOfferingVO offering = mock(ServiceOfferingVO.class);
        when(serviceOfferingDao.findById(1L)).thenReturn(offering);

        VMTemplateVO template = mock(VMTemplateVO.class);
        when(templateDao.findById(1L)).thenReturn(template);

        BulkJobVO persistedJob = createMockJob(1L, BulkJobVO.JobType.DEPLOY_VMS, BulkJobVO.Status.PENDING, 5, 0, 0);
        when(bulkJobDao.persist(any(BulkJobVO.class))).thenReturn(persistedJob);

        BulkJobResponse response = manager.bulkDeployVMs(cmd);

        assertNotNull(response);
    }

    @Test
    public void testGetCommands_returnsAllCommands() {
        List<Class<?>> commands = manager.getCommands();
        assertNotNull(commands);
        assertEquals(3, commands.size());
    }

    // ──────────────────────────────────────────────────────────────────
    //  PORT FORWARDING MODE TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void testBulkDeployVMs_portForwardMode() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(3);
        when(cmd.getNamePrefix()).thenReturn("pf-vm");
        when(cmd.getZoneId()).thenReturn(1L);
        when(cmd.getServiceOfferingId()).thenReturn(1L);
        when(cmd.getTemplateId()).thenReturn(1L);
        when(cmd.getBatchSize()).thenReturn(50);
        when(cmd.getIpMode()).thenReturn("PORT_FORWARD");
        when(cmd.getAssignPublicIp()).thenReturn(true);
        when(cmd.getPrivatePorts()).thenReturn("22,80");

        DataCenterVO zone = mock(DataCenterVO.class);
        when(dataCenterDao.findById(1L)).thenReturn(zone);
        ServiceOfferingVO offering = mock(ServiceOfferingVO.class);
        when(serviceOfferingDao.findById(1L)).thenReturn(offering);
        VMTemplateVO template = mock(VMTemplateVO.class);
        when(templateDao.findById(1L)).thenReturn(template);

        BulkJobVO persistedJob = createMockJob(1L, BulkJobVO.JobType.DEPLOY_VMS, BulkJobVO.Status.PENDING, 3, 0, 0);
        when(bulkJobDao.persist(any(BulkJobVO.class))).thenReturn(persistedJob);

        BulkJobResponse response = manager.bulkDeployVMs(cmd);
        assertNotNull(response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_invalidIpMode() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(5);
        when(cmd.getNamePrefix()).thenReturn("test-vm");
        when(cmd.getIpMode()).thenReturn("INVALID_MODE");

        manager.bulkDeployVMs(cmd);
    }

    // ──────────────────────────────────────────────────────────────────
    //  INPUT VALIDATION / SECURITY TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_namePrefixSqlInjection() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(1);
        when(cmd.getNamePrefix()).thenReturn("test'; DROP TABLE users;--");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.bulkDeployVMs(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_namePrefixCommandInjection() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(1);
        when(cmd.getNamePrefix()).thenReturn("test$(whoami)");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.bulkDeployVMs(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_namePrefixXss() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(1);
        when(cmd.getNamePrefix()).thenReturn("<script>alert(1)</script>");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.bulkDeployVMs(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_namePrefixEmpty() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(1);
        when(cmd.getNamePrefix()).thenReturn("");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.bulkDeployVMs(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_namePrefixStartsWithHyphen() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(1);
        when(cmd.getNamePrefix()).thenReturn("-invalid");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.bulkDeployVMs(cmd);
    }

    // ──────────────────────────────────────────────────────────────────
    //  PORT PARSING TESTS (pentest: port injection/manipulation)
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void testParseAndValidatePorts_valid() {
        int[] ports = BulkProvisioningManagerImpl.parseAndValidatePorts("22,80,443");
        assertEquals(3, ports.length);
        assertEquals(22, ports[0]);
        assertEquals(80, ports[1]);
        assertEquals(443, ports[2]);
    }

    @Test
    public void testParseAndValidatePorts_singlePort() {
        int[] ports = BulkProvisioningManagerImpl.parseAndValidatePorts("22");
        assertEquals(1, ports.length);
        assertEquals(22, ports[0]);
    }

    @Test
    public void testParseAndValidatePorts_null() {
        int[] ports = BulkProvisioningManagerImpl.parseAndValidatePorts(null);
        assertEquals(1, ports.length);
        assertEquals(22, ports[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseAndValidatePorts_portZero() {
        BulkProvisioningManagerImpl.parseAndValidatePorts("0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseAndValidatePorts_portTooHigh() {
        BulkProvisioningManagerImpl.parseAndValidatePorts("70000");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseAndValidatePorts_negativePort() {
        BulkProvisioningManagerImpl.parseAndValidatePorts("-1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseAndValidatePorts_nonNumeric() {
        BulkProvisioningManagerImpl.parseAndValidatePorts("22,abc,80");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseAndValidatePorts_injectionAttempt() {
        BulkProvisioningManagerImpl.parseAndValidatePorts("22; rm -rf /");
    }

    // ──────────────────────────────────────────────────────────────────
    //  PORT FORWARDING RULE ADAPTER TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void testPortForwardingRuleAdapter_correctValues() {
        BulkProvisioningManagerImpl.PortForwardingRuleAdapter adapter =
            new BulkProvisioningManagerImpl.PortForwardingRuleAdapter(
                100L, 12345, 22, "tcp", 10L, 1L, 1L, 200L);

        assertEquals(Long.valueOf(100L), adapter.getSourceIpAddressId());
        assertEquals(Integer.valueOf(12345), adapter.getSourcePortStart());
        assertEquals(Integer.valueOf(12345), adapter.getSourcePortEnd());
        assertEquals(22, adapter.getDestinationPortStart());
        assertEquals(22, adapter.getDestinationPortEnd());
        assertEquals("tcp", adapter.getProtocol());
        assertEquals(200L, adapter.getVirtualMachineId());
        assertEquals(10L, adapter.getNetworkId());
        assertEquals(1L, adapter.getAccountId());
        assertEquals(1L, adapter.getDomainId());
        assertEquals(com.cloud.network.rules.FirewallRule.Purpose.PortForwarding, adapter.getPurpose());
        assertEquals(com.cloud.network.rules.FirewallRule.State.Add, adapter.getState());
        assertEquals(com.cloud.network.rules.FirewallRule.TrafficType.Ingress, adapter.getTrafficType());
        assertTrue(adapter.isDisplay());
    }

    // ──────────────────────────────────────────────────────────────────
    //  BOUNDARY / STRESS TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_countNegative() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(-1);

        manager.bulkDeployVMs(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_countMaxPlusOne() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(10001);

        manager.bulkDeployVMs(cmd);
    }

    @Test
    public void testBulkDeployVMs_countExactlyMax() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        when(cmd.getCount()).thenReturn(10000);
        when(cmd.getNamePrefix()).thenReturn("maxtest");
        when(cmd.getZoneId()).thenReturn(1L);
        when(cmd.getServiceOfferingId()).thenReturn(1L);
        when(cmd.getTemplateId()).thenReturn(1L);
        when(cmd.getBatchSize()).thenReturn(50);
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");
        when(cmd.getAssignPublicIp()).thenReturn(false);

        when(dataCenterDao.findById(1L)).thenReturn(mock(DataCenterVO.class));
        when(serviceOfferingDao.findById(1L)).thenReturn(mock(ServiceOfferingVO.class));
        when(templateDao.findById(1L)).thenReturn(mock(VMTemplateVO.class));

        BulkJobVO job = createMockJob(1L, BulkJobVO.JobType.DEPLOY_VMS, BulkJobVO.Status.PENDING, 10000, 0, 0);
        when(bulkJobDao.persist(any(BulkJobVO.class))).thenReturn(job);

        BulkJobResponse response = manager.bulkDeployVMs(cmd);
        assertNotNull(response);
        assertEquals(10000, response.getTotalCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBulkDeployVMs_portForwardTooManyVMs() {
        BulkDeployVMsCmd cmd = mock(BulkDeployVMsCmd.class);
        // 10000 VMs x 3 ports = 30000, which fits in range [10000-65000]
        // But 10000 VMs x 6 ports = 60000, exceeds 55000 available
        when(cmd.getCount()).thenReturn(10000);
        when(cmd.getNamePrefix()).thenReturn("pf-test");
        when(cmd.getIpMode()).thenReturn("PORT_FORWARD");
        when(cmd.getAssignPublicIp()).thenReturn(true);
        when(cmd.getPrivatePorts()).thenReturn("22,80,443,3306,5432,8080");

        when(dataCenterDao.findById(1L)).thenReturn(mock(DataCenterVO.class));
        when(serviceOfferingDao.findById(1L)).thenReturn(mock(ServiceOfferingVO.class));
        when(templateDao.findById(1L)).thenReturn(mock(VMTemplateVO.class));

        manager.bulkDeployVMs(cmd);
    }

    // ──────────────────────────────────────────────────────────────────
    //  HELPER
    // ──────────────────────────────────────────────────────────────────

    private BulkJobVO createMockJob(long id, BulkJobVO.JobType type, BulkJobVO.Status status,
                                     int total, int completed, int failed) {
        BulkJobVO vo = new BulkJobVO();
        vo.setJobType(type);
        vo.setStatus(status);
        vo.setTotalCount(total);
        vo.setCompletedCount(completed);
        vo.setFailedCount(failed);
        vo.setAccountId(1L);
        vo.setDomainId(1L);
        vo.setNamePrefix("test");

        try {
            Field idField = vo.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(vo, id);
        } catch (Exception e) {
            // ignore in test
        }

        return vo;
    }
}
