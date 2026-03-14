// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.bulk;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.BulkJobResponse;
import org.apache.cloudstack.api.response.DiskOfferingResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import com.cloud.bulk.BulkProvisioningService;

@APICommand(name = "bulkDeployVirtualMachines",
            description = "Deploy multiple virtual machines in bulk with automatic naming and optional public IP assignment",
            responseObject = BulkJobResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class BulkDeployVMsCmd extends BaseCmd {

    @Inject
    public BulkProvisioningService bulkProvisioningService;

    @Parameter(name = "count",
               type = CommandType.INTEGER,
               required = true,
               description = "Number of VMs to deploy (1-10000)")
    private Integer count;

    @Parameter(name = ApiConstants.ZONE_ID,
               type = CommandType.UUID,
               entityType = ZoneResponse.class,
               required = true,
               description = "Availability zone for the VMs")
    private Long zoneId;

    @Parameter(name = ApiConstants.SERVICE_OFFERING_ID,
               type = CommandType.UUID,
               entityType = ServiceOfferingResponse.class,
               required = true,
               description = "Service offering (CPU/RAM) for the VMs")
    private Long serviceOfferingId;

    @Parameter(name = ApiConstants.TEMPLATE_ID,
               type = CommandType.UUID,
               entityType = TemplateResponse.class,
               required = true,
               description = "Template (OS image) for the VMs")
    private Long templateId;

    @Parameter(name = ApiConstants.DISK_OFFERING_ID,
               type = CommandType.UUID,
               entityType = DiskOfferingResponse.class,
               description = "Disk offering for additional data disk (optional)")
    private Long diskOfferingId;

    @Parameter(name = ApiConstants.NETWORK_ID,
               type = CommandType.UUID,
               entityType = NetworkResponse.class,
               description = "Network to attach VMs to")
    private Long networkId;

    @Parameter(name = "nameprefix",
               type = CommandType.STRING,
               required = true,
               description = "Prefix for VM names. VMs will be named prefix-001, prefix-002, etc.")
    private String namePrefix;

    @Parameter(name = "assignpublicip",
               type = CommandType.BOOLEAN,
               description = "If true, allocate a public IP and enable static NAT for each VM (default: false)")
    private Boolean assignPublicIp;

    @Parameter(name = "ipmode",
               type = CommandType.STRING,
               description = "IP assignment mode: STATIC_NAT (1 IP per VM, default) or PORT_FORWARD (shared IP + random ports)")
    private String ipMode;

    @Parameter(name = "privateports",
               type = CommandType.STRING,
               description = "Comma-separated list of private ports to forward (e.g. '22,80,443'). Used only with PORT_FORWARD mode. Default: '22'")
    private String privatePorts;

    @Parameter(name = "batchsize",
               type = CommandType.INTEGER,
               description = "Number of VMs to deploy in parallel per batch (default: 50, max: 200)")
    private Integer batchSize;

    @Parameter(name = "startipaddress",
               type = CommandType.STRING,
               description = "Starting IP address for sequential public IP allocation")
    private String startIpAddress;

    @Parameter(name = "keypairname",
               type = CommandType.STRING,
               description = "SSH key pair name to inject into VMs")
    private String keyPairName;

    @Parameter(name = "userdata",
               type = CommandType.STRING,
               description = "Base64-encoded user data for cloud-init")
    private String userData;

    // Getters
    public Integer getCount() { return count; }
    public Long getZoneId() { return zoneId; }
    public Long getServiceOfferingId() { return serviceOfferingId; }
    public Long getTemplateId() { return templateId; }
    public Long getDiskOfferingId() { return diskOfferingId; }
    public Long getNetworkId() { return networkId; }
    public String getNamePrefix() { return namePrefix; }
    public Boolean getAssignPublicIp() { return assignPublicIp != null ? assignPublicIp : false; }
    public String getIpMode() { return ipMode != null ? ipMode.toUpperCase() : "STATIC_NAT"; }
    public String getPrivatePorts() { return privatePorts != null ? privatePorts : "22"; }
    public Integer getBatchSize() { return batchSize != null ? Math.min(batchSize, 200) : 50; }
    public String getStartIpAddress() { return startIpAddress; }
    public String getKeyPairName() { return keyPairName; }
    public String getUserData() { return userData; }

    @Override
    public void execute() throws ServerApiException {
        BulkJobResponse response = bulkProvisioningService.bulkDeployVMs(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return callContext().getCallingAccountId();
    }

    private static org.apache.cloudstack.context.CallContext callContext() {
        return org.apache.cloudstack.context.CallContext.current();
    }
}
