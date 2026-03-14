// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.catalog;

import java.util.List;

import org.apache.cloudstack.api.command.user.catalog.DeployCatalogItemCmd;
import org.apache.cloudstack.api.command.user.catalog.ListCatalogDeploymentStatusCmd;
import org.apache.cloudstack.api.command.user.catalog.ListCatalogItemsCmd;
import org.apache.cloudstack.api.response.CatalogItemResponse;
import org.apache.cloudstack.api.response.CatalogDeploymentResponse;
import org.apache.cloudstack.api.response.CatalogDeploymentStatusResponse;
import org.apache.cloudstack.framework.config.Configurable;

import com.cloud.utils.component.PluggableService;

/**
 * Service Catalog - A marketplace for one-click deployment of pre-configured services.
 *
 * Provides a curated catalog of ready-to-deploy services including:
 * - VPS packages (Web Server, App Server, etc.)
 * - Database packages (MySQL, PostgreSQL, MongoDB, SQL Server)
 * - Application stacks (LAMP, MEAN, WordPress, etc.)
 * - Management tools (phpMyAdmin, Portainer, Grafana, etc.)
 *
 * Each catalog item bundles: VM template + service offering + network config + cloud-init setup
 */
public interface ServiceCatalogService extends PluggableService, Configurable {

    List<CatalogItemResponse> listCatalogItems(ListCatalogItemsCmd cmd);

    CatalogDeploymentResponse deployCatalogItem(DeployCatalogItemCmd cmd);

    CatalogDeploymentStatusResponse listCatalogDeploymentStatus(ListCatalogDeploymentStatusCmd cmd);
}
