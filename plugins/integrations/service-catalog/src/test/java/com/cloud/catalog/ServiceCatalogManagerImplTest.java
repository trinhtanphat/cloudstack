// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.catalog;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.UUID;

import org.apache.cloudstack.api.command.user.catalog.DeployCatalogItemCmd;
import org.apache.cloudstack.api.response.CatalogDeploymentResponse;
import org.apache.cloudstack.api.response.DatabaseInstanceResponse;
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

import com.cloud.dc.dao.DataCenterDao;
import com.cloud.dbaas.DatabaseAsAService;
import com.cloud.network.NetworkService;
import com.cloud.network.rules.RulesService;
import com.cloud.service.dao.ServiceOfferingDao;
import com.cloud.storage.dao.VMTemplateDao;
import com.cloud.user.Account;
import com.cloud.user.AccountManager;
import com.cloud.user.AccountVO;
import com.cloud.user.UserVO;
import com.cloud.vm.UserVmService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceCatalogManagerImplTest {

    @InjectMocks
    private ServiceCatalogManagerImpl manager;

    @Mock private AccountManager accountManager;
    @Mock private DatabaseAsAService databaseService;
    @Mock private UserVmService userVmService;
    @Mock private DataCenterDao dataCenterDao;
    @Mock private ServiceOfferingDao serviceOfferingDao;
    @Mock private VMTemplateDao templateDao;
    @Mock private NetworkService networkService;
    @Mock private RulesService rulesService;

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

        when(accountManager.getAccount(1L)).thenReturn(testAccount);
    }

    @After
    public void tearDown() {
        if (callContextMock != null) {
            callContextMock.close();
        }
    }

    @Test
    public void testDeployCatalogItem_databaseDelegatesToDbaas() {
        DeployCatalogItemCmd cmd = mock(DeployCatalogItemCmd.class);
        when(cmd.getCatalogItemId()).thenReturn("mysql-8.0");
        when(cmd.getName()).thenReturn("orders-db");
        when(cmd.getZoneId()).thenReturn(1L);
        when(cmd.getServiceOfferingId()).thenReturn(2L);
        when(cmd.getTemplateId()).thenReturn(3L);
        when(cmd.getNetworkId()).thenReturn(4L);
        when(cmd.getAssignPublicIp()).thenReturn(true);
        when(cmd.getIpMode()).thenReturn("PORT_FORWARD");
        when(cmd.getAllowedCidr()).thenReturn("10.0.0.0/8");
        when(cmd.getCount()).thenReturn(1);

        when(databaseService.createDatabaseInstance(any())).thenReturn(new DatabaseInstanceResponse());

        CatalogDeploymentResponse response = manager.deployCatalogItem(cmd);

        assertNotNull(response);
        verify(databaseService).createDatabaseInstance(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeployCatalogItem_invalidCount() {
        DeployCatalogItemCmd cmd = mock(DeployCatalogItemCmd.class);
        when(cmd.getCatalogItemId()).thenReturn("nginx-latest");
        when(cmd.getAllowedCidr()).thenReturn("0.0.0.0/0");
        when(cmd.getIpMode()).thenReturn("STATIC_NAT");
        when(cmd.getCount()).thenReturn(0);

        manager.deployCatalogItem(cmd);
    }
}