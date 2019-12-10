package com.baselib.instant.breakpoint.database;

import android.content.Context;
import android.support.annotation.Nullable;

import com.baselib.instant.breakpoint.database.room.RoomStrategy;
import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.util.LogUtils;

import java.util.List;

/**
 * 数据库策略管理类
 * <p>
 * 所有数据库操作交由{@link #mDatabase}处理
 *
 * @author wsb
 */
public class DataBaseRepository implements DatabaseOperate {
    private DatabaseOperate mDatabase;

    public DataBaseRepository(Context context) {
        mDatabase = new RoomStrategy(context);
    }

    @Override
    public void addTaskRecord(TaskRecordEntity recordEntity) {
        mDatabase.addTaskRecord(recordEntity);
    }

    @Override
    public void updateTaskRecord(TaskRecordEntity recordEntity) {
        mDatabase.updateTaskRecord(recordEntity);
    }

    @Override
    public void deleteTaskRecord(TaskRecordEntity recordEntity) {
        mDatabase.deleteTaskRecord(recordEntity);
    }

    @Override
    public void cleanTaskRecord() {
        mDatabase.cleanTaskRecord();
    }

    @Nullable
    @Override
    public List<TaskRecordEntity> loadAllTaskRecord() {
        LogUtils.d("加载当前本地未完成的缓存任务");
        return mDatabase.loadAllTaskRecord();
    }

    @Override
    public TaskRecordEntity obtainTaskRecordById(String id) {
        return mDatabase.obtainTaskRecordById(id);
    }
}
