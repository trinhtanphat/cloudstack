// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.gpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.cloudstack.api.command.user.gpu.CreateGpuInstanceCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuMetricsCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuUsageCmd;
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

import com.cloud.agent.api.VgpuTypesInfo;
import com.cloud.dc.DataCenterVO;
import com.cloud.dc.dao.DataCenterDao;
import com.cloud.gpu.dao.GpuServiceInstanceDao;
import com.cloud.gpu.dao.GpuServiceInstanceVO;
import com.cloud.gpu.dao.VGPUTypesDao;
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

@RunWith(MockitoJUnitRunner.Silent.class)
public class GpuServiceManagerImplTest {

    @InjectMocks
    private GpuServiceManagerImpl manager;

    @Mock
    private GpuServiceInstanceDao gpuServiceInstanceDao;
    @Mock
    private AccountManager accountManager;
    @Mock
    private UserVmService userVmService;
    @Mock
    private DataCenterDao dataCenterDao;
    @Mock
    private ServiceOfferingDao serviceOfferingDao;
    @Mock
    private VMTemplateDao templateDao;
    @Mock
    private VGPUTypesDao vgpuTypesDao;

    private MockedStatic<CallContext> callContextMock;
    private AccountVO testAccount;

    @Before
    public void setUp() throws Exception {
        testAccount = new AccountVO("gpuuser", 1L, "", Account.Type.NORMAL, UUID.randomUUID().toString());

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

        when(accountManager.getAccount(1L)).thenReturn(testAccount);
    }

    @After
    public void tearDown() {
        if (callContextMock != null) {
            callContextMock.close();
        }
    }

    @Test
    public void testCreateGpuInstance_withVmLifecycleAndCapacityCheck() throws Exception {
        CreateGpuInstanceCmd cmd = mock(CreateGpuInstanceCmd.class);
        when(cmd.getName()).thenReturn("gpu-a100");
        when(cmd.getProvider()).thenReturn("GCP");
        when(cmd.getGpuProfileId()).thenReturn("gcp-a2-highgpu-1g");
        when(cmd.getGpuCount()).thenReturn(1);
        when(cmd.getZoneId()).thenReturn(1L);
        when(cmd.getServiceOfferingId()).thenReturn(2L);
        when(cmd.getTemplateId()).thenReturn(3L);
        when(cmd.getNetworkId()).thenReturn(4L);

        DataCenterVO zone = mock(DataCenterVO.class);
        ServiceOfferingVO offering = mock(ServiceOfferingVO.class);
        VMTemplateVO template = mock(VMTemplateVO.class);
        when(dataCenterDao.findById(1L)).thenReturn(zone);
        when(serviceOfferingDao.findById(2L)).thenReturn(offering);
        when(templateDao.findById(3L)).thenReturn(template);

        when(vgpuTypesDao.listGPUCapacities(1L, null, null)).thenReturn(List.of(
            new VgpuTypesInfo("grp-a100", "A100", null, null, null, null, 8L, 8L, 8L)
        ));

        when(gpuServiceInstanceDao.listByAccountId(1L)).thenReturn(List.of());

        UserVm vm = mock(UserVm.class);
        when(vm.getId()).thenReturn(99L);
        when(vm.getState()).thenReturn(com.cloud.vm.VirtualMachine.State.Running);
        when(userVmService.createAdvancedVirtualMachine(any(), any(), any(), any(), any(), any(), any(),
            any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean(), any(),
            any(), any(), any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any())).thenReturn(vm);

        when(gpuServiceInstanceDao.persist(any(GpuServiceInstanceVO.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertNotNull(manager.createGpuInstance(cmd));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateGpuInstance_quotaExceeded() {
        CreateGpuInstanceCmd cmd = mock(CreateGpuInstanceCmd.class);
        when(cmd.getName()).thenReturn("gpu-a100");
        when(cmd.getProvider()).thenReturn("GCP");
        when(cmd.getGpuProfileId()).thenReturn("gcp-a2-highgpu-1g");
        when(cmd.getGpuCount()).thenReturn(9);
        when(cmd.getZoneId()).thenReturn(1L);
        when(cmd.getServiceOfferingId()).thenReturn(2L);
        when(cmd.getTemplateId()).thenReturn(3L);

        GpuServiceInstanceVO existing = new GpuServiceInstanceVO();
        existing.setAccountId(1L);
        existing.setGpuProfileId("gcp-a2-highgpu-1g");
        existing.setGpuCount(8);
        when(gpuServiceInstanceDao.listByAccountId(1L)).thenReturn(List.of(existing));

        manager.createGpuInstance(cmd);
    }

    @Test
    public void testListGpuMetrics() {
        GpuServiceInstanceVO instance = new GpuServiceInstanceVO();
        instance.setAccountId(1L);
        instance.setName("gpu-demo");
        instance.setProvider("GCP");
        instance.setGpuProfileId("gcp-a2-highgpu-1g");
        instance.setGpuCount(1);

        when(gpuServiceInstanceDao.listByAccountId(1L)).thenReturn(List.of(instance));

        ListGpuMetricsCmd cmd = mock(ListGpuMetricsCmd.class);
        when(cmd.getId()).thenReturn(null);

        assertEquals(1, manager.listGpuMetrics(cmd).size());
    }

    @Test
    public void testListGpuUsage() {
        GpuServiceInstanceVO instance = new GpuServiceInstanceVO();
        instance.setAccountId(1L);
        instance.setName("gpu-demo");
        instance.setProvider("GCP");
        instance.setGpuProfileId("gcp-a2-highgpu-1g");
        instance.setGpuCount(2);

        when(gpuServiceInstanceDao.listByAccountId(1L)).thenReturn(List.of(instance));

        ListGpuUsageCmd cmd = mock(ListGpuUsageCmd.class);
        when(cmd.getId()).thenReturn(null);
        when(cmd.getProvider()).thenReturn(null);
        when(cmd.getGpuProfileId()).thenReturn(null);
        when(cmd.getStartDate()).thenReturn(new Date(System.currentTimeMillis() - 2 * 3600_000L));
        when(cmd.getEndDate()).thenReturn(new Date());

        assertTrue(manager.listGpuUsage(cmd).size() >= 1);
    }
}
