// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.gpu;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.GpuInstanceResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.ServiceOfferingResponse;
import org.apache.cloudstack.api.response.TemplateResponse;
import org.apache.cloudstack.api.response.ZoneResponse;

import com.cloud.gpu.GpuAsAService;

@APICommand(name = "createGpuInstance",
            description = "Create a GPU service instance",
            responseObject = GpuInstanceResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class CreateGpuInstanceCmd extends BaseCmd {

    @Inject
    public GpuAsAService gpuService;

    @Parameter(name = ApiConstants.NAME,
               type = CommandType.STRING,
               required = true,
               description = "Name for the GPU instance")
    private String name;

    @Parameter(name = "provider",
               type = CommandType.STRING,
               description = "Cloud provider profile family: AWS, AZURE, GCP")
    private String provider;

    @Parameter(name = "gpuprofileid",
               type = CommandType.STRING,
               required = true,
               description = "GPU profile id, for example: gcp-a2-highgpu-1g")
    private String gpuProfileId;

    @Parameter(name = "gpucount",
               type = CommandType.INTEGER,
               description = "GPU count for this instance")
    private Integer gpuCount;

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
               description = "Service offering")
    private Long serviceOfferingId;

    @Parameter(name = ApiConstants.TEMPLATE_ID,
               type = CommandType.UUID,
               entityType = TemplateResponse.class,
               required = true,
               description = "Template id")
    private Long templateId;

    @Parameter(name = ApiConstants.NETWORK_ID,
               type = CommandType.UUID,
               entityType = NetworkResponse.class,
               description = "Optional network id")
    private Long networkId;

    public String getName() { return name; }
    public String getProvider() { return provider != null ? provider.toUpperCase() : "GCP"; }
    public String getGpuProfileId() { return gpuProfileId; }
    public int getGpuCount() { return gpuCount != null ? gpuCount : 1; }
    public Long getZoneId() { return zoneId; }
    public Long getServiceOfferingId() { return serviceOfferingId; }
    public Long getTemplateId() { return templateId; }
    public Long getNetworkId() { return networkId; }

    @Override
    public void execute() throws ServerApiException {
        GpuInstanceResponse response = gpuService.createGpuInstance(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}
