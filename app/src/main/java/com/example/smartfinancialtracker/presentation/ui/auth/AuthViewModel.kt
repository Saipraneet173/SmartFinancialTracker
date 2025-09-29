package com.example.smartfinancialtracker.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfinancialtracker.domain.model.AuthResult
import com.example.smartfinancialtracker.domain.model.AuthState
import com.example.smartfinancialtracker.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val currentUser = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading

            when (val result = authRepository.signUp(email, password, displayName)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.exception.message ?: "Sign up failed")
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
            _isLoading.value = false
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading

            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    _authState.value = AuthState.Authenticated(result.data)
                }
                is AuthResult.Error -> {
                    _authState.value = AuthState.Error(result.exception.message ?: "Sign in failed")
                }
                is AuthResult.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun resetPassword(email: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.resetPassword(email)) {
                is AuthResult.Success -> {
                    onComplete(true, "Password reset email sent")
                }
                is AuthResult.Error -> {
                    onComplete(false, result.exception.message)
                }
                is AuthResult.Loading -> { /* Handle if needed */ }
            }
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Validation functions
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email cannot be empty"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
    }
}