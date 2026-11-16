package com.example.data.repository

import com.example.data.local.SavedBookmarkDao
import com.example.data.local.SavedBookmarkEntity
import com.example.data.model.EmergencyContact
import com.example.data.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository abstracting Room Database access for Favorite/Saved bookmarks
 * (News Articles & Emergency Directory numbers).
 */
class SavedBookmarkRepository(
    private val dao: SavedBookmarkDao
) {

    val allBookmarks: Flow<List<SavedBookmarkEntity>> =
        dao.getAllBookmarks().distinctUntilChanged()

    val savedNewsBookmarks: Flow<List<SavedBookmarkEntity>> =
        dao.getBookmarksByType("NEWS").distinctUntilChanged()

    val savedEmergencyBookmarks: Flow<List<SavedBookmarkEntity>> =
        dao.getBookmarksByType("EMERGENCY").distinctUntilChanged()

    val savedEmergencyContactIds: Flow<Set<String>> =
        dao.getSavedOriginalIdsByType("EMERGENCY")
            .map { it.toSet() }
            .distinctUntilChanged()

    val savedNewsArticleIds: Flow<Set<String>> =
        dao.getSavedOriginalIdsByType("NEWS")
            .map { it.toSet() }
            .distinctUntilChanged()

    val totalSavedCount: Flow<Int> =
        dao.getBookmarkCountFlow().distinctUntilChanged()

    suspend fun saveEmergencyContact(contact: EmergencyContact) = withContext(Dispatchers.IO) {
        val entity = SavedBookmarkEntity(
            id = "emergency_${contact.id}",
            itemType = "EMERGENCY",
            originalId = contact.id,
            title = contact.name,
            odiaTitle = contact.odiaName,
            subtitle = contact.description,
            odiaSubtitle = contact.description,
            category = contact.category,
            phoneNumber = contact.number,
            sourceOrDepartment = if (contact.category.contains("Police", ignoreCase = true)) "Balasore Police District" else "Balasore Health & Disaster Bureau",
            timestamp = System.currentTimeMillis()
        )
        dao.insertBookmark(entity)
    }

    suspend fun removeEmergencyContact(contactId: String) = withContext(Dispatchers.IO) {
        dao.deleteBookmarkById("emergency_$contactId")
        dao.deleteBookmarkByOriginalId(contactId, "EMERGENCY")
    }

    suspend fun toggleEmergencyContact(contact: EmergencyContact, isCurrentlySaved: Boolean) = withContext(Dispatchers.IO) {
        if (isCurrentlySaved) {
            removeEmergencyContact(contact.id)
        } else {
            saveEmergencyContact(contact)
        }
    }

    suspend fun saveNewsArticle(article: NewsArticle) = withContext(Dispatchers.IO) {
        val entity = SavedBookmarkEntity(
            id = "news_${article.id}",
            itemType = "NEWS",
            originalId = article.id,
            title = article.title,
            odiaTitle = article.odiaTitle,
            subtitle = article.snippet,
            odiaSubtitle = article.odiaSnippet,
            category = article.category,
            sourceOrDepartment = article.source,
            content = article.content,
            odiaContent = article.odiaContent,
            timestamp = System.currentTimeMillis()
        )
        dao.insertBookmark(entity)
    }

    suspend fun removeNewsArticle(articleId: String) = withContext(Dispatchers.IO) {
        dao.deleteBookmarkById("news_$articleId")
        dao.deleteBookmarkByOriginalId(articleId, "NEWS")
    }

    suspend fun toggleNewsArticle(article: NewsArticle, isCurrentlySaved: Boolean) = withContext(Dispatchers.IO) {
        if (isCurrentlySaved) {
            removeNewsArticle(article.id)
        } else {
            saveNewsArticle(article)
        }
    }

    suspend fun deleteBookmarkById(bookmarkId: String) = withContext(Dispatchers.IO) {
        dao.deleteBookmarkById(bookmarkId)
    }
}
