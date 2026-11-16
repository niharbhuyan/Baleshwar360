package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "news_search_queries")
data class NewsSearchQueryEntity(
    @PrimaryKey
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface NewsSearchQueryDao {
    @Query("SELECT * FROM news_search_queries ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearchQueries(): Flow<List<NewsSearchQueryEntity>>

    @Query("SELECT * FROM news_search_queries ORDER BY timestamp DESC LIMIT 10")
    fun getRecentQueries(): Flow<List<NewsSearchQueryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(entity: NewsSearchQueryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQuery(entity: NewsSearchQueryEntity)

    @Query("DELETE FROM news_search_queries WHERE query = :query")
    suspend fun deleteSearchQuery(query: String)

    @Query("DELETE FROM news_search_queries WHERE query = :query")
    suspend fun deleteQuery(query: String)

    @Query("DELETE FROM news_search_queries")
    suspend fun clearAllSearchQueries()

    @Query("DELETE FROM news_search_queries")
    suspend fun clearAllQueries()
}
