package com.flux.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flux.data.model.ProgressBoardModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressBoardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBoardItem(item: ProgressBoardModel)

    @Query("SELECT * FROM ProgressBoardModel WHERE workspaceId = :workspaceId")
    fun getBoardItemsByWorkspace(workspaceId: String): Flow<List<ProgressBoardModel>>

    @Delete
    suspend fun deleteBoardItem(item: ProgressBoardModel)

    @Query("Delete FROM ProgressBoardModel where workspaceId = :workspaceId")
    suspend fun deleteBoardItemsByWorkspace(workspaceId: String)

    @Query("SELECT * FROM ProgressBoardModel")
    fun getAllBoardItems(): Flow<List<ProgressBoardModel>>
}

