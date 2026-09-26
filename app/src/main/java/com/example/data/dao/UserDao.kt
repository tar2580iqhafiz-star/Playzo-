package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR handle LIKE '%' || :query || '%' OR bio LIKE '%' || :query || '%'")
    fun searchUsersFlow(query: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id IN (SELECT targetUserId FROM follows WHERE followerUserId = :userId)")
    fun getFollowingFlow(userId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id IN (SELECT followerUserId FROM follows WHERE targetUserId = :userId)")
    fun getFollowersFlow(userId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(handle) = LOWER(:handle) AND id != :currentUserId)")
    suspend fun isHandleTaken(handle: String, currentUserId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET followersCount = :followersCount, isFollowedByMe = :isFollowed WHERE id = :userId")
    suspend fun updateFollowStatus(userId: String, followersCount: Int, isFollowed: Boolean)

    @Query("UPDATE users SET followingCount = :followingCount WHERE id = :userId")
    suspend fun updateFollowingCount(userId: String, followingCount: Int)

    @Query("UPDATE users SET isCurrentUser = CASE WHEN id = :activeUserId THEN 1 ELSE 0 END")
    suspend fun setActiveCurrentUser(activeUserId: String)

    @Query("UPDATE users SET isCurrentUser = 0")
    suspend fun clearActiveCurrentUser()

    @Query("UPDATE users SET isFollowedByMe = CASE WHEN id IN (SELECT targetUserId FROM follows WHERE followerUserId = :activeUserId) THEN 1 ELSE 0 END")
    suspend fun syncFollowStatusesForUser(activeUserId: String)

    @Query("UPDATE users SET isFollowedByMe = 0")
    suspend fun clearAllFollowStatuses()
}
