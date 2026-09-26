package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY createdAtTimestamp DESC")
    fun getAllVideosFlow(): Flow<List<VideoEntity>>

    // Long Videos Feed
    @Query("SELECT * FROM videos WHERE isShort = 0 ORDER BY createdAtTimestamp DESC")
    fun getLongVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isShort = 0 AND category = :category ORDER BY createdAtTimestamp DESC")
    fun getLongVideosByCategoryFlow(category: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isShort = 0 AND creatorId IN (SELECT targetUserId FROM follows WHERE followerUserId = :currentUserId) ORDER BY createdAtTimestamp DESC")
    fun getFollowingLongFeedFlow(currentUserId: String): Flow<List<VideoEntity>>

    // Short Videos Feed
    @Query("SELECT * FROM videos WHERE isShort = 1 ORDER BY createdAtTimestamp DESC")
    fun getShortVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE creatorId = :creatorId ORDER BY createdAtTimestamp DESC")
    fun getVideosByCreatorFlow(creatorId: String): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE creatorId = :creatorId AND isShort = :isShort ORDER BY createdAtTimestamp DESC")
    fun getVideosByCreatorAndTypeFlow(creatorId: String, isShort: Boolean): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isLikedByMe = 1 ORDER BY createdAtTimestamp DESC")
    fun getLikedVideosFlow(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :videoId LIMIT 1")
    fun getVideoByIdFlow(videoId: String): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE id = :videoId LIMIT 1")
    suspend fun getVideoById(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR creatorName LIKE '%' || :query || '%' OR creatorHandle LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchVideosFlow(query: String): Flow<List<VideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<VideoEntity>)

    @Update
    suspend fun updateVideo(video: VideoEntity)

    @Query("UPDATE videos SET likesCount = :likesCount, isLikedByMe = :isLiked WHERE id = :videoId")
    suspend fun updateLikeStatus(videoId: String, likesCount: Long, isLiked: Boolean)

    @Query("UPDATE videos SET viewsCount = viewsCount + 1 WHERE id = :videoId")
    suspend fun incrementViewCount(videoId: String)

    @Query("UPDATE videos SET commentsCount = commentsCount + 1 WHERE id = :videoId")
    suspend fun incrementCommentCount(videoId: String)

    @Query("UPDATE videos SET creatorName = :name, creatorHandle = :handle, creatorCustomAvatarUri = :customAvatarUri WHERE creatorId = :creatorId")
    suspend fun updateCreatorInfo(creatorId: String, name: String, handle: String, customAvatarUri: String?)
}
