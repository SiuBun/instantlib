package com.baselib.room;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.baselib.room.user.UserDao;
import com.baselib.room.user.UserDatabase;
import com.baselib.room.user.UserEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class UserDaoTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    private UserDatabase mRoomDatabase;
    private UserDao mUserDao;
    private UserEntity userEntity;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mRoomDatabase = Room.inMemoryDatabaseBuilder(context, UserDatabase.class).allowMainThreadQueries().build();

        mUserDao = mRoomDatabase.getUserDao();

        userEntity = new UserEntity();
        userEntity.setUserId("testId");
        userEntity.setUserName("testName");
        userEntity.setPassword("testPsw");
        userEntity.setUpdateTime(1000L);
    }

    @After
    public void closeDb() {
        mRoomDatabase.close();
    }

    @Test
    public void inserUser() throws InterruptedException {


        mUserDao.inserUser(userEntity);
        List<UserEntity> entityList = LiveDataTestUtil.getValue(mUserDao.getAllUser());
        assertEquals(entityList.get(0).getUserId(),userEntity.getUserId());
    }

    @Test
    public void deleteUser() {
    }

    @Test
    public void getAllUser() throws InterruptedException {
        mUserDao.inserUser(userEntity);

        List<UserEntity> entityList = LiveDataTestUtil.getValue(mUserDao.getAllUser());
        assertEquals(entityList.get(0).getUserId(), "testId");
    }
}