package com.baselib.room.user;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void inserUser(UserEntity entity);

    @Query("DELETE FROM user_table")
    void deleteUser();

    @Query("SELECT * from user_table ORDER BY update_time DESC")
    LiveData<List<UserEntity>> getAllUser();

    @Query("SELECT * from user_table ORDER BY update_time DESC LIMIT 1")
    Flowable<UserEntity> getLastUser();
}
