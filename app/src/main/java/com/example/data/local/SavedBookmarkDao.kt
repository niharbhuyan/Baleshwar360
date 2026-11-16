package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedBookmarkDao {
    @Query("SELECT * FROM saved_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<SavedBookmarkEntity>>

    @Query("SELECT * FROM saved_bookmarks WHERE itemType = :type ORDER BY timestamp DESC")
    fun getBookmarksByType(type: String): Flow<List<SavedBookmarkEntity>>

    @Query("SELECT originalId FROM saved_bookmarks WHERE itemType = :type")
    fun getSavedOriginalIdsByType(type: String): Flow<List<String>>

    @Query("SELECT * FROM saved_bookmarks ORDER BY timestamp DESC")
    suspend fun getAllBookmarksSync(): List<SavedBookmarkEntity>

    @Query("SELECT * FROM saved_bookmarks WHERE id = :id LIMIT 1")
    suspend fun getBookmarkById(id: String): SavedBookmarkEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM saved_bookmarks WHERE id = :id)")
    fun isBookmarkedFlow(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_bookmarks WHERE id = :id)")
    suspend fun isBookmarked(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: SavedBookmarkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmarks(bookmarks: List<SavedBookmarkEntity>)

    @Query("DELETE FROM saved_bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String): Int

    @Query("DELETE FROM saved_bookmarks WHERE originalId = :originalId AND itemType = :itemType")
    suspend fun deleteBookmarkByOriginalId(originalId: String, itemType: String): Int

    @Query("DELETE FROM saved_bookmarks")
    suspend fun clearAllBookmarks()

    @Query("SELECT COUNT(*) FROM saved_bookmarks")
    fun getBookmarkCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM saved_bookmarks")
    suspend fun getBookmarkCount(): Int
}
