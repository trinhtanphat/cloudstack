// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.catalog;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.CatalogDeploymentStatusResponse;

import com.cloud.catalog.ServiceCatalogService;

@APICommand(name = "listCatalogDeploymentStatus",
            description = "List detailed per-instance status for a catalog batch deployment",
            responseObject = CatalogDeploymentStatusResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListCatalogDeploymentStatusCmd extends BaseCmd {

    @Inject
    public ServiceCatalogService catalogService;

    @Parameter(name = "batchoperationid",
               type = CommandType.STRING,
               required = true,
               description = "Catalog batch operation ID returned by deployCatalogItem")
    private String batchOperationId;

    public String getBatchOperationId() { return batchOperationId; }

    @Override
    public void execute() throws ServerApiException {
        CatalogDeploymentStatusResponse response = catalogService.listCatalogDeploymentStatus(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}