package com.baselib.instant.breakpoint.database.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * room数据库对数据库的操作对象
 *
 * @author wsb
 */
@Database(entities = {TaskRecordEntity.class}, version = 1, exportSchema = false)
public abstract class TaskDatabase extends RoomDatabase {
    public abstract TaskDao getTaskDao();
}
