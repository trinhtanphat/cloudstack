// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.bulk;

import java.util.List;

import org.apache.cloudstack.api.command.user.bulk.BulkDeployVMsCmd;
import org.apache.cloudstack.api.command.user.bulk.BulkAllocatePublicIpsCmd;
import org.apache.cloudstack.api.command.user.bulk.ListBulkJobsCmd;
import org.apache.cloudstack.api.response.BulkJobResponse;
import org.apache.cloudstack.framework.config.Configurable;

import com.cloud.utils.component.PluggableService;

/**
 * Bulk Provisioning Service - enables mass VM deployment and IP allocation.
 * Supports creating up to 10,000 VMs in a single API call with automatic
 * public IP assignment, used for large-scale customer provisioning.
 */
public interface BulkProvisioningService extends PluggableService, Configurable {

    /**
     * Submit a bulk VM deployment job. Creates VMs in parallel batches.
     * @return job ID for tracking progress
     */
    BulkJobResponse bulkDeployVMs(BulkDeployVMsCmd cmd);

    /**
     * Allocate multiple public IPs in bulk and optionally assign to VMs.
     * @return job ID for tracking progress
     */
    BulkJobResponse bulkAllocatePublicIps(BulkAllocatePublicIpsCmd cmd);

    /**
     * List bulk provisioning jobs with status, progress, and results.
     */
    List<BulkJobResponse> listBulkJobs(ListBulkJobsCmd cmd);
}
