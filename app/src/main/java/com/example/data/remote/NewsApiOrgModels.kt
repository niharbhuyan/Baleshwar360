package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Moshi DTOs for NewsAPI v2 (`/v2/everything` and `/v2/top-headlines`) responses.
 */
@JsonClass(generateAdapter = true)
data class NewsApiOrgResponse(
    @Json(name = "status") val status: String? = null,
    @Json(name = "totalResults") val totalResults: Int? = null,
    @Json(name = "code") val code: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "articles") val articles: List<NewsApiOrgArticleDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NewsApiOrgArticleDto(
    @Json(name = "source") val source: NewsApiOrgSourceDto? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "urlToImage") val urlToImage: String? = null,
    @Json(name = "publishedAt") val publishedAt: String? = null,
    @Json(name = "content") val content: String? = null
)

@JsonClass(generateAdapter = true)
data class NewsApiOrgSourceDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null
)
