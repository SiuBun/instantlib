package com.baselib.instant.breakpoint.database.room;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.Nullable;

import com.baselib.instant.bpdownload.database.DatabaseOperate;
import com.baselib.instant.bpdownload.utils.DownloadConst;

import java.util.List;

/**
 * room数据库方案实现数据库操作
 *
 * @author wsb
 */
public class RoomStrategy implements DatabaseOperate {

    private final TaskDao mTaskDao;

    public RoomStrategy(Context context) {
        TaskDatabase taskDatabase = Room.databaseBuilder(context.getApplicationContext(), TaskDatabase.class, DownloadConst.DB_NAME).build();
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
    public TaskRecordEntity obtainTaskRecordById(String id) {
        return mTaskDao.obtainTaskRecordById(id);
    }
}
