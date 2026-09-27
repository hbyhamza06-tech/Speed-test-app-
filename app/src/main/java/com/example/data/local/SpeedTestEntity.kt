package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "speed_test_results")
data class SpeedTestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Int,
    val jitterMs: Int,
    val packetLossPercent: Float,
    val loadedPingMs: Int,
    val serverName: String,
    val serverLocation: String,
    val serverHost: String,
    val networkType: String,
    val wifiSsid: String,
    val ispName: String,
    val publicIp: String,
    val testDurationSec: Int,
    val ratingGrade: String
)
