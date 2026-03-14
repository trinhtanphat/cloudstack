// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.gpu.dao;

import java.util.List;

import com.cloud.utils.db.GenericDao;

public interface GpuServiceInstanceDao extends GenericDao<GpuServiceInstanceVO, Long> {
    GpuServiceInstanceVO findByUuid(String uuid);

    List<GpuServiceInstanceVO> listByAccountId(long accountId);
}
