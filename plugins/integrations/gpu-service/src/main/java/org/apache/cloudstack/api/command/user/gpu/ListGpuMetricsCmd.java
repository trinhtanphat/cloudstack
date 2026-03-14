// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.gpu;

import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.GpuMetricResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.gpu.GpuAsAService;

@APICommand(name = "listGpuMetrics",
            description = "List GPU telemetry metrics for instances",
            responseObject = GpuMetricResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListGpuMetricsCmd extends BaseListCmd {

    @Inject
    public GpuAsAService gpuService;

    @Parameter(name = "id", type = CommandType.STRING, description = "GPU instance id")
    private String id;

    public String getId() { return id; }

    @Override
    public void execute() throws ServerApiException {
        List<GpuMetricResponse> responses = gpuService.listGpuMetrics(this);
        ListResponse<GpuMetricResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responses, responses.size());
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
