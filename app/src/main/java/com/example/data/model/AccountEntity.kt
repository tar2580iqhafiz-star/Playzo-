package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Stores private authentication data for a Playzo user account.
 * This table is completely isolated from the public [UserEntity].
 * Passwords are NEVER stored in plain text.
 * Email addresses are NEVER exposed to other users.
 */
@Entity(
    tableName = "user_accounts",
    indices = [
        Index(value = ["email"], unique = true)
    ]
)
data class AccountEntity(
    @PrimaryKey
    val userId: String,
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val resetCode: String? = null,
    val resetCodeExpiryTimestamp: Long? = null
)

/**
 * Tracks the current active local session.
 * Keeping only one active user signed in at a time.
 */
@Entity(tableName = "active_session")
data class ActiveSessionEntity(
    @PrimaryKey
    val id: Int = 1,
    val userId: String,
    val sessionCreatedAt: Long = System.currentTimeMillis()
)

/**
 * Safe private account summary returned ONLY to the authenticated user.
 * Sensitive fields (password, hash, salt, reset tokens) are excluded.
 */
data class PrivateAccountInfo(
    val userId: String,
    val email: String,
    val createdAtTimestamp: Long
)
