package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ActiveSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    fun getActiveSessionFlow(): Flow<ActiveSessionEntity?>

    @Query("SELECT * FROM active_session WHERE id = 1 LIMIT 1")
    suspend fun getActiveSession(): ActiveSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setActiveSession(session: ActiveSessionEntity)

    @Query("DELETE FROM active_session")
    suspend fun clearActiveSession()
}
