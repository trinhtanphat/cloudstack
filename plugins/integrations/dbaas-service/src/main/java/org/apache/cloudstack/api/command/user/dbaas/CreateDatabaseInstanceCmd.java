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
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import com.cloud.dbaas.DatabaseAsAService;

@APICommand(name = "createDatabaseInstance",
            description = "Create a managed database instance (MySQL, PostgreSQL, MongoDB, SQL Server, Redis, phpMyAdmin)",
            responseObject = DatabaseInstanceResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class CreateDatabaseInstanceCmd extends BaseCmd {

    @Inject
    public DatabaseAsAService databaseService;

    @Parameter(name = ApiConstants.NAME,
               type = CommandType.STRING,
               required = true,
               description = "Name for the database instance")
    private String name;

    @Parameter(name = "dbengine",
               type = CommandType.STRING,
               required = true,
               description = "Database engine: MYSQL, POSTGRESQL, MONGODB, SQLSERVER, REDIS, PHPMYADMIN")
    private String dbEngine;

    @Parameter(name = "dbversion",
               type = CommandType.STRING,
               description = "Database version (e.g., '8.0' for MySQL, '16' for PostgreSQL, '7.0' for MongoDB)")
    private String dbVersion;

    @Parameter(name = ApiConstants.ZONE_ID,
               type = CommandType.UUID,
               entityType = ZoneResponse.class,
               required = true,
               description = "Zone to deploy the database instance in")
    private Long zoneId;

    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID,
               type = CommandType.UUID,
               entityType = ServiceOfferingResponse.class,
               required = true,
               description = "Service offering (CPU/RAM) for the database VM")
    private Long serviceOfferingId;

    @Parameter(name = ApiConstants.TEMPLATE_ID,
               type = CommandType.UUID,
               entityType = org.apache.cloudstack.api.response.TemplateResponse.class,
               required = true,
               description = "Template (OS image) for the database VM - should support cloud-init")
    private Long templateId;

    @Parameter(name = ApiConstants.NETWORK_ID,
               type = CommandType.UUID,
               entityType = NetworkResponse.class,
               description = "Network to connect the database instance to")
    private Long networkId;

    @Parameter(name = "storagesizegb",
               type = CommandType.INTEGER,
               description = "Storage size in GB (default: 20)")
    private Integer storageSizeGb;

    @Parameter(name = "adminusername",
               type = CommandType.STRING,
               description = "Admin username for the database (default: dbadmin)")
    private String adminUsername;

    @Parameter(name = "adminpassword",
               type = CommandType.STRING,
               description = "Admin password for the database (auto-generated if not specified)")
    private String adminPassword;

    @Parameter(name = "assignpublicip",
               type = CommandType.BOOLEAN,
               description = "Assign a public IP to the database instance (default: false)")
    private Boolean assignPublicIp;

    @Parameter(name = "ipmode",
               type = CommandType.STRING,
               description = "IP assignment mode: STATIC_NAT (dedicated IP, default) or PORT_FORWARD (shared IP + random port)")
    private String ipMode;

    @Parameter(name = "allowedcidr",
               type = CommandType.STRING,
               description = "CIDR for restricting access to the database port (default: 0.0.0.0/0)")
    private String allowedCidr;

    @Parameter(name = "backupenabled",
               type = CommandType.BOOLEAN,
               description = "Enable automatic daily backups (default: true)")
    private Boolean backupEnabled;

    @Parameter(name = "highavailability",
               type = CommandType.BOOLEAN,
               description = "Enable high availability mode with replica (default: false)")
    private Boolean highAvailability;

    // Getters
    public String getName() { return name; }
    public String getDbEngine() { return dbEngine; }
    public String getDbVersion() { return dbVersion; }
    public Long getZoneId() { return zoneId; }
    public Long getServiceOfferingId() { return serviceOfferingId; }
    public Long getTemplateId() { return templateId; }
    public Long getNetworkId() { return networkId; }
    public Integer getStorageSizeGb() { return storageSizeGb != null ? storageSizeGb : 20; }
    public String getAdminUsername() { return adminUsername != null ? adminUsername : "dbadmin"; }
    public String getAdminPassword() { return adminPassword; }
    public Boolean getAssignPublicIp() { return assignPublicIp != null ? assignPublicIp : false; }
    public String getIpMode() { return ipMode != null ? ipMode.toUpperCase() : "STATIC_NAT"; }
    public String getAllowedCidr() { return allowedCidr != null ? allowedCidr : "0.0.0.0/0"; }
    public Boolean getBackupEnabled() { return backupEnabled != null ? backupEnabled : true; }
    public Boolean getHighAvailability() { return highAvailability != null ? highAvailability : false; }

    @Override
    public void execute() throws ServerApiException {
        DatabaseInstanceResponse response = databaseService.createDatabaseInstance(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}
