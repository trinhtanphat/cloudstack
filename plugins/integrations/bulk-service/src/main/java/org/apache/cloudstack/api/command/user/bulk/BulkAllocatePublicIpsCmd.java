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
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import com.cloud.bulk.BulkProvisioningService;

@APICommand(name = "bulkAllocatePublicIpAddresses",
            description = "Allocate multiple public IP addresses in bulk for a zone/network",
            responseObject = BulkJobResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class BulkAllocatePublicIpsCmd extends BaseCmd {

    @Inject
    public BulkProvisioningService bulkProvisioningService;

    @Parameter(name = "count",
               type = CommandType.INTEGER,
               required = true,
               description = "Number of public IPs to allocate (1-10000)")
    private Integer count;

    @Parameter(name = ApiConstants.ZONE_ID,
               type = CommandType.UUID,
               entityType = ZoneResponse.class,
               required = true,
               description = "Zone to allocate IPs from")
    private Long zoneId;

    @Parameter(name = ApiConstants.NETWORK_ID,
               type = CommandType.UUID,
               entityType = NetworkResponse.class,
               description = "Network to associate IPs with")
    private Long networkId;

    @Parameter(name = "assigntovms",
               type = CommandType.BOOLEAN,
               description = "If true, auto-assign IPs to VMs in the same account that don't have a public IP yet")
    private Boolean assignToVMs;

    @Parameter(name = "startipaddress",
               type = CommandType.STRING,
               description = "Starting IP address for sequential allocation from a specific range")
    private String startIpAddress;

    public Integer getCount() { return count; }
    public Long getZoneId() { return zoneId; }
    public Long getNetworkId() { return networkId; }
    public Boolean getAssignToVMs() { return assignToVMs != null ? assignToVMs : false; }
    public String getStartIpAddress() { return startIpAddress; }

    @Override
    public void execute() throws ServerApiException {
        BulkJobResponse response = bulkProvisioningService.bulkAllocatePublicIps(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}
