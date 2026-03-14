// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.bulk.dao;

import java.util.List;

import com.cloud.utils.db.GenericDao;

public interface BulkJobDao extends GenericDao<BulkJobVO, Long> {

    BulkJobVO findByUuid(String uuid);

    List<BulkJobVO> listByAccountId(long accountId);

    List<BulkJobVO> listByStatus(BulkJobVO.Status status);

    List<BulkJobVO> listByAccountAndType(long accountId, BulkJobVO.JobType jobType);
}
