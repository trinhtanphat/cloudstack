// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.gpu.dao;

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
@Table(name = "gpu_service_instances")
public class GpuServiceInstanceVO {

    public enum State {
        RUNNING, STOPPED, ERROR, DESTROYED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "domain_id")
    private long domainId;

    @Column(name = "zone_id")
    private long zoneId;

    @Column(name = "vm_id")
    private Long vmId;

    @Column(name = "service_offering_id")
    private long serviceOfferingId;

    @Column(name = "template_id")
    private long templateId;

    @Column(name = "network_id")
    private Long networkId;

    @Column(name = "gpu_profile_id")
    private String gpuProfileId;

    @Column(name = "provider")
    private String provider;

    @Column(name = "gpu_count")
    private int gpuCount;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "removed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;

    public GpuServiceInstanceVO() {
        this.uuid = UUID.randomUUID().toString();
        this.created = new Date();
        this.state = State.RUNNING;
    }

    public long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }
    public long getDomainId() { return domainId; }
    public void setDomainId(long domainId) { this.domainId = domainId; }
    public long getZoneId() { return zoneId; }
    public void setZoneId(long zoneId) { this.zoneId = zoneId; }
    public Long getVmId() { return vmId; }
    public void setVmId(Long vmId) { this.vmId = vmId; }
    public long getServiceOfferingId() { return serviceOfferingId; }
    public void setServiceOfferingId(long serviceOfferingId) { this.serviceOfferingId = serviceOfferingId; }
    public long getTemplateId() { return templateId; }
    public void setTemplateId(long templateId) { this.templateId = templateId; }
    public Long getNetworkId() { return networkId; }
    public void setNetworkId(Long networkId) { this.networkId = networkId; }
    public String getGpuProfileId() { return gpuProfileId; }
    public void setGpuProfileId(String gpuProfileId) { this.gpuProfileId = gpuProfileId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public int getGpuCount() { return gpuCount; }
    public void setGpuCount(int gpuCount) { this.gpuCount = gpuCount; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public Date getCreated() { return created; }
    public Date getRemoved() { return removed; }
    public void setRemoved(Date removed) { this.removed = removed; }
}
