package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AccountDao
import com.example.data.dao.CommentDao
import com.example.data.dao.FollowDao
import com.example.data.dao.SessionDao
import com.example.data.dao.UserDao
import com.example.data.dao.VideoDao
import com.example.data.model.AccountEntity
import com.example.data.model.ActiveSessionEntity
import com.example.data.model.CommentEntity
import com.example.data.model.FollowEntity
import com.example.data.model.UserEntity
import com.example.data.model.VideoEntity

@Database(
    entities = [
        UserEntity::class,
        VideoEntity::class,
        CommentEntity::class,
        FollowEntity::class,
        AccountEntity::class,
        ActiveSessionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class PlayzoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun videoDao(): VideoDao
    abstract fun commentDao(): CommentDao
    abstract fun followDao(): FollowDao
    abstract fun accountDao(): AccountDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: PlayzoDatabase? = null

        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE videos ADD COLUMN videoUri TEXT")
            }
        }

        fun getDatabase(context: Context): PlayzoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PlayzoDatabase::class.java,
                    "playzo_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
