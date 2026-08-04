package com.ledgerai.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class DraftTransaction(
    val amount: Double,
    val currency: String,
    val rawText: String,
    val isCredit: Boolean
)

object LocalDraftManager {
    private val _drafts = MutableSharedFlow<DraftTransaction>()
    val drafts = _drafts.asSharedFlow()
    fun postDraft(draft: DraftTransaction) { _drafts.tryEmit(draft) }
}

class NotificationParserService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val text = extras.getString(Notification.EXTRA_TEXT) ?: return
        val debitPattern = Regex("(debited|withdrawn|paid|spent).*?(\\d+\\.?\\d*)\\s*(\\w{3})", RegexOption.IGNORE_CASE)
        val creditPattern = Regex("(credited|received|deposited).*?(\\d+\\.?\\d*)\\s*(\\w{3})", RegexOption.IGNORE_CASE)
        val match = debitPattern.find(text) ?: creditPattern.find(text) ?: return
        val amount = match.groupValues[2].toDoubleOrNull() ?: return
        val currency = match.groupValues[3]
        val isCredit = creditPattern.containsMatchIn(text)
        LocalDraftManager.postDraft(DraftTransaction(amount, currency, text, isCredit))
    }
}
