package com.baselib.room;

import android.content.Context;

import com.baselib.room.user.UserDao;
import com.baselib.room.user.UserDatabase;
import com.baselib.room.user.UserEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

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
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
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
        assertEquals(entityList.get(0).getUserId(), userEntity.getUserId());
    }

    @Test
    public void deleteUser() throws InterruptedException {
        mUserDao.deleteUser();
        List<UserEntity> value = LiveDataTestUtil.getValue(mUserDao.getAllUser());
        assertEquals(value.size(), 0);
    }

    @Test
    public void getAllUser() throws InterruptedException {
        mUserDao.inserUser(userEntity);

        List<UserEntity> entityList = LiveDataTestUtil.getValue(mUserDao.getAllUser());
        assertEquals(entityList.get(0).getUserId(), "testId");
    }
}