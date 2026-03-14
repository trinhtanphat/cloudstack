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
import org.apache.cloudstack.api.response.DatabaseOfferingResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.dbaas.DatabaseAsAService;

@APICommand(name = "listDatabaseOfferings",
            description = "List available database engine offerings (MySQL, PostgreSQL, MongoDB, etc.)",
            responseObject = DatabaseOfferingResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListDatabaseOfferingsCmd extends BaseListCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = "dbengine",
               type = CommandType.STRING,
               description = "Filter by database engine type")
    private String dbEngine;

    public String getDbEngine() { return dbEngine; }

    @Override
    public void execute() throws ServerApiException {
        List<DatabaseOfferingResponse> responses = databaseService.listDatabaseOfferings(this);
        ListResponse<DatabaseOfferingResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responses, responses.size());
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
