-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.
-- Schema for Bulk Provisioning Service

CREATE TABLE IF NOT EXISTS `cloud`.`bulk_provisioning_jobs` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `uuid` varchar(40) NOT NULL UNIQUE,
    `account_id` bigint unsigned NOT NULL,
    `domain_id` bigint unsigned NOT NULL,
    `job_type` varchar(32) NOT NULL COMMENT 'DEPLOY_VMS, ALLOCATE_IPS',
    `status` varchar(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, RUNNING, COMPLETED, FAILED, PARTIAL',
    `total_count` int unsigned NOT NULL DEFAULT 0,
    `completed_count` int unsigned NOT NULL DEFAULT 0,
    `failed_count` int unsigned NOT NULL DEFAULT 0,
    `zone_id` bigint unsigned DEFAULT NULL,
    `service_offering_id` bigint unsigned DEFAULT NULL,
    `template_id` bigint unsigned DEFAULT NULL,
    `network_id` bigint unsigned DEFAULT NULL,
    `name_prefix` varchar(255) DEFAULT NULL,
    `assign_public_ip` tinyint(1) NOT NULL DEFAULT 0,
    `batch_size` int unsigned NOT NULL DEFAULT 50,
    `parameters` text DEFAULT NULL,
    `result_data` mediumtext DEFAULT NULL,
    `error_data` mediumtext DEFAULT NULL,
    `created` datetime NOT NULL,
    `completed` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_bulk_jobs_account` (`account_id`),
    INDEX `idx_bulk_jobs_status` (`status`),
    INDEX `idx_bulk_jobs_uuid` (`uuid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
