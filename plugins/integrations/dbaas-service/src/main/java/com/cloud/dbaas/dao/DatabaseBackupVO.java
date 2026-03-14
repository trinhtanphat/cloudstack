// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas.dao;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "dbaas_backups")
public class DatabaseBackupVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Column(name = "db_instance_id")
    private long dbInstanceId;

    @Column(name = "account_id")
    private long accountId;

    @Column(name = "backup_type")
    private String backupType; // FULL, INCREMENTAL

    @Column(name = "status")
    private String status; // CREATING, COMPLETED, FAILED

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(name = "volume_snapshot_id")
    private Long volumeSnapshotId;

    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "expires")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expires;

    public DatabaseBackupVO() {
        this.uuid = UUID.randomUUID().toString();
        this.created = new Date();
    }

    // Getters and Setters
    public long getId() { return id; }
    public String getUuid() { return uuid; }
    public long getDbInstanceId() { return dbInstanceId; }
    public void setDbInstanceId(long id) { this.dbInstanceId = id; }
    public long getAccountId() { return accountId; }
    public void setAccountId(long accountId) { this.accountId = accountId; }
    public String getBackupType() { return backupType; }
    public void setBackupType(String backupType) { this.backupType = backupType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public Long getVolumeSnapshotId() { return volumeSnapshotId; }
    public void setVolumeSnapshotId(Long id) { this.volumeSnapshotId = id; }
    public Date getCreated() { return created; }
    public Date getExpires() { return expires; }
    public void setExpires(Date expires) { this.expires = expires; }
}
