package com.example.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * An OkHttp Interceptor simulating the local Balasore Government & Tourism API endpoints
 * for news, coastal marine weather, and emergency advisories.
 * This ensures the Retrofit network layer performs realistic, full HTTP request/response
 * cycles, JSON serialization via Moshi, and network-to-Room persistence.
 */
class BalasoreMockApiInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()

        val jsonMediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        val responseBodyString: String? = when {
            url.contains("/api/v1/balasore/news/breaking") -> {
                val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                """
                {
                    "status": "ok",
                    "totalResults": 2,
                    "source": "Balasore District Emergency Operations Centre",
                    "articles": [
                        {
                            "id": 101,
                            "title": "Bay of Bengal High-Tide Cautionary Siren Installed at Chandipur Promenade",
                            "summary": "Automated sensor-based alert sounds 30 minutes before sea returns to safeguard tourists walking the receding sea bed.",
                            "content": "In an effort to maximize safety on Chandipur Beach, district coastal security has installed solar sirens synchronized with real-time oceanographic tide gauges. Visitors will hear warning chimes 30 minutes prior to high tide onset.",
                            "category": "Weather",
                            "source": "Coastal Police & Marine Desk",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis()},
                            "isBreaking": true,
                            "location": "Chandipur Beach"
                        },
                        {
                            "id": 102,
                            "title": "Cyclone Preparedness Drill Conducted Across 12 Coastal Blocks of Balasore",
                            "summary": "District administration tests emergency shelter readiness, relief stock supplies, and satellite communication links.",
                            "content": "A high-level pre-monsoon cyclone preparedness drill was successfully completed across all 12 coastal blocks including Remuna, Bahanaga, and Chandipur. Quick response teams inspected multi-purpose cyclone shelters and verified early warning sirens.",
                            "category": "Weather",
                            "source": "District Disaster Management Authority",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 3600000},
                            "isBreaking": true,
                            "location": "Balasore District"
                        }
                    ]
                }
                """.trimIndent()
            }
            url.contains("/api/v1/balasore/news") -> {
                val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                """
                {
                    "status": "ok",
                    "totalResults": 6,
                    "source": "Balasore Information & Public Relations Department",
                    "articles": [
                        {
                            "id": 101,
                            "title": "Bay of Bengal High-Tide Cautionary Siren Installed at Chandipur Promenade",
                            "summary": "Automated sensor-based alert sounds 30 minutes before sea returns to safeguard tourists walking the receding sea bed.",
                            "content": "In an effort to maximize safety on Chandipur Beach, district coastal security has installed solar sirens synchronized with real-time oceanographic tide gauges. Visitors will hear warning chimes 30 minutes prior to high tide onset.",
                            "category": "Weather",
                            "source": "Coastal Police & Marine Desk",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis()},
                            "isBreaking": true,
                            "location": "Chandipur Beach"
                        },
                        {
                            "id": 102,
                            "title": "Cyclone Preparedness Drill Conducted Across 12 Coastal Blocks of Balasore",
                            "summary": "District administration tests emergency shelter readiness, relief stock supplies, and satellite communication links.",
                            "content": "A high-level pre-monsoon cyclone preparedness drill was successfully completed across all 12 coastal blocks including Remuna, Bahanaga, and Chandipur. Quick response teams inspected multi-purpose cyclone shelters and verified early warning sirens.",
                            "category": "Weather",
                            "source": "District Disaster Management Authority",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 1800000},
                            "isBreaking": true,
                            "location": "Balasore District"
                        },
                        {
                            "id": 103,
                            "title": "Balasore Municipal Corporation Rolls Out Eco-Electric City Shuttles",
                            "summary": "New fleet connecting Balasore Railway Station, Station Square, and Remuna Gopinath Temple for commuters.",
                            "content": "To facilitate clean transportation for residents and pilgrims, the Balasore Municipality has launched an eco-friendly electric feeder service. The buses run at 15-minute intervals connecting major transit hubs.",
                            "category": "Local",
                            "source": "Balasore Municipal Corporation",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 7200000},
                            "isBreaking": false,
                            "location": "Balasore City"
                        },
                        {
                            "id": 104,
                            "title": "District Council Passes Resolution on Industrial Park Expansion Near Kuruda",
                            "summary": "Political consensus reached on expanding infrastructure and agro-processing clusters in Balasore district.",
                            "content": "In the latest district council meeting, elected representatives unanimously passed a resolution approving industrial park modernization near Kuruda. The project aims to attract food processing units and boost rural employment.",
                            "category": "Politics",
                            "source": "District Press Bureau",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 10800000},
                            "isBreaking": false,
                            "location": "Kuruda Industrial Area"
                        },
                        {
                            "id": 105,
                            "title": "Annual Balasore Heritage Walk & Cultural Conclave Commences This Weekend",
                            "summary": "Heritage enthusiasts, artists, and students gather to explore 10th-century temples and maritime relics.",
                            "content": "The annual Balasore Heritage Walk series kicks off this weekend from Fakir Mohan College square. Guided walking tours of historical monuments and evening folk art exhibitions are scheduled across the town.",
                            "category": "Events",
                            "source": "Balasore Cultural Foundation",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 14400000},
                            "isBreaking": false,
                            "location": "Fakir Mohan Square"
                        },
                        {
                            "id": 106,
                            "title": "Chandipur Marine Eco-Tourism Initiative Launched with Guided Mudflat Tours",
                            "summary": "Forest department and local boatmen introduce educational excursions studying horseshoe crabs and coastal mangroves.",
                            "content": "Visitors to Chandipur can now participate in certified eco-tours led by marine biologists and traditional fishing communities. The guided walks focus on preserving endangered horseshoe crab breeding zones along the receding shoreline.",
                            "category": "Tourism",
                            "source": "Odisha Tourism Development Corporation",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 18000000},
                            "isBreaking": false,
                            "location": "Chandipur Sea Beach"
                        },
                        {
                            "id": 107,
                            "title": "ISRO & DRDO Coordinate Gaganyaan Crew Module Sea Recovery Trials in Bay of Bengal",
                            "summary": "Naval divers and telemetry stations at Chandipur, Dhamra, and Sriharikota validate precision parachute splashdown and recovery protocols.",
                            "content": "In a major milestone for India's indigenous human spaceflight program Gaganyaan, ISRO and DRDO successfully completed sea-recovery validation sorties in the northern Bay of Bengal, tracked by high-precision optical radars at ITR Chandipur.",
                            "category": "National News",
                            "source": "Press Trust of India (PTI) National Bureau",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 1200000},
                            "isBreaking": true,
                            "location": "Bay of Bengal / New Delhi"
                        },
                        {
                            "id": 108,
                            "title": "Union Cabinet Clears ₹12,000-Cr National Coastal Multi-Modal Logistics Corridor",
                            "summary": "Mega expressway and freight network to connect industrial manufacturing belts directly to deep-water eastern seaboard ports.",
                            "content": "The Union Cabinet has approved the ambitious National Coastal Freight Corridor connecting Kolkata, Kharagpur, Balasore, Paradip, and Visakhapatnam to interior industrial zones, reducing transit costs by up to 30%.",
                            "category": "National News",
                            "source": "National Infrastructure Wire New Delhi",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 3600000},
                            "isBreaking": false,
                            "location": "New Delhi"
                        },
                        {
                            "id": 109,
                            "title": "UN Ocean Summit Adopts Historic Global Marine Biodiversity Treaty for Bay of Bengal",
                            "summary": "Over 190 nations ratify legally binding High Seas pact creating international ocean sanctuaries against illegal commercial overfishing.",
                            "content": "The United Nations Global Ocean Conference in Geneva formally ratified the High Seas Biodiversity Agreement (BBNJ), establishing protected maritime conservation corridors for migratory Olive Ridley sea turtles and horseshoe crabs.",
                            "category": "International News",
                            "source": "Global Climate & Ocean Watch (Geneva)",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 2400000},
                            "isBreaking": false,
                            "location": "Geneva, Switzerland"
                        },
                        {
                            "id": 110,
                            "title": "World Meteorological Organization Releases Tropical Cyclone Global Sentinel Report",
                            "summary": "Rising sea surface temperatures accelerate cyclone intensification rates; early AI warning models recommended for coastal communities.",
                            "content": "The World Meteorological Organization in Geneva published its annual Tropical Cyclone State of the Climate assessment, highlighting India's coastal cyclone tracking and localized zero-casualty evacuation infrastructure as a premier global benchmark.",
                            "category": "International News",
                            "source": "World Meteorological Organization (WMO)",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 5400000},
                            "isBreaking": false,
                            "location": "Geneva / Worldwide"
                        },
                        {
                            "id": 111,
                            "title": "Balasore Zilla Parishad & Municipal Council Approve ₹185-Cr Coastal Drainage & Ward Upgrade Budget",
                            "summary": "Bipartisan district committee prioritizes storm-water channels across all 31 municipal wards and rural block roads in Remuna and Basta.",
                            "content": "During the special budget session at Balasore Collectorate, elected representatives and Zilla Parishad members passed a ₹185-crore civic and rural infrastructure package focusing on monsoon drainage, solar streetlights, and drinking water pipelines.",
                            "category": "Politics",
                            "source": "Balasore District Bureau",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 2800000},
                            "isBreaking": false,
                            "location": "Balasore Collectorate"
                        },
                        {
                            "id": 112,
                            "title": "Balasore Hosts North Odisha Inter-District Athletics & Football Championship at Police Line Stadium",
                            "summary": "Over 480 young athletes from Balasore, Mayurbhanj, and Bhadrak compete in track events, coastal marathon, and football finals.",
                            "content": "The annual North Odisha Athletics & Football Meet kicked off at Balasore Police Line Stadium and Paramahansa Yadav Stadium. Fakir Mohan University and Balasore Sports Hostel sprinters clinched gold medals in the 100m and 400m relays.",
                            "category": "Sports",
                            "source": "Balasore District Sports Association",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 3200000},
                            "isBreaking": false,
                            "location": "Balasore Stadium"
                        },
                        {
                            "id": 113,
                            "title": "DRDO ITR Chandipur Completes High-Speed Electro-Optical Telemetry & Air Defense Radar Trials",
                            "summary": "Indigenous coastal tracking sensors at Launch Complex-III track supersonic aerial targets with sub-meter precision over Bay of Bengal.",
                            "content": "Scientists at the Integrated Test Range (ITR) in Chandipur validated next-generation electro-optical and C-band telemetry radars supporting India's indigenous air defense shield. Marine Police confirmed smooth coordination with coastal fishermen.",
                            "category": "Defense",
                            "source": "ITR Chandipur Defense Desk",
                            "publishedAt": "Updated $currentTime",
                            "timestamp": ${System.currentTimeMillis() - 4100000},
                            "isBreaking": true,
                            "location": "ITR Chandipur"
                        }
                    ]
                }
                """.trimIndent()
            }
            url.contains("/api/v1/balasore/weather/marine-observatory") -> {
                """
                {
                    "stationId": "BLS_CHANDIPUR_01",
                    "stationName": "Chandipur Marine Weather Observatory",
                    "latitude": 21.4682,
                    "longitude": 87.0163,
                    "tideState": "RECEDING",
                    "tideDescription": "Sea recedes up to 5 kilometers during low tide. Ideal for beach strolling until siren warning.",
                    "nextTideTransition": "High tide expected in 3 hours 20 minutes",
                    "alertLevel": "NORMAL",
                    "alertTitle": "Safe Coastal Conditions",
                    "alertMessage": "Sea conditions are calm to moderate along Balasore coastline. Safe for visitors and traditional artisanal fishing within 5 nautical miles.",
                    "coastalAdvisory": "Caution: Avoid walking beyond 2 km from shore markers when warning flags are hoisted.",
                    "seaCondition": "Slight to Moderate Waves (0.6m - 1.1m)"
                }
                """.trimIndent()
            }
            url.contains("/api/v1/balasore/emergency/live-alerts") -> {
                val currentTime = SimpleDateFormat("h:mm a, dd MMM yyyy", Locale.getDefault()).format(Date())
                """
                {
                    "status": "active_emergency_bulletin",
                    "issuedAt": "$currentTime",
                    "activeCount": 2,
                    "alerts": [
                        {
                            "id": "alert_cyclone_live_01",
                            "title": "IMD Red Warning: Cyclone Alert for Balasore Coastal Belt",
                            "odiaTitle": "ବାଲେଶ୍ୱର ଉପକୂଳରେ ବାତ୍ୟା ସତର୍କ ସୂଚନା (ନାଲି ଚେତାବନୀ)",
                            "type": "CYCLONE",
                            "severity": "CRITICAL",
                            "summary": "Deep depression over Northwest Bay of Bengal intensifying. Squally winds 85-95 km/h expected near Chandipur & Talasari.",
                            "details": "Special Relief Commissioner advises total suspension of artisanal fishing. Multipurpose cyclone shelters in Bhograi, Remuna, and Sadar blocks opened for coastal residents.",
                            "affectedArea": "Chandipur, Talasari, Bahanaga & Kasafal Coast",
                            "windSpeedKmph": 95,
                            "actionRequired": "Stay away from exposed intertidal mudflats. Secure coastal rooftop assets.",
                            "emergencyHelpline": "06782-262244"
                        },
                        {
                            "id": "alert_river_spike_02",
                            "title": "Flood Alert: Subarnarekha River Level Spike at Rajghat",
                            "odiaTitle": "ସୁବର୍ଣ୍ଣରେଖା ନଦୀରେ ଜଳସ୍ତର ବୃଦ୍ଧି ସତର୍କତା (ରାଜଘାଟ ଷ୍ଟେସନ)",
                            "type": "RIVER_SPIKE",
                            "severity": "HIGH_PRIORITY",
                            "summary": "Subarnarekha River water level spiked to 10.42m, exceeding Danger Level (10.36m) following upstream Galudih barrage discharge.",
                            "details": "Rapid influx of 1,50,000 cusecs recorded. Low-lying riparian hamlets in Jaleswar and Baliapal blocks instructed to remain vigilant. ODRAF team stationed at Jaleswar bridge.",
                            "affectedArea": "Jaleswar, Bhograi & Baliapal Riverine Basin",
                            "waterLevelMeters": 10.42,
                            "dangerLevelMeters": 10.36,
                            "actionRequired": "Riparian residents should avoid crossing submerged bridges and stay tuned to Block Nodal Officer updates.",
                            "emergencyHelpline": "06781-222045"
                        }
                    ]
                }
                """.trimIndent()
            }
            else -> null
        }

        if (responseBodyString != null) {
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(responseBodyString.toResponseBody(jsonMediaType))
                .build()
        }

        return chain.proceed(request)
    }
}
