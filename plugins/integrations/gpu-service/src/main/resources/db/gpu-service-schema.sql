-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.
-- Schema for GPU Service Plugin

CREATE TABLE IF NOT EXISTS `cloud`.`gpu_service_instances` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `uuid` varchar(40) NOT NULL UNIQUE,
    `name` varchar(255) NOT NULL,
    `account_id` bigint unsigned NOT NULL,
    `domain_id` bigint unsigned NOT NULL,
    `zone_id` bigint unsigned NOT NULL,
    `vm_id` bigint unsigned DEFAULT NULL,
    `service_offering_id` bigint unsigned NOT NULL,
    `template_id` bigint unsigned NOT NULL,
    `network_id` bigint unsigned DEFAULT NULL,
    `gpu_profile_id` varchar(128) NOT NULL,
    `provider` varchar(32) NOT NULL DEFAULT 'GCP',
    `gpu_count` int unsigned NOT NULL DEFAULT 1,
    `state` varchar(32) NOT NULL DEFAULT 'RUNNING',
    `created` datetime NOT NULL,
    `removed` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_gpu_account` (`account_id`),
    INDEX `idx_gpu_zone` (`zone_id`),
    INDEX `idx_gpu_vm` (`vm_id`),
    INDEX `idx_gpu_state` (`state`),
    INDEX `idx_gpu_profile` (`gpu_profile_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
