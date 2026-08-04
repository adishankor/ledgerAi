package com.ledgerai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerai.data.local.LedgerDatabase
import com.ledgerai.data.local.TransactionEntity
import com.ledgerai.domain.DoubleEntryService
import com.ledgerai.service.DraftTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = LedgerDatabase.getInstance(application)
    private val doubleEntry = DoubleEntryService(db.transactionDao())

    private val _draftTransactions = MutableStateFlow<List<DraftTransaction>>(emptyList())
    val draftTransactions = _draftTransactions.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions = _transactions.asStateFlow()

    init {
        viewModelScope.launch {
            db.transactionDao().getAllTransactions().collect { _transactions.value = it }
        }
    }

    fun addDraft(draft: DraftTransaction) {
        _draftTransactions.value = _draftTransactions.value + draft
    }

    fun approveDraft(draft: DraftTransaction) {
        viewModelScope.launch {
            doubleEntry.recordTransaction(
                description = draft.rawText.take(50),
                amount = draft.amount,
                debitAccountId = if (draft.isCredit) "Asset:Bank" else "Expense:Unknown",
                creditAccountId = if (draft.isCredit) "Income:Unknown" else "Asset:Bank"
            )
            _draftTransactions.value = _draftTransactions.value - draft
        }
    }
}
