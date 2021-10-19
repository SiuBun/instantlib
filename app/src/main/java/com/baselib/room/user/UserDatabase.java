package com.baselib.room.user;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {UserEntity.class}, version = 1,exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    public abstract UserDao getUserDao();

    private static volatile UserDatabase sUserDatabase;

    public static UserDatabase getInstance(final Context context) {
        if (null == sUserDatabase) {
            synchronized (UserDatabase.class) {
                if (null == sUserDatabase) {
                    sUserDatabase = Room
                        .databaseBuilder(context.getApplicationContext(),UserDatabase.class,"user_database")
//                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return sUserDatabase;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // If you want to keep the data through app restarts,
            // comment out the following line.
            new PopulateDbAsync(sUserDatabase).execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final UserDao mDao;

        PopulateDbAsync(UserDatabase db) {
            mDao = db.getUserDao();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            // Start the app with a clean database every time.
            // Not needed if you only populate on creation.
            mDao.deleteUser();

            UserEntity word = new UserEntity();
            word.setUserId("001");
            word.setUserName("hello");
            word.setPassword("123");
            mDao.inserUser(word);

            UserEntity user = new UserEntity();
            user.setUserId("002");
            user.setUserName("world");
            user.setPassword("123");
            mDao.inserUser(user);
            return null;
        }
    }
}
