// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.gpu;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.GpuUsageResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.gpu.GpuAsAService;

@APICommand(name = "listGpuUsage",
            description = "List GPU usage and billing for instances",
            responseObject = GpuUsageResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListGpuUsageCmd extends BaseListCmd {

    @Inject
    public GpuAsAService gpuService;

    @Parameter(name = "id", type = CommandType.STRING, description = "GPU instance id")
    private String id;

    @Parameter(name = "provider", type = CommandType.STRING, description = "Provider profile family")
    private String provider;

    @Parameter(name = "gpuprofileid", type = CommandType.STRING, description = "GPU profile id")
    private String gpuProfileId;

    @Parameter(name = "startdate", type = CommandType.DATE, description = "Usage window start date")
    private Date startDate;

    @Parameter(name = "enddate", type = CommandType.DATE, description = "Usage window end date")
    private Date endDate;

    public String getId() { return id; }
    public String getProvider() { return provider != null ? provider.toUpperCase() : null; }
    public String getGpuProfileId() { return gpuProfileId; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }

    @Override
    public void execute() throws ServerApiException {
        List<GpuUsageResponse> responses = gpuService.listGpuUsage(this);
        ListResponse<GpuUsageResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responses, responses.size());
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
