package com.ledgerai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val currencyCode: String,
    val balance: Double = 0.0
)
