package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestDao {

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<SpeedTestEntity>>

    @Query("SELECT * FROM speed_test_results WHERE networkType = :networkType ORDER BY timestamp DESC")
    fun getResultsByNetwork(networkType: String): Flow<List<SpeedTestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: SpeedTestEntity): Long

    @Query("DELETE FROM speed_test_results WHERE id = :id")
    suspend fun deleteResult(id: Long)

    @Query("DELETE FROM speed_test_results")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM speed_test_results")
    suspend fun getCount(): Int

    @Query("SELECT AVG(downloadMbps) FROM speed_test_results")
    suspend fun getAverageDownload(): Float?

    @Query("SELECT AVG(uploadMbps) FROM speed_test_results")
    suspend fun getAverageUpload(): Float?
}
