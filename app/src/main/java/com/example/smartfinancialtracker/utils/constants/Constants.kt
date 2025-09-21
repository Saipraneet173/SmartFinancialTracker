package com.example.smartfinancialtracker.utils.constants

object Constants {
    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val EXPENSES_COLLECTION = "expenses"
    const val CATEGORIES_COLLECTION = "categories"

    // Preferences Keys
    const val PREFS_NAME = "smart_financial_tracker_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_IS_FIRST_LAUNCH = "is_first_launch"

    // Database
    const val DATABASE_NAME = "smart_financial_tracker.db"
    const val DATABASE_VERSION = 1

    // Default Categories
    val DEFAULT_CATEGORIES = listOf(
        "Food & Dining",
        "Transportation",
        "Shopping",
        "Entertainment",
        "Bills & Utilities",
        "Healthcare",
        "Education",
        "Personal Care",
        "Travel",
        "Insurance",
        "Savings",
        "Investments",
        "Rent/EMI",
        "Gifts & Donations",
        "Others"
    )
}