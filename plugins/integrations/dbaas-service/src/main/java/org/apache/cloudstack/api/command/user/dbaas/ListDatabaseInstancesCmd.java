// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.dbaas;

import java.util.List;
import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DatabaseInstanceResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.dbaas.DatabaseAsAService;

@APICommand(name = "listDatabaseInstances",
            description = "List managed database instances",
            responseObject = DatabaseInstanceResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListDatabaseInstancesCmd extends BaseListCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = "id",
               type = CommandType.STRING,
               description = "Filter by database instance ID")
    private String id;

    @Parameter(name = "dbengine",
               type = CommandType.STRING,
               description = "Filter by database engine: MYSQL, POSTGRESQL, MONGODB, SQLSERVER, REDIS, PHPMYADMIN")
    private String dbEngine;

    @Parameter(name = "state",
               type = CommandType.STRING,
               description = "Filter by state: CREATING, RUNNING, STOPPED, ERROR")
    private String state;

    public String getId() { return id; }
    public String getDbEngine() { return dbEngine; }
    public String getState() { return state; }

    @Override
    public void execute() throws ServerApiException {
        List<DatabaseInstanceResponse> responses = databaseService.listDatabaseInstances(this);
        ListResponse<DatabaseInstanceResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responses, responses.size());
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
