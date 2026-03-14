// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.dbaas;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DatabaseInstanceResponse;

import com.cloud.dbaas.DatabaseAsAService;

@APICommand(name = "deleteDatabaseInstance",
            description = "Delete a managed database instance and its underlying VM",
            responseObject = DatabaseInstanceResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class DeleteDatabaseInstanceCmd extends BaseCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = "id",
               type = CommandType.STRING,
               required = true,
               description = "ID of the database instance to delete")
    private String id;

    @Parameter(name = "expunge",
               type = CommandType.BOOLEAN,
               description = "If true, immediately expunge the VM (default: false)")
    private Boolean expunge;

    public String getId() { return id; }
    public Boolean getExpunge() { return expunge != null ? expunge : false; }

    @Override
    public void execute() throws ServerApiException {
        DatabaseInstanceResponse response = databaseService.deleteDatabaseInstance(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}
