// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.dbaas;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DatabaseInstanceResponse;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;

import com.cloud.dbaas.DatabaseAsAService;

@APICommand(name = "scaleDatabaseInstance",
            description = "Scale a database instance to a different service offering (CPU/RAM)",
            responseObject = DatabaseInstanceResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ScaleDatabaseInstanceCmd extends BaseCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = "id",
               type = CommandType.STRING,
               required = true,
               description = "ID of the database instance")
    private String id;

    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID,
               type = CommandType.UUID,
               entityType = ServiceOfferingResponse.class,
               required = true,
               description = "New service offering for the database VM")
    private Long serviceOfferingId;

    public String getId() { return id; }
    public Long getServiceOfferingId() { return serviceOfferingId; }

    @Override
    public void execute() throws ServerApiException {
        DatabaseInstanceResponse response = databaseService.scaleDatabaseInstance(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}
