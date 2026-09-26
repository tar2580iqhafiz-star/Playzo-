package com.example.data.model

import androidx.room.Entity

@Entity(
    tableName = "follows",
    primaryKeys = ["followerUserId", "targetUserId"]
)
data class FollowEntity(
    val followerUserId: String,
    val targetUserId: String,
    val followedAt: Long = System.currentTimeMillis()
)
