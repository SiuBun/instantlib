package com.baselib.instant.breakpoint.database.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * room数据库dao层对象
 *
 * @author wsb
 */
@Dao
public interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addTaskRecord(TaskRecordEntity entity);

    @Query("SELECT * from download_task_record_table")
    List<TaskRecordEntity> loadAllTaskRecord();

    @Query("SELECT * from download_task_record_table WHERE task_id= :id")
    TaskRecordEntity obtainTaskRecordById(String id);

    @Delete
    void deleteTaskRecord(TaskRecordEntity recordEntity);

    @Update
    void updateTaskRecord(TaskRecordEntity recordEntity);

    @Query("UPDATE download_task_record_table SET current_size= :currentSize WHERE task_id=:taskId")
    void updateTaskRecord(int taskId, String currentSize);

    @Query("DELETE FROM download_task_record_table")
    void cleanTaskRecord();
}
