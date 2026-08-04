package com.ledgerai.data.local

import androidx.room.*
import android.content.Context

@Database(entities = [TransactionEntity::class, AccountEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class LedgerDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: LedgerDatabase? = null
        fun getInstance(context: Context): LedgerDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, LedgerDatabase::class.java, "ledgerai.db")
                    .build().also { INSTANCE = it }
            }
        }
    }
}

class Converters {
    @TypeConverter fun fromLong(value: Long?) = value ?: 0L
    @TypeConverter fun toLong(value: Long) = value
}
