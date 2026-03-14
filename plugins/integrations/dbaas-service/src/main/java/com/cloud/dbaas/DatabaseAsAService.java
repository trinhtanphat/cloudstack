// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas;

import java.util.List;

import org.apache.cloudstack.api.command.user.dbaas.CheckDatabaseHealthCmd;
import org.apache.cloudstack.api.command.user.dbaas.CreateDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.dbaas.DeleteDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseInstancesCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseOfferingsCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseUsageCmd;
import org.apache.cloudstack.api.command.user.dbaas.RestartDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.dbaas.ScaleDatabaseInstanceCmd;
import org.apache.cloudstack.api.command.user.dbaas.CreateDatabaseBackupCmd;
import org.apache.cloudstack.api.command.user.dbaas.ListDatabaseBackupsCmd;
import org.apache.cloudstack.api.command.user.dbaas.RestoreDatabaseBackupCmd;
import org.apache.cloudstack.api.response.DatabaseHealthCheckResponse;
import org.apache.cloudstack.api.response.DatabaseInstanceResponse;
import org.apache.cloudstack.api.response.DatabaseOfferingResponse;
import org.apache.cloudstack.api.response.DatabaseBackupResponse;
import org.apache.cloudstack.api.response.DatabaseUsageResponse;
import org.apache.cloudstack.framework.config.Configurable;

import com.cloud.utils.component.PluggableService;

/**
 * Database-as-a-Service Plugin for CloudStack.
 *
 * Provisions managed database instances (MySQL, PostgreSQL, MongoDB, SQL Server,
 * Redis, phpMyAdmin) on top of CloudStack VMs with pre-configured templates.
 *
 * Each database instance is backed by a dedicated CloudStack VM with:
 * - Pre-installed database software (via template or cloud-init)
 * - Automatic firewall rules for database ports
 * - Optional public IP assignment
 * - Backup scheduling integration
 * - Scaling (CPU/RAM) without data loss
 *
 * Supports: MySQL 8.0, PostgreSQL 16, MongoDB 7.0, SQL Server 2022,
 *           Redis 7.x, phpMyAdmin (as a web admin tool)
 */
public interface DatabaseAsAService extends PluggableService, Configurable {

    DatabaseInstanceResponse createDatabaseInstance(CreateDatabaseInstanceCmd cmd);

    List<DatabaseInstanceResponse> listDatabaseInstances(ListDatabaseInstancesCmd cmd);

    DatabaseInstanceResponse deleteDatabaseInstance(DeleteDatabaseInstanceCmd cmd);

    DatabaseInstanceResponse restartDatabaseInstance(RestartDatabaseInstanceCmd cmd);

    DatabaseInstanceResponse scaleDatabaseInstance(ScaleDatabaseInstanceCmd cmd);

    List<DatabaseOfferingResponse> listDatabaseOfferings(ListDatabaseOfferingsCmd cmd);

    DatabaseBackupResponse createDatabaseBackup(CreateDatabaseBackupCmd cmd);

    List<DatabaseBackupResponse> listDatabaseBackups(ListDatabaseBackupsCmd cmd);

    DatabaseInstanceResponse restoreDatabaseBackup(RestoreDatabaseBackupCmd cmd);

    DatabaseHealthCheckResponse checkDatabaseHealth(CheckDatabaseHealthCmd cmd);

    List<DatabaseUsageResponse> listDatabaseUsage(ListDatabaseUsageCmd cmd);
}
