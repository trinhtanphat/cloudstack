// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.cloudstack.api.command.user.catalog.DeployCatalogItemCmd;
import org.apache.cloudstack.api.command.user.catalog.ListCatalogDeploymentStatusCmd;
import org.apache.cloudstack.api.response.CatalogDeploymentResponse;
import org.apache.cloudstack.api.response.CatalogDeploymentStatusResponse;
import org.apache.cloudstack.context.CallContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dbaas.DatabaseAsAService;
import com.cloud.network.IpAddress;
import com.cloud.network.NetworkService;
import com.cloud.network.rules.RulesService;
import com.cloud.utils.net.Ip;
import com.cloud.service.ServiceOfferingVO;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.VMTemplateVO;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.uservm.UserVm;
import com.cloud.vm.UserVmService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
    classes = ServiceCatalogGenericDeployIntegrationTest.Config.class)
public class ServiceCatalogGenericDeployIntegrationTest {

    @Configuration
    static class Config {
        @Bean
        ServiceCatalogManagerImpl serviceCatalogManagerImpl() {
            return new ServiceCatalogManagerImpl();
        }

        @Bean AccountManager accountManager() { return mock(AccountManager.class); }
        @Bean DatabaseAsAService databaseAsAService() { return mock(DatabaseAsAService.class); }
        @Bean UserVmService userVmService() { return mock(UserVmService.class); }
        @Bean DataCenterDao dataCenterDao() { return mock(DataCenterDao.class); }
        @Bean ServiceOfferingDao serviceOfferingDao() { return mock(ServiceOfferingDao.class); }
        @Bean VMTemplateDao vmTemplateDao() { return mock(VMTemplateDao.class); }
        @Bean NetworkService networkService() { return mock(NetworkService.class); }
        @Bean RulesService rulesService() { return mock(RulesService.class); }
    }

    @Inject private ServiceCatalogManagerImpl manager;
    @Inject private UserVmService userVmService;
    @Inject private DataCenterDao dataCenterDao;
    @Inject private ServiceOfferingDao serviceOfferingDao;
    @Inject private VMTemplateDao templateDao;
    @Inject private NetworkService networkService;
    @Inject private RulesService rulesService;

    private MockedStatic<CallContext> callContextMock;

    @Before
    public void setUp() throws Exception {
        AccountVO account = new AccountVO("testuser", 1L, "", Account.Type.NORMAL, UUID.randomUUID().toString());
        Field idField = AccountVO.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(account, 1L);

        CallContext ctx = mock(CallContext.class);
        when(ctx.getCallingAccount()).thenReturn(account);
        when(ctx.getCallingAccountId()).thenReturn(1L);

        UserVO user = mock(UserVO.class);
        when(user.getId()).thenReturn(1L);
        when(ctx.getCallingUser()).thenReturn(user);

        callContextMock = Mockito.mockStatic(CallContext.class);
        callContextMock.when(CallContext::current).thenReturn(ctx);
    }

    @After
    public void tearDown() {
        if (callContextMock != null) {
            callContextMock.close();
        }
    }

    @Test
    public void testGenericDeployPathEndToEnd() throws Exception {
        DeployCatalogItemCmd cmd = mock(DeployCatalogItemCmd.class);
        when(cmd.getCatalogItemId()).thenReturn("nginx-latest");
        when(cmd.getName()).thenReturn("web-01");
        when(cmd.getZoneId()).thenReturn(1L);
        when(cmd.getServiceOfferingId()).thenReturn(2L);
        when(cmd.getTemplateId()).thenReturn(3L);
        when(cmd.getNetworkId()).thenReturn(4L);
        when(cmd.getAssignPublicIp()).thenReturn(false);
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");
        when(cmd.getAllowedCidr()).thenReturn("0.0.0.0/0");
        when(cmd.getCount()).thenReturn(1);

        DataCenterVO zone = mock(DataCenterVO.class);
        ServiceOfferingVO offering = mock(ServiceOfferingVO.class);
        VMTemplateVO template = mock(VMTemplateVO.class);
        UserVm vm = mock(UserVm.class);

        when(dataCenterDao.findById(1L)).thenReturn(zone);
        when(serviceOfferingDao.findById(2L)).thenReturn(offering);
        when(templateDao.findById(3L)).thenReturn(template);
        when(vm.getId()).thenReturn(100L);
        when(vm.getPrivateIpAddress()).thenReturn("10.10.10.20");

        doReturn(vm).when(userVmService).createAdvancedVirtualMachine(
            any(), any(), any(), anyList(), any(), anyString(), anyString(),
            any(), any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), any(), anyBoolean(),
            any(), any(), any(), any()
        );

        CatalogDeploymentResponse response = manager.deployCatalogItem(cmd);
        assertNotNull(response);
        assertEquals("DEPLOYED", readField(response, "status"));
        assertEquals("100", readField(response, "vmId"));
        assertEquals("10.10.10.20", readField(response, "ipAddress"));

        verify(userVmService).createAdvancedVirtualMachine(
            any(), any(), any(), anyList(), any(), anyString(), anyString(),
            any(), any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), any(), anyBoolean(),
            any(), any(), any(), any()
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testBatchPortForwardTracksPerInstanceStatus() throws Exception {
        DeployCatalogItemCmd cmd = mock(DeployCatalogItemCmd.class);
        when(cmd.getCatalogItemId()).thenReturn("nginx-latest");
        when(cmd.getName()).thenReturn("web-batch");
        when(cmd.getZoneId()).thenReturn(1L);
        when(cmd.getServiceOfferingId()).thenReturn(2L);
        when(cmd.getTemplateId()).thenReturn(3L);
        when(cmd.getNetworkId()).thenReturn(4L);
        when(cmd.getAssignPublicIp()).thenReturn(true);
        when(cmd.getIpMode()).thenReturn("PORT_FORWARD");
        when(cmd.getAllowedCidr()).thenReturn("0.0.0.0/0");
        when(cmd.getPrivatePorts()).thenReturn("80");
        when(cmd.getCount()).thenReturn(2);

        DataCenterVO zone = mock(DataCenterVO.class);
        ServiceOfferingVO offering = mock(ServiceOfferingVO.class);
        VMTemplateVO template = mock(VMTemplateVO.class);
        IpAddress ipAddress = mock(IpAddress.class);
        UserVm vm1 = mock(UserVm.class);
        UserVm vm2 = mock(UserVm.class);

        when(dataCenterDao.findById(1L)).thenReturn(zone);
        when(serviceOfferingDao.findById(2L)).thenReturn(offering);
        when(templateDao.findById(3L)).thenReturn(template);

        when(ipAddress.getId()).thenReturn(101L);
        when(ipAddress.getAddress()).thenReturn(new Ip("203.0.113.10"));
        when(networkService.allocateIP(any(), anyLong(), any(), any(), any())).thenReturn(ipAddress);

        when(vm1.getId()).thenReturn(2001L);
        when(vm1.getPrivateIpAddress()).thenReturn("10.0.0.11");
        when(vm2.getId()).thenReturn(2002L);
        when(vm2.getPrivateIpAddress()).thenReturn("10.0.0.12");

        doReturn(vm1, vm2).when(userVmService).createAdvancedVirtualMachine(
            any(), any(), any(), anyList(), any(), anyString(), anyString(),
            any(), any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), any(), anyBoolean(),
            any(), any(), any(), any()
        );

        CatalogDeploymentResponse deployResponse = manager.deployCatalogItem(cmd);
        String batchOperationId = (String) readField(deployResponse, "batchOperationId");

        ListCatalogDeploymentStatusCmd statusCmd = mock(ListCatalogDeploymentStatusCmd.class);
        when(statusCmd.getBatchOperationId()).thenReturn(batchOperationId);
        CatalogDeploymentStatusResponse statusResponse = manager.listCatalogDeploymentStatus(statusCmd);

        assertEquals("COMPLETED", readField(statusResponse, "state"));
        assertEquals(2, readField(statusResponse, "totalCount"));
        assertEquals(2, readField(statusResponse, "completedCount"));
        assertEquals(0, readField(statusResponse, "failedCount"));

        List<Object> instances = (List<Object>) readField(statusResponse, "instances");
        assertEquals(2, instances.size());
        assertEquals("COMPLETED", readField(instances.get(0), "status"));
        assertEquals("203.0.113.10", readField(instances.get(0), "publicIp"));
        assertNotNull(readField(instances.get(0), "publicPort"));
        assertEquals("COMPLETED", readField(instances.get(1), "status"));
        assertEquals("203.0.113.10", readField(instances.get(1), "publicIp"));
        assertNotNull(readField(instances.get(1), "publicPort"));
        assertTrue(((Integer) readField(instances.get(0), "publicPort")) > 0);
        assertTrue(((Integer) readField(instances.get(1), "publicPort")) > 0);

        verify(networkService, times(1)).allocateIP(any(), anyLong(), any(), any(), any());
        verify(rulesService, times(2)).createPortForwardingRule(any(), anyLong(), any(), anyBoolean(), any());
        verify(rulesService, times(2)).applyPortForwardingRules(anyLong(), any());
    }

    private static Object readField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }
}
