// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.response;

import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

import java.util.Date;
import java.util.List;

public class BulkJobResponse extends BaseResponse {

    @SerializedName("jobid")
    private String jobId;

    @SerializedName("jobtype")
    private String jobType;

    @SerializedName("status")
    private String status;

    @SerializedName("totalcount")
    private int totalCount;

    @SerializedName("completedcount")
    private int completedCount;

    @SerializedName("failedcount")
    private int failedCount;

    @SerializedName("progress")
    private int progress;

    @SerializedName("created")
    private Date created;

    @SerializedName("completed")
    private Date completed;

    @SerializedName("results")
    private List<BulkJobItemResponse> results;

    @SerializedName("errors")
    private List<String> errors;

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }

    public Date getCompleted() { return completed; }
    public void setCompleted(Date completed) { this.completed = completed; }

    public List<BulkJobItemResponse> getResults() { return results; }
    public void setResults(List<BulkJobItemResponse> results) { this.results = results; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
