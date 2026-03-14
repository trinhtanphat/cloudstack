// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas.dao;

import java.util.List;
import com.cloud.utils.db.GenericDao;

public interface DatabaseBackupDao extends GenericDao<DatabaseBackupVO, Long> {
    DatabaseBackupVO findByUuid(String uuid);
    List<DatabaseBackupVO> listByInstanceId(long dbInstanceId);
    List<DatabaseBackupVO> listByAccountId(long accountId);
}
