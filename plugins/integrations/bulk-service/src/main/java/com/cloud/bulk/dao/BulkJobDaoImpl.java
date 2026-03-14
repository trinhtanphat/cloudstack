// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.bulk.dao;

import java.util.List;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

public class BulkJobDaoImpl extends GenericDaoBase<BulkJobVO, Long> implements BulkJobDao {

    private final SearchBuilder<BulkJobVO> uuidSearch;
    private final SearchBuilder<BulkJobVO> accountSearch;
    private final SearchBuilder<BulkJobVO> statusSearch;
    private final SearchBuilder<BulkJobVO> accountTypeSearch;

    public BulkJobDaoImpl() {
        uuidSearch = createSearchBuilder();
        uuidSearch.and("uuid", uuidSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        uuidSearch.done();

        accountSearch = createSearchBuilder();
        accountSearch.and("accountId", accountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        accountSearch.done();

        statusSearch = createSearchBuilder();
        statusSearch.and("status", statusSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        statusSearch.done();

        accountTypeSearch = createSearchBuilder();
        accountTypeSearch.and("accountId", accountTypeSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        accountTypeSearch.and("jobType", accountTypeSearch.entity().getJobType(), SearchCriteria.Op.EQ);
        accountTypeSearch.done();
    }

    @Override
    public BulkJobVO findByUuid(String uuid) {
        SearchCriteria<BulkJobVO> sc = uuidSearch.create();
        sc.setParameters("uuid", uuid);
        return findOneBy(sc);
    }

    @Override
    public List<BulkJobVO> listByAccountId(long accountId) {
        SearchCriteria<BulkJobVO> sc = accountSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public List<BulkJobVO> listByStatus(BulkJobVO.Status status) {
        SearchCriteria<BulkJobVO> sc = statusSearch.create();
        sc.setParameters("status", status);
        return listBy(sc);
    }

    @Override
    public List<BulkJobVO> listByAccountAndType(long accountId, BulkJobVO.JobType jobType) {
        SearchCriteria<BulkJobVO> sc = accountTypeSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("jobType", jobType);
        return listBy(sc);
    }
}
