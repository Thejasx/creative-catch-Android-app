package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: Long): User?

    @Query("SELECT * FROM users WHERE userType = 'PROVIDER'")
    fun getAllProviders(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio_items WHERE userId = :userId ORDER BY id DESC")
    fun getPortfolioForUser(userId: Long): Flow<List<PortfolioItem>>

    @Query("SELECT * FROM portfolio_items WHERE userId = :userId ORDER BY id DESC")
    suspend fun getPortfolioForUserSync(userId: Long): List<PortfolioItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPortfolioItem(item: PortfolioItem)

    @Delete
    suspend fun deletePortfolioItem(item: PortfolioItem)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE (senderId = :user1Id AND receiverId = :user2Id) OR (senderId = :user2Id AND receiverId = :user1Id) ORDER BY timestamp ASC")
    fun getChatMessages(user1Id: Long, user2Id: Long): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
    
    @Query("SELECT DISTINCT CASE WHEN senderId = :userId THEN receiverId ELSE senderId END FROM chat_messages WHERE senderId = :userId OR receiverId = :userId")
    fun getChattedUserIds(userId: Long): Flow<List<Long>>
}
