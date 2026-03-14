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
import org.apache.cloudstack.api.response.GpuProfileResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.gpu.GpuAsAService;

@APICommand(name = "listGpuProfiles",
            description = "List supported GPU profiles",
            responseObject = GpuProfileResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListGpuProfilesCmd extends BaseListCmd {

    @Inject
    public GpuAsAService gpuService;

    @Parameter(name = "provider", type = CommandType.STRING, description = "Provider profile family: AWS, AZURE, GCP")
    private String provider;

    @Parameter(name = "keyword", type = CommandType.STRING, description = "Keyword to filter profile id or name")
    private String keyword;

    public String getProvider() { return provider != null ? provider.toUpperCase() : null; }
    public String getKeyword() { return keyword != null ? keyword.toLowerCase() : null; }

    @Override
    public void execute() throws ServerApiException {
        List<GpuProfileResponse> responses = gpuService.listGpuProfiles(this);
        ListResponse<GpuProfileResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responses, responses.size());
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
