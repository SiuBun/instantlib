package com.baselib.instant.breakpoint.database.room;

import android.content.Context;

import com.baselib.instant.breakpoint.database.DatabaseOperate;
import com.baselib.instant.breakpoint.utils.BreakPointConst;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.room.Room;

/**
 * room数据库方案实现数据库操作
 *
 * @author wsb
 */
public class RoomStrategy implements DatabaseOperate {

    private final TaskDao mTaskDao;

    public RoomStrategy(Context context) {
        TaskDatabase taskDatabase = Room
            .databaseBuilder(context.getApplicationContext(), TaskDatabase.class, BreakPointConst.DB_NAME).build();
        mTaskDao = taskDatabase.getTaskDao();
    }

    @Override
    public void addTaskRecord(TaskRecordEntity recordEntity) {
        mTaskDao.addTaskRecord(recordEntity);
    }

    @Override
    public void updateTaskRecord(TaskRecordEntity recordEntity) {
        mTaskDao.updateTaskRecord(recordEntity);
    }

    @Override
    public void updateTaskRecord(int taskId, String currentSize) {
        mTaskDao.updateTaskRecord(taskId, currentSize);
    }

    @Override
    public void deleteTaskRecord(TaskRecordEntity recordEntity) {
        mTaskDao.deleteTaskRecord(recordEntity);
    }

    @Override
    public void cleanTaskRecord() {
        mTaskDao.cleanTaskRecord();
    }

    @Override
    @Nullable
    public List<TaskRecordEntity> loadAllTaskRecord() {
        return mTaskDao.loadAllTaskRecord();
    }

    @Override
    public TaskRecordEntity obtainTaskRecordById(int id) {
        return mTaskDao.obtainTaskRecordById(id);
    }
}
