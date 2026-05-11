package com.shaalevikas.app.data

data class Need(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val targetAmount: Double = 0.0,
    val pledgedAmount: Double = 0.0,
    val beforePhotoUrl: String = "",
    val afterPhotoUrl: String = "",
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Int
        get() = if (targetAmount > 0) ((pledgedAmount / targetAmount) * 100).toInt().coerceAtMost(100) else 0
}

data class Pledge(
    val id: String = "",
    val needId: String = "",
    val alumniName: String = "",
    val alumniCity: String = "",
    val batchYear: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserRole(
    val uid: String = "",
    val email: String = "",
    val role: String = "alumni"
)

val CATEGORIES = listOf("Infrastructure", "Furniture", "Sanitation", "Stationery", "Other")
