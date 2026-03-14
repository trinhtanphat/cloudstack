// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.catalog;

import javax.inject.Inject;

import com.cloud.catalog.ServiceCatalogEventTypes;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiCommandResourceType;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.CatalogDeploymentResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import com.cloud.catalog.ServiceCatalogService;

@APICommand(name = "deployCatalogItem",
            description = "Deploy a service from the catalog (one-click deployment of DB, web server, app stack, etc.)",
            responseObject = CatalogDeploymentResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class DeployCatalogItemCmd extends BaseAsyncCmd {

    @Inject
    public ServiceCatalogService catalogService;

    @Parameter(name = "catalogitemid",
               type = CommandType.STRING,
               required = true,
               description = "ID of the catalog item to deploy (e.g., mysql-8.0, wordpress, lamp-stack)")
    private String catalogItemId;

    @Parameter(name = ApiConstants.NAME,
               type = CommandType.STRING,
               required = true,
               description = "Name for the deployed instance")
    private String name;

    @Parameter(name = ApiConstants.ZONE_ID,
               type = CommandType.UUID,
               entityType = ZoneResponse.class,
               required = true,
               description = "Zone to deploy in")
    private Long zoneId;

    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID,
               type = CommandType.UUID,
               entityType = ServiceOfferingResponse.class,
               required = true,
               description = "Service offering (CPU/RAM)")
    private Long serviceOfferingId;

    @Parameter(name = ApiConstants.TEMPLATE_ID,
               type = CommandType.UUID,
               entityType = TemplateResponse.class,
               description = "Template (OS image) for generic service deployments. If omitted, the first ready zone template is used")
    private Long templateId;

    @Parameter(name = ApiConstants.NETWORK_ID,
               type = CommandType.UUID,
               entityType = NetworkResponse.class,
               description = "Network to connect to")
    private Long networkId;

    @Parameter(name = "assignpublicip",
               type = CommandType.BOOLEAN,
               description = "Assign public IP (default: false)")
    private Boolean assignPublicIp;

    @Parameter(name = "ipmode",
               type = CommandType.STRING,
               description = "IP assignment mode: STATIC_NAT (default) or PORT_FORWARD")
    private String ipMode;

    @Parameter(name = "privateports",
               type = CommandType.STRING,
               description = "Comma-separated private ports to forward in PORT_FORWARD mode. Defaults to the catalog item's default port")
    private String privatePorts;

    @Parameter(name = "allowedcidr",
               type = CommandType.STRING,
               description = "Allowed CIDR for ingress access in database and port-forward mode (default: 0.0.0.0/0)")
    private String allowedCidr;

    @Parameter(name = "count",
               type = CommandType.INTEGER,
               description = "Number of instances to deploy (default: 1)")
    private Integer count;

    // Getters
    public String getCatalogItemId() { return catalogItemId; }
    public String getName() { return name; }
    public Long getZoneId() { return zoneId; }
    public Long getServiceOfferingId() { return serviceOfferingId; }
    public Long getTemplateId() { return templateId; }
    public Long getNetworkId() { return networkId; }
    public Boolean getAssignPublicIp() { return assignPublicIp != null ? assignPublicIp : false; }
    public String getIpMode() { return ipMode != null ? ipMode.toUpperCase() : "STATIC_NAT"; }
    public String getPrivatePorts() { return privatePorts; }
    public String getAllowedCidr() { return allowedCidr != null ? allowedCidr : "0.0.0.0/0"; }
    public Integer getCount() { return count != null ? count : 1; }

    @Override
    public void execute() throws ServerApiException {
        CatalogDeploymentResponse response = catalogService.deployCatalogItem(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getEventType() {
        return ServiceCatalogEventTypes.EVENT_CATALOG_ITEM_DEPLOY;
    }

    @Override
    public String getEventDescription() {
        return "Deploying catalog item " + getCatalogItemId() + " as " + getName();
    }

    @Override
    public ApiCommandResourceType getApiResourceType() {
        return ApiCommandResourceType.VirtualMachine;
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}
