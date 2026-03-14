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
import org.apache.cloudstack.api.response.DatabaseBackupResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.dbaas.DatabaseAsAService;

@APICommand(name = "listDatabaseBackups",
            description = "List backups for a managed database instance",
            responseObject = DatabaseBackupResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListDatabaseBackupsCmd extends BaseListCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = "dbinstanceid",
               type = CommandType.STRING,
               description = "Filter by database instance ID")
    private String dbInstanceId;

    public String getDbInstanceId() { return dbInstanceId; }

    @Override
    public void execute() throws ServerApiException {
        List<DatabaseBackupResponse> responses = databaseService.listDatabaseBackups(this);
        ListResponse<DatabaseBackupResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responses, responses.size());
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
