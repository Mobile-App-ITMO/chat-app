package io.ktor.chat.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface StorageDelegate {
    fun saveString(key: String, value: String)
    fun getString(key: String): String?
    fun saveBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
}

class LocalStorage(private val delegate: StorageDelegate? = null) {
    companion object {
        private const val KEY_EMAIL = "last_email"
        private const val KEY_PASSWORD = "last_password"
        private const val KEY_REMEMBER = "remember_password"

        // 单例实例
        private var instance: LocalStorage? = null

        fun getInstance(delegate: StorageDelegate? = null): LocalStorage {
            if (instance == null) {
                instance = LocalStorage(delegate)
            } else if (delegate != null) {
                // 如果提供了新的delegate，更新它
                instance = LocalStorage(delegate)
            }
            return instance!!
        }

        fun destroy() {
            instance = null
        }
    }

    // 状态管理
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _remember = MutableStateFlow(false)
    val remember: StateFlow<Boolean> = _remember.asStateFlow()

    init {
        loadFromStorage()
    }

    private fun loadFromStorage() {
        if (delegate == null) {
            return
        }

        _email.value = delegate.getString(KEY_EMAIL) ?: ""
        _password.value = delegate.getString(KEY_PASSWORD) ?: ""
        _remember.value = delegate.getBoolean(KEY_REMEMBER, false)
    }

    fun saveLogin(email: String, password: String? = null, remember: Boolean = false) {
        _email.value = email
        _remember.value = remember

        val pwdToSave = if (remember && !password.isNullOrBlank()) {
            password
        } else {
            ""
        }
        _password.value = pwdToSave

        delegate?.apply {
            saveString(KEY_EMAIL, email)
            saveString(KEY_PASSWORD, pwdToSave)
            saveBoolean(KEY_REMEMBER, remember)
        }
    }

    fun updateEmail(email: String) {
        _email.value = email
        delegate?.saveString(KEY_EMAIL, email)
    }

    fun getSavedPassword(): String? {
        return if (_remember.value && _password.value.isNotBlank()) {
            _password.value
        } else {
            null
        }
    }

    fun hasSavedLogin(): Boolean {
        return _email.value.isNotBlank()
    }

    fun clearPassword() {
        _password.value = ""
        _remember.value = false
        delegate?.saveString(KEY_PASSWORD, "")
        delegate?.saveBoolean(KEY_REMEMBER, false)
    }

    fun clearAll() {
        _email.value = ""
        _password.value = ""
        _remember.value = false

        delegate?.apply {
            saveString(KEY_EMAIL, "")
            saveString(KEY_PASSWORD, "")
            saveBoolean(KEY_REMEMBER, false)
        }
    }
}