package com.baselib.instant.breakpoint.database.room;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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
    TaskRecordEntity obtainTaskRecordById(int id);

    @Delete
    void deleteTaskRecord(TaskRecordEntity recordEntity);

    @Update
    void updateTaskRecord(TaskRecordEntity recordEntity);

    @Query("UPDATE download_task_record_table SET current_size= :currentSize WHERE task_id=:taskId")
    void updateTaskRecord(int taskId, String currentSize);

    @Query("DELETE FROM download_task_record_table")
    void cleanTaskRecord();
}
