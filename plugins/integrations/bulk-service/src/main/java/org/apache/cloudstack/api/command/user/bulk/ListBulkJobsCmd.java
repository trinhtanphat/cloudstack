// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.bulk;

import java.util.List;

import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.BulkJobResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.bulk.BulkProvisioningService;

@APICommand(name = "listBulkJobs",
            description = "List bulk provisioning jobs with status and progress",
            responseObject = BulkJobResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListBulkJobsCmd extends BaseListCmd {

    @Inject
    public BulkProvisioningService bulkProvisioningService;

    @Parameter(name = "jobid",
               type = CommandType.STRING,
               description = "Filter by specific bulk job ID")
    private String jobId;

    @Parameter(name = "status",
               type = CommandType.STRING,
               description = "Filter by status: PENDING, RUNNING, COMPLETED, FAILED, PARTIAL")
    private String status;

    @Parameter(name = "jobtype",
               type = CommandType.STRING,
               description = "Filter by job type: DEPLOY_VMS, ALLOCATE_IPS")
    private String jobType;

    public String getJobId() { return jobId; }
    public String getStatus() { return status; }
    public String getJobType() { return jobType; }

    @Override
    public void execute() throws ServerApiException {
        List<BulkJobResponse> responses = bulkProvisioningService.listBulkJobs(this);
        ListResponse<BulkJobResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responses, responses.size());
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
