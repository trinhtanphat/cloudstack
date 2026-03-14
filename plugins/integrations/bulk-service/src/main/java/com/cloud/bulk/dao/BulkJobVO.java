// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.bulk.dao;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "bulk_provisioning_jobs")
public class BulkJobVO {

    public enum Status { PENDING, RUNNING, COMPLETED, FAILED, PARTIAL }
    public enum JobType { DEPLOY_VMS, ALLOCATE_IPS }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "job_type")
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "total_count")
    private int totalCount;

    @Column(name = "completed_count")
    private int completedCount;

    @Column(name = "failed_count")
    private int failedCount;

    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "service_offering_id")
    private Long serviceOfferingId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "network_id")
    private Long networkId;

    @Column(name = "name_prefix")
    private String namePrefix;

    @Column(name = "assign_public_ip")
    private boolean assignPublicIp;

    @Column(name = "batch_size")
    private int batchSize;

    @Column(name = "parameters", length = 4096)
    private String parameters;

    @Column(name = "result_data", length = 65535)
    private String resultData;

    @Column(name = "error_data", length = 65535)
    private String errorData;

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "completed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completed;

    public BulkJobVO() {
        this.uuid = UUID.randomUUID().toString();
        this.status = Status.PENDING;
        this.created = new Date();
    }

    // Getters and Setters
    public long getId() { return id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }
    public long getDomainId() { return domainId; }
    public void setDomainId(long domainId) { this.domainId = domainId; }
    public JobType getJobType() { return jobType; }
    public void setJobType(JobType jobType) { this.jobType = jobType; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public Long getServiceOfferingId() { return serviceOfferingId; }
    public void setServiceOfferingId(Long serviceOfferingId) { this.serviceOfferingId = serviceOfferingId; }
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public Long getNetworkId() { return networkId; }
    public void setNetworkId(Long networkId) { this.networkId = networkId; }
    public String getNamePrefix() { return namePrefix; }
    public void setNamePrefix(String namePrefix) { this.namePrefix = namePrefix; }
    public boolean isAssignPublicIp() { return assignPublicIp; }
    public void setAssignPublicIp(boolean assignPublicIp) { this.assignPublicIp = assignPublicIp; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    public String getResultData() { return resultData; }
    public void setResultData(String resultData) { this.resultData = resultData; }
    public String getErrorData() { return errorData; }
    public void setErrorData(String errorData) { this.errorData = errorData; }
    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }
    public Date getCompleted() { return completed; }
    public void setCompleted(Date completed) { this.completed = completed; }
}
