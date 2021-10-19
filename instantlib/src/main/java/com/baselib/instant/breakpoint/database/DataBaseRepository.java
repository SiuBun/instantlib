package com.baselib.instant.breakpoint.database;

import android.content.Context;

import com.baselib.instant.breakpoint.database.room.RoomStrategy;
import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;
import com.baselib.instant.breakpoint.utils.BreakPointConst;
import com.baselib.instant.util.LogUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;

/**
 * 数据库策略管理类
 * <p>
 * 所有数据库操作交由{@link #mDatabaseStrategy}处理
 *
 * @author wsb
 */
public class DataBaseRepository implements DatabaseOperate {
    private DatabaseOperate mDatabaseStrategy;
    private long mUptimeMillis;

    public DataBaseRepository(Context context) {
        mUptimeMillis = System.currentTimeMillis();
        mDatabaseStrategy = new RoomStrategy(context);
    }

    @Override
    public void addTaskRecord(TaskRecordEntity recordEntity) {
        final int id = recordEntity.getId();
        if (null == mDatabaseStrategy.obtainTaskRecordById(id)) {
            LogUtils.i("本地不存在该id" + id + ",插入新条目");
            mDatabaseStrategy.addTaskRecord(recordEntity);
        } else {
            LogUtils.i("本地已存在id条目" + id + ",不重复插入");
        }
    }

    @Override
    public void updateTaskRecord(TaskRecordEntity recordEntity) {
        LogUtils.i("更新任务内容到" + recordEntity.getId() + "任务的记录里" + recordEntity.getCurrentSize());
        mDatabaseStrategy.updateTaskRecord(recordEntity);
    }

    @Override
    public void updateTaskRecord(int taskId, String currentSize) {
        final long millis = System.currentTimeMillis();
        if (millis - mUptimeMillis >= TimeUnit.SECONDS.toMillis(BreakPointConst.DATABASE_UPDATE_INTERVAL)) {
//            LogUtils.i("更新当前进度到"+taskId+"任务的记录里"+currentSize);
            mUptimeMillis = millis;
            mDatabaseStrategy.updateTaskRecord(taskId, currentSize);
        }
    }

    @Override
    public void deleteTaskRecord(TaskRecordEntity recordEntity) {
        mDatabaseStrategy.deleteTaskRecord(recordEntity);
    }

    @Override
    public void cleanTaskRecord() {
        mDatabaseStrategy.cleanTaskRecord();
    }

    @Nullable
    @Override
    public List<TaskRecordEntity> loadAllTaskRecord() {
        LogUtils.d("加载当前本地未完成的缓存任务");
        return mDatabaseStrategy.loadAllTaskRecord();
    }

    @Override
    @Nullable
    public TaskRecordEntity obtainTaskRecordById(int id) {
        return mDatabaseStrategy.obtainTaskRecordById(id);
    }
}
