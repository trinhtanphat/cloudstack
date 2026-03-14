// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas.dao;

import java.util.List;
import com.cloud.utils.db.GenericDao;

public interface DatabaseInstanceDao extends GenericDao<DatabaseInstanceVO, Long> {
    DatabaseInstanceVO findByUuid(String uuid);
    List<DatabaseInstanceVO> listByAccountId(long accountId);
    List<DatabaseInstanceVO> listByAccountAndEngine(long accountId, DatabaseInstanceVO.DbEngine engine);
    List<DatabaseInstanceVO> listByZoneId(long zoneId);
    List<DatabaseInstanceVO> listByState(DatabaseInstanceVO.State state);
    DatabaseInstanceVO findByVmId(long vmId);
}
