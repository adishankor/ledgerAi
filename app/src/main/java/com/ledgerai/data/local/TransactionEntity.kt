package com.ledgerai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val description: String,
    val amount: Double,
    val currencyCode: String,
    val date: Long = System.currentTimeMillis(),
    val category: String = "Uncategorized",
    val debitAccountId: String,
    val creditAccountId: String,
    val hash: String,
    val isAutoDetected: Boolean = false,
    val parentTransactionId: String? = null
)
