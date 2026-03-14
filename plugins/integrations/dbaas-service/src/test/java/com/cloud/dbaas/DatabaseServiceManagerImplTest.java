// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.cloudstack.api.command.user.dbaas.CheckDatabaseHealthCmd;
import org.apache.cloudstack.api.command.user.dbaas.CreateDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.dbaas.DeleteDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseInstancesCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseOfferingsCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseUsageCmd;
import org.apache.cloudstack.api.response.DatabaseHealthCheckResponse;
import org.apache.cloudstack.api.response.DatabaseInstanceResponse;
import org.apache.cloudstack.api.response.DatabaseOfferingResponse;
import org.apache.cloudstack.api.response.DatabaseUsageResponse;
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

import com.cloud.dbaas.dao.DatabaseBackupDao;
import com.cloud.dbaas.dao.DatabaseInstanceDao;
import com.cloud.dbaas.dao.DatabaseInstanceVO;
import com.cloud.dbaas.dao.DatabaseInstanceVO.DbEngine;
import com.cloud.dbaas.dao.DatabaseInstanceVO.State;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.network.NetworkService;
import com.cloud.network.firewall.FirewallService;
import com.cloud.network.rules.RulesService;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.uservm.UserVm;
import com.cloud.vm.UserVmService;
import com.cloud.vm.VirtualMachine;

import java.util.ArrayList;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DatabaseServiceManagerImplTest {

    @InjectMocks
    private DatabaseServiceManagerImpl manager;

    @Mock private DatabaseInstanceDao dbInstanceDao;
    @Mock private DatabaseBackupDao dbBackupDao;
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
    public void testListDatabaseOfferings_returnsAllEngines() {
        ListDatabaseOfferingsCmd cmd = new ListDatabaseOfferingsCmd();
        List<DatabaseOfferingResponse> offerings = manager.listDatabaseOfferings(cmd);

        assertNotNull(offerings);
        assertEquals("Should return 6 default offerings", 6, offerings.size());
    }

    @Test
    public void testListDatabaseOfferings_filterByEngine() throws Exception {
        ListDatabaseOfferingsCmd cmd = new ListDatabaseOfferingsCmd();

        Field engineField = ListDatabaseOfferingsCmd.class.getDeclaredField("dbEngine");
        engineField.setAccessible(true);
        engineField.set(cmd, "MYSQL");

        List<DatabaseOfferingResponse> offerings = manager.listDatabaseOfferings(cmd);

        assertNotNull(offerings);
        assertEquals("Should return only MySQL offering", 1, offerings.size());
        assertEquals("MYSQL", offerings.get(0).getDbEngine());
    }

    @Test
    public void testListDatabaseInstances_empty() {
        when(dbInstanceDao.listByAccountId(1L)).thenReturn(new ArrayList<>());

        ListDatabaseInstancesCmd cmd = new ListDatabaseInstancesCmd();
        List<DatabaseInstanceResponse> instances = manager.listDatabaseInstances(cmd);

        assertNotNull(instances);
        assertTrue(instances.isEmpty());
    }

    @Test
    public void testListDatabaseInstances_withResults() {
        DatabaseInstanceVO instance1 = createMockInstance(1L, "test-mysql", DbEngine.MYSQL, State.RUNNING);
        DatabaseInstanceVO instance2 = createMockInstance(2L, "test-pg", DbEngine.POSTGRESQL, State.RUNNING);

        when(dbInstanceDao.listByAccountId(1L)).thenReturn(List.of(instance1, instance2));
        when(accountManager.getAccount(1L)).thenReturn(testAccount);

        DataCenterVO zone = mock(DataCenterVO.class);
        when(zone.getName()).thenReturn("zone-1");
        when(dataCenterDao.findById(anyLong())).thenReturn(zone);

        ListDatabaseInstancesCmd cmd = new ListDatabaseInstancesCmd();
        List<DatabaseInstanceResponse> instances = manager.listDatabaseInstances(cmd);

        assertNotNull(instances);
        assertEquals(2, instances.size());
    }

    @Test
    public void testDeleteDatabaseInstance() throws Exception {
        DatabaseInstanceVO instance = createMockInstance(1L, "test-mysql", DbEngine.MYSQL, State.RUNNING);
        instance.setVmId(100L);

        when(dbInstanceDao.findByUuid("uuid-1")).thenReturn(instance);
        when(accountManager.getAccount(1L)).thenReturn(testAccount);

        DataCenterVO zone = mock(DataCenterVO.class);
        when(zone.getName()).thenReturn("zone-1");
        when(dataCenterDao.findById(anyLong())).thenReturn(zone);
        when(dbInstanceDao.update(anyLong(), any(DatabaseInstanceVO.class))).thenReturn(true);

        UserVm vm = mock(UserVm.class);
        when(userVmService.destroyVm(100L, false)).thenReturn(vm);

        DeleteDatabaseInstanceCmd cmd = mock(DeleteDatabaseInstanceCmd.class);
        when(cmd.getId()).thenReturn("uuid-1");
        when(cmd.getExpunge()).thenReturn(false);

        DatabaseInstanceResponse response = manager.deleteDatabaseInstance(cmd);
        assertNotNull(response);
        verify(userVmService).destroyVm(100L, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteDatabaseInstance_notFound() throws Exception {
        when(dbInstanceDao.findByUuid("nonexistent")).thenReturn(null);

        DeleteDatabaseInstanceCmd cmd = mock(DeleteDatabaseInstanceCmd.class);
        when(cmd.getId()).thenReturn("nonexistent");
        when(cmd.getExpunge()).thenReturn(false);

        manager.deleteDatabaseInstance(cmd);
    }

    @Test
    public void testCheckDatabaseHealth_vmRunning() {
        DatabaseInstanceVO instance = createMockInstance(1L, "test-mysql", DbEngine.MYSQL, State.RUNNING);
        instance.setVmId(100L);
        instance.setIpAddress("10.0.0.5");
        instance.setPort(3306);

        when(dbInstanceDao.findById(1L)).thenReturn(instance);

        UserVm vm = mock(UserVm.class);
        when(vm.getState()).thenReturn(VirtualMachine.State.Running);
        when(userVmService.getUserVm(100L)).thenReturn(vm);

        CheckDatabaseHealthCmd cmd = mock(CheckDatabaseHealthCmd.class);
        when(cmd.getId()).thenReturn(1L);

        DatabaseHealthCheckResponse response = manager.checkDatabaseHealth(cmd);

        assertNotNull(response);
        assertEquals("RUNNING", response.getState());
        assertEquals("Running", response.getVmState());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckDatabaseHealth_instanceNotFound() {
        when(dbInstanceDao.findById(999L)).thenReturn(null);

        CheckDatabaseHealthCmd cmd = mock(CheckDatabaseHealthCmd.class);
        when(cmd.getId()).thenReturn(999L);

        manager.checkDatabaseHealth(cmd);
    }

    @Test
    public void testListDatabaseUsage_emptyResults() {
        when(dbInstanceDao.listByAccountId(1L)).thenReturn(new ArrayList<>());

        ListDatabaseUsageCmd cmd = mock(ListDatabaseUsageCmd.class);
        when(cmd.getId()).thenReturn(null);
        when(cmd.getStartDate()).thenReturn(null);
        when(cmd.getEndDate()).thenReturn(null);

        List<DatabaseUsageResponse> responses = manager.listDatabaseUsage(cmd);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    public void testListDatabaseUsage_withInstances() {
        DatabaseInstanceVO instance = createMockInstance(1L, "test-mysql", DbEngine.MYSQL, State.RUNNING);
        instance.setCpuCores(2);
        instance.setMemoryMb(2048);
        instance.setStorageSizeGb(20);

        when(dbInstanceDao.listByAccountId(1L)).thenReturn(List.of(instance));
        when(dbBackupDao.listByInstanceId(1L)).thenReturn(new ArrayList<>());
        when(accountManager.getAccount(1L)).thenReturn(testAccount);

        ListDatabaseUsageCmd cmd = mock(ListDatabaseUsageCmd.class);
        when(cmd.getId()).thenReturn(null);
        when(cmd.getStartDate()).thenReturn(null);
        when(cmd.getEndDate()).thenReturn(null);

        List<DatabaseUsageResponse> responses = manager.listDatabaseUsage(cmd);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("test-mysql", responses.get(0).getName());
    }

    @Test
    public void testGetCommands_returnsAllCommands() {
        List<Class<?>> commands = manager.getCommands();
        assertNotNull(commands);
        assertEquals(11, commands.size());
    }

    // ──────────────────────────────────────────────────────────────────
    //  INPUT VALIDATION / SECURITY TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_nameSqlInjection() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("test'; DROP TABLE dbaas_instances;--");
        when(cmd.getDbEngine()).thenReturn("MYSQL");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.createDatabaseInstance(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_nameCommandInjection() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("test$(curl attacker.com)");
        when(cmd.getDbEngine()).thenReturn("MYSQL");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.createDatabaseInstance(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_nameXss() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("<img src=x onerror=alert(1)>");
        when(cmd.getDbEngine()).thenReturn("MYSQL");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.createDatabaseInstance(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_invalidEngine() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("valid-name");
        when(cmd.getDbEngine()).thenReturn("ORACLE");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.createDatabaseInstance(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_invalidIpMode() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("valid-name");
        when(cmd.getDbEngine()).thenReturn("MYSQL");
        when(cmd.getIpMode()).thenReturn("HACK_MODE");

        manager.createDatabaseInstance(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_weakPassword() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("valid-name");
        when(cmd.getDbEngine()).thenReturn("MYSQL");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");
        when(cmd.getAdminPassword()).thenReturn("short");
        when(cmd.getAllowedCidr()).thenReturn("0.0.0.0/0");
        when(cmd.getZoneId()).thenReturn(1L);
        when(dataCenterDao.findById(1L)).thenReturn(mock(DataCenterVO.class));
        when(serviceOfferingDao.findById(anyLong())).thenReturn(mock(ServiceOfferingVO.class));

        manager.createDatabaseInstance(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_passwordTooLong() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("valid-name");
        when(cmd.getDbEngine()).thenReturn("MYSQL");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");
        // 200 character password exceeds limit
        when(cmd.getAdminPassword()).thenReturn("A".repeat(200));
        when(cmd.getAllowedCidr()).thenReturn("0.0.0.0/0");
        when(cmd.getZoneId()).thenReturn(1L);
        when(dataCenterDao.findById(1L)).thenReturn(mock(DataCenterVO.class));
        when(serviceOfferingDao.findById(anyLong())).thenReturn(mock(ServiceOfferingVO.class));

        manager.createDatabaseInstance(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_invalidCidr() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("valid-name");
        when(cmd.getDbEngine()).thenReturn("MYSQL");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");
        when(cmd.getAdminPassword()).thenReturn(null);
        when(cmd.getAllowedCidr()).thenReturn("not-a-cidr");
        when(cmd.getZoneId()).thenReturn(1L);
        when(dataCenterDao.findById(1L)).thenReturn(mock(DataCenterVO.class));
        when(serviceOfferingDao.findById(anyLong())).thenReturn(mock(ServiceOfferingVO.class));

        manager.createDatabaseInstance(cmd);
    }

    // ──────────────────────────────────────────────────────────────────
    //  PORT FORWARDING ADAPTER TESTS
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void testPortForwardingRuleAdapter_mysqlPort() {
        DatabaseServiceManagerImpl.PortForwardingRuleAdapter adapter =
            new DatabaseServiceManagerImpl.PortForwardingRuleAdapter(
                50L, 30000, 3306, "tcp", 10L, 1L, 1L, 100L);

        assertEquals(Long.valueOf(50L), adapter.getSourceIpAddressId());
        assertEquals(Integer.valueOf(30000), adapter.getSourcePortStart());
        assertEquals(Integer.valueOf(30000), adapter.getSourcePortEnd());
        assertEquals(3306, adapter.getDestinationPortStart());
        assertEquals(3306, adapter.getDestinationPortEnd());
        assertEquals("tcp", adapter.getProtocol());
        assertEquals(100L, adapter.getVirtualMachineId());
        assertEquals(com.cloud.network.rules.FirewallRule.Purpose.PortForwarding, adapter.getPurpose());
    }

    @Test
    public void testPortForwardingRuleAdapter_postgresqlPort() {
        DatabaseServiceManagerImpl.PortForwardingRuleAdapter adapter =
            new DatabaseServiceManagerImpl.PortForwardingRuleAdapter(
                50L, 40000, 5432, "tcp", 10L, 1L, 1L, 101L);

        assertEquals(5432, adapter.getDestinationPortStart());
        assertEquals(101L, adapter.getVirtualMachineId());
    }

    @Test
    public void testPortForwardingRuleAdapter_redisPort() {
        DatabaseServiceManagerImpl.PortForwardingRuleAdapter adapter =
            new DatabaseServiceManagerImpl.PortForwardingRuleAdapter(
                50L, 50000, 6379, "tcp", 10L, 1L, 1L, 102L);

        assertEquals(6379, adapter.getDestinationPortStart());
    }

    // ──────────────────────────────────────────────────────────────────
    //  DELETE WITH PORT CLEANUP TEST
    // ──────────────────────────────────────────────────────────────────

    @Test
    public void testDeleteDatabaseInstance_withPublicPort() throws Exception {
        DatabaseInstanceVO instance = createMockInstance(1L, "test-mysql-pf", DbEngine.MYSQL, State.RUNNING);
        instance.setVmId(100L);
        instance.setPublicIpId(50L);
        instance.setPublicIpAddress("203.0.113.5");
        instance.setPublicPort(30000);

        when(dbInstanceDao.findByUuid("uuid-pf")).thenReturn(instance);
        when(dbInstanceDao.update(anyLong(), any(DatabaseInstanceVO.class))).thenReturn(true);

        UserVm vm = mock(UserVm.class);
        when(userVmService.destroyVm(100L, false)).thenReturn(vm);

        DeleteDatabaseInstanceCmd cmd = mock(DeleteDatabaseInstanceCmd.class);
        when(cmd.getId()).thenReturn("uuid-pf");
        when(cmd.getExpunge()).thenReturn(false);

        DatabaseInstanceResponse response = manager.deleteDatabaseInstance(cmd);
        assertNotNull(response);
        verify(networkService).releaseIpAddress(50L);
        verify(userVmService).destroyVm(100L, false);
    }

    // ──────────────────────────────────────────────────────────────────
    //  PENTEST SCENARIOS
    // ──────────────────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_engineInjection() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("valid-name");
        when(cmd.getDbEngine()).thenReturn("MYSQL; DROP TABLE");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.createDatabaseInstance(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_cidrSsrfAttempt() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("valid-name");
        when(cmd.getDbEngine()).thenReturn("MYSQL");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");
        when(cmd.getAdminPassword()).thenReturn(null);
        when(cmd.getAllowedCidr()).thenReturn("http://169.254.169.254/latest/meta-data/");
        when(cmd.getZoneId()).thenReturn(1L);
        when(dataCenterDao.findById(1L)).thenReturn(mock(DataCenterVO.class));
        when(serviceOfferingDao.findById(anyLong())).thenReturn(mock(ServiceOfferingVO.class));

        manager.createDatabaseInstance(cmd);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseInstance_namePathTraversal() {
        CreateDatabaseInstanceCmd cmd = mock(CreateDatabaseInstanceCmd.class);
        when(cmd.getName()).thenReturn("../../etc/passwd");
        when(cmd.getDbEngine()).thenReturn("MYSQL");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");

        manager.createDatabaseInstance(cmd);
    }

    // ──────────────────────────────────────────────────────────────────
    //  HELPER
    // ──────────────────────────────────────────────────────────────────

    private DatabaseInstanceVO createMockInstance(long id, String name, DbEngine engine, State state) {
        DatabaseInstanceVO vo = new DatabaseInstanceVO();
        vo.setName(name);
        vo.setDbEngine(engine);
        vo.setState(state);
        vo.setAccountId(1L);
        vo.setDomainId(1L);
        vo.setZoneId(1L);
        vo.setServiceOfferingId(1L);
        vo.setTemplateId(1L);
        vo.setPort(engine == DbEngine.MYSQL ? 3306 : 5432);
        vo.setCpuCores(1);
        vo.setMemoryMb(1024);
        vo.setStorageSizeGb(10);

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
