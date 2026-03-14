// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.gpu.dao;

import java.util.List;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

public class GpuServiceInstanceDaoImpl extends GenericDaoBase<GpuServiceInstanceVO, Long> implements GpuServiceInstanceDao {

    private final SearchBuilder<GpuServiceInstanceVO> uuidSearch;
    private final SearchBuilder<GpuServiceInstanceVO> accountSearch;

    public GpuServiceInstanceDaoImpl() {
        uuidSearch = createSearchBuilder();
        uuidSearch.and("uuid", uuidSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        uuidSearch.done();

        accountSearch = createSearchBuilder();
        accountSearch.and("accountId", accountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        accountSearch.and("removed", accountSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        accountSearch.done();
    }

    @Override
    public GpuServiceInstanceVO findByUuid(String uuid) {
        SearchCriteria<GpuServiceInstanceVO> sc = uuidSearch.create();
        sc.setParameters("uuid", uuid);
        return findOneBy(sc);
    }

    @Override
    public List<GpuServiceInstanceVO> listByAccountId(long accountId) {
        SearchCriteria<GpuServiceInstanceVO> sc = accountSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }
}
