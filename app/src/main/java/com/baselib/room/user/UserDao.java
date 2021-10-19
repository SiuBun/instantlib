package com.baselib.room.user;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
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
