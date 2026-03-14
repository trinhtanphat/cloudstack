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

@APICommand(name = "restartDatabaseInstance",
            description = "Restart a managed database instance",
            responseObject = DatabaseInstanceResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class RestartDatabaseInstanceCmd extends BaseCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = "id",
               type = CommandType.STRING,
               required = true,
               description = "ID of the database instance to restart")
    private String id;

    public String getId() { return id; }

    @Override
    public void execute() throws ServerApiException {
        DatabaseInstanceResponse response = databaseService.restartDatabaseInstance(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}
