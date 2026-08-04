package com.ledgerai.domain

import com.ledgerai.data.local.TransactionDao
import com.ledgerai.data.local.TransactionEntity

class DoubleEntryService(private val transactionDao: TransactionDao) {
    suspend fun recordTransaction(
        description: String,
        amount: Double,
        debitAccountId: String,
        creditAccountId: String
    ): Result<TransactionEntity> {
        val hash = generateHash(amount, description, System.currentTimeMillis())
        val existing = transactionDao.getByHash(hash)
        if (existing != null) return Result.failure(Exception("Duplicate Transaction"))
        val transaction = TransactionEntity(
            description = description,
            amount = amount,
            debitAccountId = debitAccountId,
            creditAccountId = creditAccountId,
            hash = hash,
            currencyCode = "USD"
        )
        transactionDao.insert(transaction)
        return Result.success(transaction)
    }

    private fun generateHash(amount: Double, description: String, timestamp: Long): String {
        return "$amount$description$timestamp".hashCode().toString()
    }
}
