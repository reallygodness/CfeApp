package com.example.cfeprjct;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {User.class,
}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UserDAO userDAO();

    // Миграция с версии 3 на 4:
    // - Удаляем старый столбец "id" (INTEGER, primary key)
    // - Создаём новый столбец "userId" (TEXT, non-null, primary key) вместо него.
    // - Не включаем столбец profile_picture (предполагается, что теперь используется profileImage)
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Создаем новую таблицу с нужной схемой:
            database.execSQL("CREATE TABLE IF NOT EXISTS `users_new` (" +
                    "`userId` TEXT NOT NULL, " +
                    "`firstName` TEXT, " +
                    "`lastName` TEXT, " +
                    "`email` TEXT, " +
                    "`phoneNumber` TEXT, " +
                    "`resetCode` TEXT, " +
                    "`password` TEXT, " +
                    "`profileImage` BLOB, " +
                    "PRIMARY KEY(`userId`))");
            // Копируем данные из старой таблицы в новую. Поле id преобразуем в текст для userId.
            // Если в старой базе поле id являлось primary key, данные будут сконвертированы.
            database.execSQL("INSERT INTO `users_new` (userId, firstName, lastName, email, phoneNumber, resetCode, password, profileImage) " +
                    "SELECT CAST(id AS TEXT), firstName, lastName, email, phoneNumber, resetCode, password, profileImage FROM users");
            // Удаляем старую таблицу
            database.execSQL("DROP TABLE users");
            // Переименовываем новую таблицу в users
            database.execSQL("ALTER TABLE users_new RENAME TO users");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                    .addMigrations(MIGRATION_3_4) // Применяем миграцию с версии 3 на 4
                    .build();
        }
        return instance;
    }
}
