// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package org.apache.cloudstack.api.command.user.catalog;

import java.util.List;
import javax.inject.Inject;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.CatalogItemResponse;
import org.apache.cloudstack.api.response.ListResponse;

import com.cloud.catalog.ServiceCatalogService;

@APICommand(name = "listCatalogItems",
            description = "List available service catalog items (databases, application stacks, tools)",
            responseObject = CatalogItemResponse.class,
            responseView = ResponseView.Restricted,
            authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User})
public class ListCatalogItemsCmd extends BaseListCmd {

    @Inject
    public ServiceCatalogService catalogService;

    @Parameter(name = "category",
               type = CommandType.STRING,
               description = "Filter by category: DATABASE, WEBSERVER, APPSTACK, CACHE, MONITORING, MANAGEMENT")
    private String category;

    @Parameter(name = "keyword",
               type = CommandType.STRING,
               description = "Search by name or description keyword")
    private String keyword;

    public String getCategory() { return category; }
    public String getKeyword() { return keyword; }

    @Override
    public void execute() throws ServerApiException {
        List<CatalogItemResponse> responses = catalogService.listCatalogItems(this);
        ListResponse<CatalogItemResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responses, responses.size());
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
