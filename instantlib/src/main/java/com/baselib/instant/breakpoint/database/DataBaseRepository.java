package com.baselib.instant.breakpoint.database;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.baselib.instant.breakpoint.database.room.RoomStrategy;
import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.util.LogUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 数据库策略管理类
 * <p>
 * 所有数据库操作交由{@link #mDatabase}处理
 *
 * @author wsb
 */
public class DataBaseRepository implements DatabaseOperate {
    private DatabaseOperate mDatabase;
    private long mUptimeMillis;

    public DataBaseRepository(Context context) {
        mUptimeMillis = System.currentTimeMillis();
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
    public void updateTaskRecord(int taskId, String currentSize) {
        final long millis = System.currentTimeMillis();
        if (millis - mUptimeMillis >= TimeUnit.SECONDS.toMillis(BreakPointConst.DATABASE_UPDATE_INTERVAL)) {
            LogUtils.i("更新当前进度到"+taskId+"任务的记录里"+currentSize);
            mUptimeMillis = millis;
            mDatabase.updateTaskRecord(taskId, currentSize);
        }
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
