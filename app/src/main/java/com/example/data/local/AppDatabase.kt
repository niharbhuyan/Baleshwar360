package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        NewsArticleEntity::class,
        WeatherCacheEntity::class,
        DailyForecastEntity::class,
        HotspotEntity::class,
        CacheSyncMetadataEntity::class,
        UserEntity::class,
        ReviewEntity::class,
        ItineraryItemEntity::class,
        TravelJournalEntity::class,
        ChandipurTideEntity::class,
        CivicReportEntity::class,
        OdiaHoroscopeEntity::class,
        OdiaFestivalEntity::class,
        SavedBookmarkEntity::class,
        BusScheduleEntity::class,
        SevereWeatherAlertEntity::class,
        TrainScheduleEntity::class,
        TransportAlertEntity::class,
        EmergencyContactEntity::class,
        NewsSearchQueryEntity::class,
        CitizenVoteEntity::class,
        CommunityNewsSubmissionEntity::class
    ],
    version = 22,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Abstract getter method for Citizen Local News Submissions & Editorial Moderation DAO.
     */
    abstract fun communityNewsSubmissionDao(): CommunityNewsSubmissionDao

    /**
     * Abstract getter method for Citizen Voice Polling DAO (Offline votes and sync status).
     */
    abstract fun citizenVoteDao(): CitizenVoteDao

    /**
     * Abstract getter method for Recent News Search History DAO.
     */
    abstract fun newsSearchQueryDao(): NewsSearchQueryDao

    /**
     * Abstract getter method for Balasore Emergency Contacts DAO.
     */
    abstract fun emergencyContactDao(): EmergencyContactDao

    /**
     * Abstract getter method for Balasore Train Schedules DAO.
     */
    abstract fun trainScheduleDao(): TrainScheduleDao

    /**
     * Abstract getter method for Transport Delay Alerts DAO.
     */
    abstract fun transportAlertDao(): TransportAlertDao

    /**
     * Abstract getter method for Balasore Severe Weather Alert History DAO.
     */
    abstract fun severeWeatherAlertDao(): SevereWeatherAlertDao

    /**
     * Abstract getter method for Balasore Bus Stand Live Bus Schedules DAO.
     */
    abstract fun busScheduleDao(): BusScheduleDao

    /**
     * Abstract getter method for user-saved bookmarks (News articles & Emergency directory contacts).
     */
    abstract fun savedBookmarkDao(): SavedBookmarkDao

    /**
     * Abstract getter method for Odia Festivals & Cultural Dates DAO.
     */
    abstract fun odiaFestivalDao(): OdiaFestivalDao

    /**
     * Abstract getter method for Odia Daily Horoscope DAO.
     */
    abstract fun odiaHoroscopeDao(): OdiaHoroscopeDao

    /**
     * Abstract getter method for Citizen Civic Reports DAO (offline issue tracking & progression).
     */
    abstract fun civicReportDao(): CivicReportDao

    /**
     * Abstract getter method for News DAO to enable CRUD operations for local news and district bulletins.
     */
    abstract fun newsDao(): NewsDao

    /**
     * Abstract getter method for Tourism DAO to enable CRUD operations for tourism destinations, heritage spots, and beaches.
     */
    abstract fun tourismDao(): TourismDao

    /**
     * Abstract getter method for Custom Travel Itinerary DAO.
     */
    abstract fun itineraryDao(): ItineraryDao

    /**
     * Abstract getter method for Offline Travel Journal DAO.
     */
    abstract fun travelJournalDao(): TravelJournalDao

    /**
     * Abstract getter method for Chandipur Tide Schedule & Last Known Tide Offline Storage.
     */
    abstract fun chandipurTideDao(): ChandipurTideDao

    /**
     * Hotspot DAO getter implemented to delegate to tourismDao for backward compatibility.
     */
    fun hotspotDao(): HotspotDao = tourismDao()

    /**
     * Abstract getter method for Weather & marine forecast DAO.
     */
    abstract fun weatherDao(): WeatherDao

    /**
     * Abstract getter method for cache synchronization metadata.
     */
    abstract fun cacheMetadataDao(): CacheMetadataDao

    /**
     * Abstract getter method for user credentials and profiles.
     */
    abstract fun userDao(): UserDao

    /**
     * Abstract getter method for local community ratings and reviews.
     */
    abstract fun reviewDao(): ReviewDao

    companion object {
        private const val DATABASE_NAME = "balasore_app_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
