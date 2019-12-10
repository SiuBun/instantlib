package com.baselib.instant.breakpoint.database;

import android.support.annotation.Nullable;


import com.baselib.instant.breakpoint.database.room.TaskRecordEntity;

import java.util.List;

/**
 * 数据库操作定义
 *
 * @author wsb
 */
public interface DatabaseOperate {
    /**
     * 添加任务到本地记录中
     *
     * @param recordEntity 准备保存的任务条目
     */
    void addTaskRecord(TaskRecordEntity recordEntity);

    /**
     * 更新任务记录到本地记录中
     * @param recordEntity 更新条目
     */
    void updateTaskRecord(TaskRecordEntity recordEntity);

    /**
     * 从本地记录中删除任务条目
     *
     * @param recordEntity 删除条目
     */
    void deleteTaskRecord(TaskRecordEntity recordEntity);

    /**
     * 清除所有本地记录的任务条目
     */
    void cleanTaskRecord();

    /**
     * 获取本地记录的所有任务
     *
     * @return 返回任务条目列表
     */
    @Nullable List<TaskRecordEntity> loadAllTaskRecord();

    /**
     * 根据id获取对应的任务记录
     *
     * @param id 查找id
     * @return 从数据库中查找到的任务条目
     */
    TaskRecordEntity obtainTaskRecordById(String id);
}
