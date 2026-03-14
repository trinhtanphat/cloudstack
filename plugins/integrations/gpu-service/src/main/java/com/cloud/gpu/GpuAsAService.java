// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.gpu;

import java.util.List;

import org.apache.cloudstack.api.command.user.gpu.CreateGpuInstanceCmd;
import org.apache.cloudstack.api.command.user.gpu.DeleteGpuInstanceCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuInstancesCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuMetricsCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuProfilesCmd;
import org.apache.cloudstack.api.command.user.gpu.ListGpuUsageCmd;
import org.apache.cloudstack.api.command.user.gpu.StartGpuInstanceCmd;
import org.apache.cloudstack.api.command.user.gpu.StopGpuInstanceCmd;
import org.apache.cloudstack.api.response.GpuInstanceResponse;
import org.apache.cloudstack.api.response.GpuMetricResponse;
import org.apache.cloudstack.api.response.GpuProfileResponse;
import org.apache.cloudstack.api.response.GpuUsageResponse;
import org.apache.cloudstack.framework.config.Configurable;

import com.cloud.utils.component.PluggableService;

public interface GpuAsAService extends PluggableService, Configurable {
    GpuInstanceResponse createGpuInstance(CreateGpuInstanceCmd cmd);

    List<GpuInstanceResponse> listGpuInstances(ListGpuInstancesCmd cmd);

    GpuInstanceResponse startGpuInstance(StartGpuInstanceCmd cmd);

    GpuInstanceResponse stopGpuInstance(StopGpuInstanceCmd cmd);

    GpuInstanceResponse deleteGpuInstance(DeleteGpuInstanceCmd cmd);

    List<GpuProfileResponse> listGpuProfiles(ListGpuProfilesCmd cmd);

    List<GpuMetricResponse> listGpuMetrics(ListGpuMetricsCmd cmd);

    List<GpuUsageResponse> listGpuUsage(ListGpuUsageCmd cmd);
}
