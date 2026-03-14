// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.dbaas;

import java.util.Date;
import java.util.List;
import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.DatabaseUsageResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.dbaas.DatabaseAsAService;

@APICommand(name = "listDatabaseUsage",
            description = "List usage/billing records for database instances including CPU hours, memory hours, storage hours, and backup usage",
            responseObject = DatabaseUsageResponse.class,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListDatabaseUsageCmd extends BaseListCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = "id",
               type = CommandType.UUID,
               entityType = DatabaseUsageResponse.class,
               description = "The ID of a specific database instance to get usage for")
    private Long id;

    @Parameter(name = "startdate",
               type = CommandType.DATE,
               description = "Start date for usage period (format: yyyy-MM-dd)")
    private Date startDate;

    @Parameter(name = "enddate",
               type = CommandType.DATE,
               description = "End date for usage period (format: yyyy-MM-dd)")
    private Date endDate;

    public Long getId() { return id; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }

    @Override
    public void execute() throws ServerApiException {
        List<DatabaseUsageResponse> responses = databaseService.listDatabaseUsage(this);
        ListResponse<DatabaseUsageResponse> listResp = new ListResponse<>();
        listResp.setResponses(responses, responses.size());
        listResp.setResponseName(getCommandName());
        setResponseObject(listResp);
    }
}
