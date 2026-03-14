// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas.dao;

import java.util.List;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

public class DatabaseBackupDaoImpl extends GenericDaoBase<DatabaseBackupVO, Long> implements DatabaseBackupDao {

    private final SearchBuilder<DatabaseBackupVO> uuidSearch;
    private final SearchBuilder<DatabaseBackupVO> instanceSearch;
    private final SearchBuilder<DatabaseBackupVO> accountSearch;

    public DatabaseBackupDaoImpl() {
        uuidSearch = createSearchBuilder();
        uuidSearch.and("uuid", uuidSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        uuidSearch.done();

        instanceSearch = createSearchBuilder();
        instanceSearch.and("dbInstanceId", instanceSearch.entity().getDbInstanceId(), SearchCriteria.Op.EQ);
        instanceSearch.done();

        accountSearch = createSearchBuilder();
        accountSearch.and("accountId", accountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        accountSearch.done();
    }

    @Override
    public DatabaseBackupVO findByUuid(String uuid) {
        SearchCriteria<DatabaseBackupVO> sc = uuidSearch.create();
        sc.setParameters("uuid", uuid);
        return findOneBy(sc);
    }

    @Override
    public List<DatabaseBackupVO> listByInstanceId(long dbInstanceId) {
        SearchCriteria<DatabaseBackupVO> sc = instanceSearch.create();
        sc.setParameters("dbInstanceId", dbInstanceId);
        return listBy(sc);
    }

    @Override
    public List<DatabaseBackupVO> listByAccountId(long accountId) {
        SearchCriteria<DatabaseBackupVO> sc = accountSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }
}
