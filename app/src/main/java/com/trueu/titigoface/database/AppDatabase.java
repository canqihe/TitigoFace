package com.trueu.titigoface.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Colin
 * on 2020/8/17
 * E-mail: hecanqi168@gmail.com
 */
@Database(entities = {UserEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase sInstance;

    public static AppDatabase getDatabase(Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context, AppDatabase.class,
                            "db.face_library.db").build();
                }
            }
        }
        return sInstance;
    }
    //注意这里的UserDaoExtend对象是编译后生成的接口对象.
    public abstract UserDaoExtend userDao();
}
