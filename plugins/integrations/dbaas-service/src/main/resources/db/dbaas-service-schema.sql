-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.
-- Schema for Database-as-a-Service Plugin

CREATE TABLE IF NOT EXISTS `cloud`.`dbaas_instances` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `uuid` varchar(40) NOT NULL UNIQUE,
    `name` varchar(255) NOT NULL,
    `account_id` bigint unsigned NOT NULL,
    `domain_id` bigint unsigned NOT NULL,
    `db_engine` varchar(32) NOT NULL COMMENT 'MYSQL, POSTGRESQL, MONGODB, SQLSERVER, REDIS, PHPMYADMIN',
    `db_version` varchar(32) DEFAULT NULL,
    `state` varchar(32) NOT NULL DEFAULT 'CREATING',
    `zone_id` bigint unsigned NOT NULL,
    `vm_id` bigint unsigned DEFAULT NULL COMMENT 'CloudStack VM backing this DB instance',
    `service_offering_id` bigint unsigned NOT NULL,
    `template_id` bigint unsigned DEFAULT NULL,
    `network_id` bigint unsigned DEFAULT NULL,
    `ip_address` varchar(45) DEFAULT NULL COMMENT 'Private IP',
    `public_ip_address` varchar(45) DEFAULT NULL COMMENT 'Public IP via static NAT',
    `public_ip_id` bigint unsigned DEFAULT NULL COMMENT 'CloudStack public IP ID for lifecycle management',
    `public_port` int unsigned DEFAULT NULL COMMENT 'Public port for PORT_FORWARD mode',
    `port` int unsigned NOT NULL,
    `admin_username` varchar(128) DEFAULT 'dbadmin',
    `admin_password_encrypted` varchar(512) DEFAULT NULL,
    `storage_size_gb` int unsigned NOT NULL DEFAULT 20,
    `cpu_cores` int unsigned NOT NULL DEFAULT 1,
    `memory_mb` int unsigned NOT NULL DEFAULT 1024,
    `backup_enabled` tinyint(1) NOT NULL DEFAULT 1,
    `backup_schedule` varchar(255) DEFAULT NULL,
    `high_availability` tinyint(1) NOT NULL DEFAULT 0,
    `connection_string` varchar(1024) DEFAULT NULL,
    `created` datetime NOT NULL,
    `removed` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_dbaas_account` (`account_id`),
    INDEX `idx_dbaas_engine` (`db_engine`),
    INDEX `idx_dbaas_state` (`state`),
    INDEX `idx_dbaas_vm` (`vm_id`),
    INDEX `idx_dbaas_zone` (`zone_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `cloud`.`dbaas_backups` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `uuid` varchar(40) NOT NULL UNIQUE,
    `db_instance_id` bigint unsigned NOT NULL,
    `account_id` bigint unsigned NOT NULL,
    `backup_type` varchar(32) NOT NULL DEFAULT 'FULL',
    `status` varchar(32) NOT NULL DEFAULT 'CREATING',
    `size_bytes` bigint unsigned NOT NULL DEFAULT 0,
    `volume_snapshot_id` bigint unsigned DEFAULT NULL,
    `created` datetime NOT NULL,
    `expires` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_dbaas_backup_instance` (`db_instance_id`),
    INDEX `idx_dbaas_backup_account` (`account_id`),
    CONSTRAINT `fk_dbaas_backup_instance` FOREIGN KEY (`db_instance_id`)
        REFERENCES `dbaas_instances` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
