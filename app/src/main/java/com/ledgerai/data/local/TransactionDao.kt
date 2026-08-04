package com.ledgerai.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE hash = :hash")
    suspend fun getByHash(hash: String): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
}
