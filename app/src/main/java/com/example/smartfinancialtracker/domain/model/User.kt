package com.example.smartfinancialtracker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val createdAt: Date = Date(),
    val lastLoginAt: Date = Date(),

    // App-specific fields
    val monthlyBudget: Double = 0.0,
    val currency: String = "INR",
    val categories: List<String> = emptyList(),
    val isPremium: Boolean = false
) : Parcelable