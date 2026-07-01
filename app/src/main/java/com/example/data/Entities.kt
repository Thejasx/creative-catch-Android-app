package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val password: String, // simple text password for simulated auth
    val userType: String, // "CLIENT" or "PROVIDER"
    val name: String,
    val category: String, // e.g. "Photographer", "Videographer", "Designer", "Makeup Artist"
    val location: String, // e.g. "San Francisco, CA"
    val pricePerHr: Double, // e.g. 75.0
    val bio: String,
    val avatarRes: String = "avatar_1", // identifier for built-in or custom avatars
    val rating: Double = 4.8,
    val jobsCount: Int = 12
) : Serializable

@Entity(tableName = "portfolio_items")
data class PortfolioItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val imageUrl: String, // either a drawable name, local path, or custom text
    val title: String,
    val description: String = ""
) : Serializable

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: Long,
    val receiverId: Long,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
