"""Automated unit tests for the Balasore 360 news & weather alert aggregation backend."""

from __future__ import annotations

import datetime as dt
import sys
import tempfile
import time
import unittest
from pathlib import Path
from typing import List, Tuple
from zoneinfo import ZoneInfo

sys.path.insert(0, str(Path(__file__).resolve().parent))

from aggregators import (
    BalasoreAggregationService,
    BalasoreNewsAggregator,
    BalasoreWeatherAggregator,
    derive_balasore_weather_alerts,
    parse_rss_xml_safely,
    validate_safe_url,
)
from config import BackendConfig
from database import (
    BalasoreDatabase,
    NewsArticleRecord,
    WeatherAlertRecord,
    WeatherSnapshotRecord,
)
from scheduler import BalasoreDailyScheduler, seconds_until_next_daily_run


SAMPLE_RSS_XML = b"""<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0">
  <channel>
    <title>Odisha Regional Feed</title>
    <item>
      <title><![CDATA[<b>Red Alert</b>: Heavy Rain & Subarnarekha Flood Watch in Balasore]]></title>
      <description>IMD warns Jaleswar, Basta and Bhograi blocks in Balasore district.</description>
      <link>https://news.google.com/articles/balasore-flood-01</link>
      <pubDate>Wed, 25 Nov 2026 06:00:00 GMT</pubDate>
      <source>IMD Balasore Bureau</source>
    </item>
    <item>
      <title>DRDO successfully tests coastal interceptor from ITR Chandipur</title>
      <description>Launch conducted from Integrated Test Range at Chandipur coast.</description>
      <link>https://news.google.com/articles/chandipur-itr-02</link>
      <pubDate>Wed, 25 Nov 2026 05:30:00 GMT</pubDate>
      <source>Odisha News Desk</source>
    </item>
    <item>
      <title>Unrelated Mumbai Stock Market Wrap</title>
      <description>Sensex closes higher on banking gains.</description>
      <link>https://news.google.com/articles/mumbai-03</link>
      <pubDate>Wed, 25 Nov 2026 05:00:00 GMT</pubDate>
    </item>
  </channel>
</rss>
"""


class FakeNewsAggregator(BalasoreNewsAggregator):
    def fetch_articles(self) -> Tuple[List[NewsArticleRecord], List[str]]:
        return parse_rss_xml_safely(SAMPLE_RSS_XML, "Test Feed"), []


class FakeWeatherAggregator(BalasoreWeatherAggregator):
    def fetch_weather_and_alerts(self) -> Tuple[List[WeatherSnapshotRecord], List[WeatherAlertRecord], List[str]]:
        now_ms = int(time.time() * 1000)
        snap = WeatherSnapshotRecord(
            id=f"snap_chandipur_{now_ms}",
            region_code="CHANDIPUR",
            provider="OpenWeatherMap",
            temperature_c=41.5,
            feels_like_c=45.2,
            humidity_pct=78,
            wind_speed_kmh=68.0,
            wind_gust_kmh=82.0,
            pressure_hpa=996,
            rain_mm_3h=58.0,
            weather_code="Thunderstorm",
            condition_summary="Severe Thunderstorm and Gale Winds",
            flood_risk_level="WARNING",
            tide_advisory="Avoid Chandipur beach walk during gale surge.",
            observed_at_ms=now_ms,
        )
        alerts = derive_balasore_weather_alerts(snap)
        return [snap], alerts, []


class BalasoreBackendTestCase(unittest.TestCase):
    def setUp(self) -> None:
        self.temp_dir = tempfile.TemporaryDirectory()
        self.db_path = Path(self.temp_dir.name) / "balasore_test.db"
        self.config = BackendConfig(
            db_path=self.db_path,
            openweathermap_api_key="",
            news_api_key="",
            firebase_service_account_path="",
            firestore_database_id="ai-studio-android-balasore-011eecca-ad37-48d7-9ed9-0ca2c072c765",
        )
        self.db = BalasoreDatabase(self.db_path)

    def tearDown(self) -> None:
        self.temp_dir.cleanup()

    def test_rss_parsing_filters_balasore_sanitizes_html_and_blocks_xxe(self) -> None:
        articles = parse_rss_xml_safely(SAMPLE_RSS_XML, "Test Feed")
        self.assertEqual(len(articles), 2)
        self.assertNotIn("<b>", articles[0].title)
        self.assertEqual(articles[0].category, "Weather")
        self.assertTrue(articles[0].is_breaking)
        self.assertEqual(articles[0].region_code, "JALESWAR")
        self.assertEqual(articles[1].category, "Defense")
        self.assertEqual(articles[1].region_code, "CHANDIPUR")

        xxe_payload = b"<!DOCTYPE foo [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]><rss></rss>"
        with self.assertRaises(ValueError):
            parse_rss_xml_safely(xxe_payload, "Bad Feed")

    def test_url_validation_blocks_unsafe_schemes_and_private_hosts(self) -> None:
        self.assertIsNone(validate_safe_url("javascript:alert(1)"))
        self.assertIsNone(validate_safe_url("data:text/html,<script>alert(1)</script>"))
        self.assertIsNone(validate_safe_url("http://127.0.0.1:8080/admin"))
        self.assertIsNone(validate_safe_url("http://169.254.169.254/latest/meta-data"))
        self.assertEqual(
            validate_safe_url("https://news.google.com/articles/balasore"),
            "https://news.google.com/articles/balasore",
        )

    def test_end_to_end_aggregation_deduplication_and_expiry(self) -> None:
        service = BalasoreAggregationService(
            config=self.config,
            db=self.db,
            news_aggregator=FakeNewsAggregator(self.config),
            weather_aggregator=FakeWeatherAggregator(self.config),
        )
        first = service.run_aggregation(trigger_type="MANUAL")
        self.assertEqual(first.status, "SUCCESS")
        self.assertEqual(first.news_upserted, 2)
        self.assertEqual(first.snapshots_saved, 1)
        self.assertEqual(first.alerts_upserted, 4)  # CYCLONE, FLOOD, HEATWAVE, THUNDERSTORM

        # Running a second time deduplicates via content_hash / alert_hash
        second = service.run_aggregation(trigger_type="SCHEDULED_DAILY")
        self.assertEqual(second.status, "SUCCESS")
        self.assertEqual(len(self.db.get_latest_news()), 2)
        self.assertEqual(len(self.db.get_active_weather_alerts()), 4)
        self.assertEqual(len(self.db.get_latest_weather_snapshots()), 1)
        self.assertEqual(len(self.db.get_recent_runs()), 2)

        # Deactivating expired alerts works cleanly
        expired = self.db.deactivate_expired_alerts(now_ms=int(time.time() * 1000) + 24 * 60 * 60 * 1000)
        self.assertEqual(expired, 4)
        self.assertEqual(len(self.db.get_active_weather_alerts()), 0)

    def test_daily_schedule_time_calculation_and_startup_run(self) -> None:
        ist = ZoneInfo("Asia/Kolkata")
        before_run = dt.datetime(2026, 11, 25, 5, 30, 0, tzinfo=ist)
        self.assertEqual(seconds_until_next_daily_run(6, 0, "Asia/Kolkata", now=before_run), 1800.0)

        after_run = dt.datetime(2026, 11, 25, 6, 15, 0, tzinfo=ist)
        self.assertEqual(seconds_until_next_daily_run(6, 0, "Asia/Kolkata", now=after_run), 23 * 3600 + 45 * 60)

        service = BalasoreAggregationService(
            config=self.config,
            db=self.db,
            news_aggregator=FakeNewsAggregator(self.config),
            weather_aggregator=FakeWeatherAggregator(self.config),
        )
        scheduler = BalasoreDailyScheduler(service=service, hour=6, minute=0, timezone_name="Asia/Kolkata")
        engine = scheduler.start(run_immediately=True)
        self.assertIn(engine, {"apscheduler", "stdlib-thread"})
        scheduler.stop()
        self.assertEqual(len(self.db.get_latest_news()), 2)

    def test_news_topic_search_tracking_and_ranking(self) -> None:
        # Seed aggregated Balasore articles first to verify matching_articles count
        articles = parse_rss_xml_safely(SAMPLE_RSS_XML, "Test Feed")
        self.db.upsert_news_articles(articles)

        self.db.record_news_topic_search("Chandipur")
        self.db.record_news_topic_search("Subarnarekha Flood")
        self.db.record_news_topic_search("Subarnarekha Flood")
        self.db.record_news_topic_search("Subarnarekha Flood")
        self.db.record_news_topic_search("Chandipur")
        self.db.record_news_topic_search("Khirachora Temple")

        ranked = self.db.get_ranked_news_topics(limit=10)
        self.assertEqual(len(ranked), 3)
        self.assertEqual(ranked[0]["rank"], 1)
        self.assertEqual(ranked[0]["topic_label"], "Subarnarekha Flood")
        self.assertEqual(ranked[0]["search_count"], 3)
        self.assertEqual(ranked[0]["category"], "Weather")
        self.assertEqual(ranked[1]["rank"], 2)
        self.assertEqual(ranked[1]["topic_label"], "Chandipur")
        self.assertEqual(ranked[1]["search_count"], 2)
        self.assertGreaterEqual(ranked[1]["matching_articles"], 1)
        self.assertEqual(ranked[2]["rank"], 3)
        self.assertEqual(ranked[2]["category"], "Heritage")

        self.assertTrue(self.db.delete_news_topic_search("khirachora temple"))
        self.assertEqual(len(self.db.get_ranked_news_topics()), 2)
        self.assertEqual(self.db.clear_news_topic_searches(), 2)
        self.assertEqual(len(self.db.get_ranked_news_topics()), 0)

    def test_news_topic_7_day_search_trends(self) -> None:
        self.db.record_news_topic_search("Chandipur Tide", search_date="2026-11-19")
        self.db.record_news_topic_search("Chandipur Tide", search_date="2026-11-22")
        self.db.record_news_topic_search("Chandipur Tide", search_date="2026-11-22")
        self.db.record_news_topic_search("Chandipur Tide", search_date="2026-11-25")
        self.db.record_news_topic_search("Chandipur Tide", search_date="2026-11-25")
        self.db.record_news_topic_search("Chandipur Tide", search_date="2026-11-25")

        trend = self.db.get_news_topic_trends("Chandipur Tide", days=7, end_date="2026-11-25")
        self.assertEqual(len(trend), 7)
        self.assertEqual(trend[0]["date"], "2026-11-19")
        self.assertEqual(trend[0]["searches"], 1)
        self.assertEqual(trend[1]["searches"], 0)
        self.assertEqual(trend[3]["date"], "2026-11-22")
        self.assertEqual(trend[3]["searches"], 2)
        self.assertEqual(trend[6]["date"], "2026-11-25")
        self.assertEqual(trend[6]["searches"], 3)
        self.assertEqual(sum(point["searches"] for point in trend), 6)

        # Cascade delete clears daily buckets cleanly
        self.assertTrue(self.db.delete_news_topic_search("chandipur tide"))
        cleared_trend = self.db.get_news_topic_trends("chandipur tide", days=7, end_date="2026-11-25")
        self.assertEqual(sum(point["searches"] for point in cleared_trend), 0)

    def test_tourism_hotspots_schema_and_map_queries(self) -> None:
        all_spots = self.db.get_tourism_hotspots()
        self.assertGreaterEqual(len(all_spots), 8)
        spot_ids = {s["spot_id"] for s in all_spots}
        self.assertIn("spot_chandipur_beach", spot_ids)
        self.assertIn("spot_remuna_khirachora", spot_ids)

        chandipur = next(s for s in all_spots if s["spot_id"] == "spot_chandipur_beach")
        self.assertAlmostEqual(chandipur["latitude"], 21.4416, places=3)
        self.assertAlmostEqual(chandipur["longitude"], 87.0149, places=3)
        self.assertGreaterEqual(len(chandipur["images"]), 2)

        # Bounding-box filter around Chandipur & Remuna excludes far-north Talasari
        central_bbox = self.db.get_tourism_hotspots(min_lat=21.40, max_lat=21.56, min_lon=86.80, max_lon=87.10)
        central_ids = {s["spot_id"] for s in central_bbox}
        self.assertIn("spot_chandipur_beach", central_ids)
        self.assertIn("spot_remuna_khirachora", central_ids)
        self.assertNotIn("spot_talasari_beach", central_ids)

        geojson = self.db.get_tourism_hotspots_geojson(category="BEACH")
        self.assertEqual(geojson["type"], "FeatureCollection")
        self.assertGreaterEqual(len(geojson["features"]), 2)
        first_geom = geojson["features"][0]["geometry"]
        self.assertEqual(first_geom["type"], "Point")
        self.assertEqual(len(first_geom["coordinates"]), 2)


if __name__ == "__main__":
    unittest.main()


