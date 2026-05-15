package com.noowar.smsforwarder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.noowar.smsforwarder.data.AppDatabase
import com.noowar.smsforwarder.data.ForwardRule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RuleViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getInstance(app).ruleDao()

    val rules: StateFlow<List<ForwardRule>> = dao.getAll().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addRule(rule: ForwardRule) = viewModelScope.launch { dao.insert(rule) }

    fun updateRule(rule: ForwardRule) = viewModelScope.launch { dao.update(rule) }

    fun deleteRule(rule: ForwardRule) = viewModelScope.launch { dao.delete(rule) }

    fun toggleEnabled(rule: ForwardRule) = viewModelScope.launch {
        dao.update(rule.copy(isEnabled = !rule.isEnabled))
    }
}
