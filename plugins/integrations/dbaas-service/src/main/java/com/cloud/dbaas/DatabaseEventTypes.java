// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.
package com.cloud.dbaas;

public final class DatabaseEventTypes {

    public static final String EVENT_DBAAS_INSTANCE_CREATE = "DBAAS.INSTANCE.CREATE";
    public static final String EVENT_DBAAS_INSTANCE_DELETE = "DBAAS.INSTANCE.DELETE";
    public static final String EVENT_DBAAS_INSTANCE_RESTART = "DBAAS.INSTANCE.RESTART";
    public static final String EVENT_DBAAS_INSTANCE_SCALE = "DBAAS.INSTANCE.SCALE";
    public static final String EVENT_DBAAS_BACKUP_CREATE = "DBAAS.BACKUP.CREATE";
    public static final String EVENT_DBAAS_BACKUP_RESTORE = "DBAAS.BACKUP.RESTORE";
    public static final String EVENT_DBAAS_HEALTH_CHECK = "DBAAS.HEALTH.CHECK";

    private DatabaseEventTypes() {
    }
}
