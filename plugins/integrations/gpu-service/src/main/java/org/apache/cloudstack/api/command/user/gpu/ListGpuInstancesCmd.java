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
import org.apache.cloudstack.api.response.GpuInstanceResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.gpu.GpuAsAService;

@APICommand(name = "listGpuInstances",
            description = "List GPU service instances",
            responseObject = GpuInstanceResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListGpuInstancesCmd extends BaseListCmd {

    @Inject
    public GpuAsAService gpuService;

    @Parameter(name = "id", type = CommandType.STRING, description = "GPU instance id")
    private String id;

    @Parameter(name = "state", type = CommandType.STRING, description = "GPU instance state")
    private String state;

    @Parameter(name = "provider", type = CommandType.STRING, description = "Provider profile family")
    private String provider;

    @Parameter(name = "gpuprofileid", type = CommandType.STRING, description = "GPU profile id")
    private String gpuProfileId;

    public String getId() { return id; }
    public String getState() { return state != null ? state.toUpperCase() : null; }
    public String getProvider() { return provider != null ? provider.toUpperCase() : null; }
    public String getGpuProfileId() { return gpuProfileId; }

    @Override
    public void execute() throws ServerApiException {
        List<GpuInstanceResponse> responses = gpuService.listGpuInstances(this);
        ListResponse<GpuInstanceResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responses, responses.size());
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
