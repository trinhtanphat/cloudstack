// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.gpu;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.GpuInstanceResponse;

import com.cloud.gpu.GpuAsAService;

@APICommand(name = "startGpuInstance",
            description = "Start a GPU service instance",
            responseObject = GpuInstanceResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class StartGpuInstanceCmd extends BaseCmd {

    @Inject
    public GpuAsAService gpuService;

    @Parameter(name = "id", type = CommandType.STRING, required = true, description = "GPU instance id")
    private String id;

    public String getId() { return id; }

    @Override
    public void execute() throws ServerApiException {
        GpuInstanceResponse response = gpuService.startGpuInstance(this);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return org.apache.cloudstack.context.CallContext.current().getCallingAccountId();
    }
}
