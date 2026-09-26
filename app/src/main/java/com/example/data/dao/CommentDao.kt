package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE videoId = :videoId ORDER BY createdAtTimestamp DESC")
    fun getCommentsForVideoFlow(videoId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Update
    suspend fun updateComment(comment: CommentEntity)

    @Query("UPDATE comments SET likesCount = :likesCount, isLikedByMe = :isLiked WHERE id = :commentId")
    suspend fun updateCommentLike(commentId: String, likesCount: Int, isLiked: Boolean)

    @Query("SELECT COUNT(*) FROM comments WHERE videoId = :videoId")
    suspend fun getCommentCountForVideo(videoId: String): Int
}
