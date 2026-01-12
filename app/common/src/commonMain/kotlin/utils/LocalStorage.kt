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
        private const val KEY_USER_MEMOS = "user_memos"

        private var instance: LocalStorage? = null

        fun getInstance(delegate: StorageDelegate? = null): LocalStorage {
            if (instance == null) {
                instance = LocalStorage(delegate)
            } else if (delegate != null) {
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

    private val _userMemo = MutableStateFlow("")
    val userMemo: StateFlow<String> = _userMemo.asStateFlow()

    private val _allUserMemos = mutableMapOf<String, String>()

    init {
        loadFromStorage()
    }

    fun saveUserMemo(memo: String) {
        val currentEmail = _email.value
        if (currentEmail.isNotBlank()) {
            _userMemo.value = memo
            _allUserMemos[currentEmail] = memo
            saveAllMemosToStorage()
        }
    }

    fun getUserMemoForEmail(email: String): String {
        return _allUserMemos[email] ?: ""
    }

    fun getAllUserMemos(): Map<String, String> {
        return _allUserMemos.toMap()
    }

    private fun saveAllMemosToStorage() {
        if (delegate == null) return

        val memoString = _allUserMemos.entries.joinToString(";") { (email, memo) ->
            "${escapeSpecialChars(email)}:${escapeSpecialChars(memo)}"
        }
        delegate.saveString(KEY_USER_MEMOS, memoString)
    }

    private fun loadAllMemosFromStorage() {
        if (delegate == null) return

        _allUserMemos.clear()
        val savedContent = delegate.getString(KEY_USER_MEMOS) ?: ""

        if (savedContent.isNotBlank()) {
            val entries = savedContent.split(";")
            for (entry in entries) {
                if (entry.isNotBlank() && entry.contains(":")) {
                    val split = entry.split(":", limit = 2)
                    if (split.size == 2) {
                        val email = unescapeSpecialChars(split[0])
                        val memo = unescapeSpecialChars(split[1])
                        _allUserMemos[email] = memo
                    }
                }
            }
        }
    }

    private fun escapeSpecialChars(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace(":", "\\:")
            .replace(";", "\\;")
    }

    private fun unescapeSpecialChars(str: String): String {
        val result = StringBuilder()
        var i = 0
        while (i < str.length) {
            if (str[i] == '\\' && i + 1 < str.length) {
                when (str[i + 1]) {
                    '\\' -> result.append('\\')
                    ':' -> result.append(':')
                    ';' -> result.append(';')
                    else -> result.append(str[i])
                }
                i += 2
            } else {
                result.append(str[i])
                i++
            }
        }
        return result.toString()
    }

    private fun loadFromStorage() {
        if (delegate == null) {
            return
        }

        _email.value = delegate.getString(KEY_EMAIL) ?: ""
        _password.value = delegate.getString(KEY_PASSWORD) ?: ""
        _remember.value = delegate.getBoolean(KEY_REMEMBER, false)

        loadAllMemosFromStorage()

        val currentEmail = _email.value
        if (currentEmail.isNotBlank()) {
            _userMemo.value = _allUserMemos[currentEmail] ?: ""
        } else {
            _userMemo.value = ""
        }
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

        _userMemo.value = _allUserMemos[email] ?: ""
    }

    fun updateEmail(email: String) {
        _email.value = email
        delegate?.saveString(KEY_EMAIL, email)
        _userMemo.value = _allUserMemos[email] ?: ""
    }

    fun getEmail(): String {
        return _email.value
    }

    fun clearUserMemoForCurrentUser() {
        val currentEmail = _email.value
        if (currentEmail.isNotBlank()) {
            _allUserMemos.remove(currentEmail)
            _userMemo.value = ""
            saveAllMemosToStorage()
        }
    }
}