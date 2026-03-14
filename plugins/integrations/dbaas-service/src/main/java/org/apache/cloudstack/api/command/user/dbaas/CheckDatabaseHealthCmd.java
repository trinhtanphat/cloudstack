// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.dbaas;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DatabaseHealthCheckResponse;

import com.cloud.dbaas.DatabaseAsAService;

@APICommand(name = "checkDatabaseHealth",
            description = "Check the health status of a database instance including VM state, port reachability, and resource usage",
            responseObject = DatabaseHealthCheckResponse.class,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class CheckDatabaseHealthCmd extends BaseCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = ApiConstants.ID,
               type = CommandType.UUID,
               entityType = DatabaseHealthCheckResponse.class,
               required = true,
               description = "The ID of the database instance to check health for")
    private Long id;

    public Long getId() { return id; }

    @Override
    public void execute() throws ServerApiException {
        DatabaseHealthCheckResponse response = databaseService.checkDatabaseHealth(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return 0;
    }
}
