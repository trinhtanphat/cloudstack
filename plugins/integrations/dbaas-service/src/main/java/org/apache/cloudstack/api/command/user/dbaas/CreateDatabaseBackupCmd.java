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
import org.apache.cloudstack.api.response.DatabaseBackupResponse;

import com.cloud.dbaas.DatabaseAsAService;

@APICommand(name = "createDatabaseBackup",
            description = "Create a backup of a managed database instance",
            responseObject = DatabaseBackupResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class CreateDatabaseBackupCmd extends BaseCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = "dbinstanceid",
               type = CommandType.STRING,
               required = true,
               description = "ID of the database instance to backup")
    private String dbInstanceId;

    @Parameter(name = "backuptype",
               type = CommandType.STRING,
               description = "Backup type: FULL or INCREMENTAL (default: FULL)")
    private String backupType;

    public String getDbInstanceId() { return dbInstanceId; }
    public String getBackupType() { return backupType != null ? backupType : "FULL"; }

    @Override
    public void execute() throws ServerApiException {
        DatabaseBackupResponse response = databaseService.createDatabaseBackup(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}
