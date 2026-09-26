package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FollowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun follow(follow: FollowEntity)

    @Query("DELETE FROM follows WHERE followerUserId = :followerId AND targetUserId = :targetId")
    suspend fun unfollow(followerId: String, targetId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerUserId = :followerId AND targetUserId = :targetId)")
    suspend fun isFollowing(followerId: String, targetId: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM follows WHERE followerUserId = :followerId AND targetUserId = :targetId)")
    fun isFollowingFlow(followerId: String, targetId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM follows WHERE targetUserId = :targetId")
    suspend fun countFollowers(targetId: String): Int

    @Query("SELECT COUNT(*) FROM follows WHERE followerUserId = :followerId")
    suspend fun countFollowing(followerId: String): Int
}
