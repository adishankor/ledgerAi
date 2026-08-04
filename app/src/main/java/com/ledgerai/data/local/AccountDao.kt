package com.ledgerai.data.local

import androidx.room.*

@Dao
interface AccountDao {
    @Insert
    suspend fun insert(account: AccountEntity)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccount(id: String): AccountEntity?
}
