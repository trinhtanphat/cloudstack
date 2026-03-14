// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas.dao;

import java.util.List;

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

public class DatabaseInstanceDaoImpl extends GenericDaoBase<DatabaseInstanceVO, Long> implements DatabaseInstanceDao {

    private final SearchBuilder<DatabaseInstanceVO> uuidSearch;
    private final SearchBuilder<DatabaseInstanceVO> accountSearch;
    private final SearchBuilder<DatabaseInstanceVO> accountEngineSearch;
    private final SearchBuilder<DatabaseInstanceVO> zoneSearch;
    private final SearchBuilder<DatabaseInstanceVO> stateSearch;
    private final SearchBuilder<DatabaseInstanceVO> vmSearch;

    public DatabaseInstanceDaoImpl() {
        uuidSearch = createSearchBuilder();
        uuidSearch.and("uuid", uuidSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        uuidSearch.done();

        accountSearch = createSearchBuilder();
        accountSearch.and("accountId", accountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        accountSearch.and("removed", accountSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        accountSearch.done();

        accountEngineSearch = createSearchBuilder();
        accountEngineSearch.and("accountId", accountEngineSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        accountEngineSearch.and("dbEngine", accountEngineSearch.entity().getDbEngine(), SearchCriteria.Op.EQ);
        accountEngineSearch.and("removed", accountEngineSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        accountEngineSearch.done();

        zoneSearch = createSearchBuilder();
        zoneSearch.and("zoneId", zoneSearch.entity().getZoneId(), SearchCriteria.Op.EQ);
        zoneSearch.and("removed", zoneSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        zoneSearch.done();

        stateSearch = createSearchBuilder();
        stateSearch.and("state", stateSearch.entity().getState(), SearchCriteria.Op.EQ);
        stateSearch.and("removed", stateSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        stateSearch.done();

        vmSearch = createSearchBuilder();
        vmSearch.and("vmId", vmSearch.entity().getVmId(), SearchCriteria.Op.EQ);
        vmSearch.done();
    }

    @Override
    public DatabaseInstanceVO findByUuid(String uuid) {
        SearchCriteria<DatabaseInstanceVO> sc = uuidSearch.create();
        sc.setParameters("uuid", uuid);
        return findOneBy(sc);
    }

    @Override
    public List<DatabaseInstanceVO> listByAccountId(long accountId) {
        SearchCriteria<DatabaseInstanceVO> sc = accountSearch.create();
        sc.setParameters("accountId", accountId);
        return listBy(sc);
    }

    @Override
    public List<DatabaseInstanceVO> listByAccountAndEngine(long accountId, DatabaseInstanceVO.DbEngine engine) {
        SearchCriteria<DatabaseInstanceVO> sc = accountEngineSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("dbEngine", engine);
        return listBy(sc);
    }

    @Override
    public List<DatabaseInstanceVO> listByZoneId(long zoneId) {
        SearchCriteria<DatabaseInstanceVO> sc = zoneSearch.create();
        sc.setParameters("zoneId", zoneId);
        return listBy(sc);
    }

    @Override
    public List<DatabaseInstanceVO> listByState(DatabaseInstanceVO.State state) {
        SearchCriteria<DatabaseInstanceVO> sc = stateSearch.create();
        sc.setParameters("state", state);
        return listBy(sc);
    }

    @Override
    public DatabaseInstanceVO findByVmId(long vmId) {
        SearchCriteria<DatabaseInstanceVO> sc = vmSearch.create();
        sc.setParameters("vmId", vmId);
        return findOneBy(sc);
    }
}
