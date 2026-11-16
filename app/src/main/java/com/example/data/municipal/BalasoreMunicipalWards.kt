package com.example.data.municipal

/**
 * Administrative Zones for Balasore Municipality (ବାଲେଶ୍ୱର ପୌରପାଳିକା).
 */
enum class MunicipalZone(val labelEn: String, val labelOd: String) {
    ALL("All Zones (31 Wards)", "ସମସ୍ତ ଜୋନ୍ (୩୧ ୱାର୍ଡ)"),
    CENTRAL("Central Zone", "ମଧ୍ୟ ଜୋନ୍"),
    NORTH("North Zone", "ଉତ୍ତର ଜୋନ୍"),
    SOUTH("South Zone", "ଦକ୍ଷିଣ ଜୋନ୍"),
    EAST("East Zone", "ପୂର୍ବ ଜୋନ୍"),
    WEST("West Zone", "ପଶ୍ଚିମ ଜୋନ୍")
}

/**
 * Representation of an official administrative ward under Balasore Municipality (Wards 1 through 31).
 */
data class BalasoreWard(
    val wardNumber: Int,
    val nameEn: String,
    val nameOd: String,
    val prominentLocalities: String,
    val prominentLocalitiesOd: String,
    val latitude: Double,
    val longitude: Double,
    val pinCode: String = "756001",
    val zone: MunicipalZone,
    val keyLandmarks: List<String>,
    val sanitationSupervisor: String,
    val sanitationSupervisorPhone: String,
    val juniorEngineerPhone: String = "06782-262024",
    val estimatedHouseholds: Int,
    val slaResolutionRatePercent: Int = 85
) {
    val formattedDisplayNameEn: String
        get() = "Ward $wardNumber - $nameEn"

    val formattedDisplayNameOd: String
        get() = "ୱାର୍ଡ $wardNumber - $nameOd"

    val geoAddress: String
        get() = "$nameEn, Ward $wardNumber, Balasore, Odisha $pinCode"
}

/**
 * Official registry of all 31 administrative wards under Balasore Municipality.
 */
object BalasoreMunicipalWards {

    val ALL_WARDS: List<BalasoreWard> = listOf(
        BalasoreWard(
            wardNumber = 1,
            nameEn = "Azimabad",
            nameOd = "ଅଜିମାବାଦ",
            prominentLocalities = "Azimabad, Fakir Mohan College Campus, Old Town West",
            prominentLocalitiesOd = "ଅଜିମାବାଦ, ଫକୀର ମୋହନ କଲେଜ କ୍ୟାମ୍ପସ, ପୁରୁଣା ସହର ପଶ୍ଚିମ",
            latitude = 21.5034,
            longitude = 86.9180,
            pinCode = "756001",
            zone = MunicipalZone.NORTH,
            keyLandmarks = listOf("Fakir Mohan Autonomous College", "Azimabad High School", "FM College Road"),
            sanitationSupervisor = "Prasanta Mohapatra",
            sanitationSupervisorPhone = "+91 94370 21001",
            estimatedHouseholds = 1420,
            slaResolutionRatePercent = 91
        ),
        BalasoreWard(
            wardNumber = 2,
            nameEn = "Fakir Mohan Nagar / Barabati",
            nameOd = "ଫକୀର ମୋହନ ନଗର / ବାରବାଟୀ",
            prominentLocalities = "FM Nagar, Barabati Girls High School Area, Kacheri Road",
            prominentLocalitiesOd = "ଏଫଏମ ନଗର, ବାରବାଟୀ ବାଳିକା ଉଚ୍ଚ ବିଦ୍ୟାଳୟ, କଚେରୀ ରୋଡ୍",
            latitude = 21.5015,
            longitude = 86.9205,
            pinCode = "756001",
            zone = MunicipalZone.CENTRAL,
            keyLandmarks = listOf("Barabati High School", "Fakir Mohan Smruti Peetha", "Kacheri Canal"),
            sanitationSupervisor = "Ranjan Jena",
            sanitationSupervisorPhone = "+91 94370 21002",
            estimatedHouseholds = 1350,
            slaResolutionRatePercent = 88
        ),
        BalasoreWard(
            wardNumber = 3,
            nameEn = "Nuabazar / Jagannath Mandir",
            nameOd = "ନୂଆବଜାର / ଜଗନ୍ନାଥ ମନ୍ଦିର",
            prominentLocalities = "Nuabazar Daily Market, Vegetable Mandi, Jagannath Mandir",
            prominentLocalitiesOd = "ନୂଆବଜାର ଦୈନିକ ହାଟ, ପନିପରିବା ମଣ୍ଡି, ଶ୍ରୀ ଜଗନ୍ନାଥ ମନ୍ଦିର",
            latitude = 21.4985,
            longitude = 86.9220,
            pinCode = "756001",
            zone = MunicipalZone.CENTRAL,
            keyLandmarks = listOf("Nuabazar Sabzi Mandi", "Jagannath Temple Nuabazar", "Old Sub-Jail Road"),
            sanitationSupervisor = "Bijay Kumar Das",
            sanitationSupervisorPhone = "+91 94370 21003",
            estimatedHouseholds = 1580,
            slaResolutionRatePercent = 86
        ),
        BalasoreWard(
            wardNumber = 4,
            nameEn = "Sahadevkhunta",
            nameOd = "ସହଦେବଖୁଣ୍ଟା",
            prominentLocalities = "Sahadevkhunta Central Bus Stand, Police Station Area, NH-16 Link",
            prominentLocalitiesOd = "ସହଦେବଖୁଣ୍ଟା କେନ୍ଦ୍ରୀୟ ବସ୍ ଟର୍ମିନାଲ୍, ଥାନା ଅଞ୍ଚଳ, ଏନଏଚ-୧୬ ସଂଯୋଗ",
            latitude = 21.4981,
            longitude = 86.9240,
            pinCode = "756001",
            zone = MunicipalZone.CENTRAL,
            keyLandmarks = listOf("Balasore Central Bus Stand", "Sahadevkhunta Police Station", "Bus Terminal Complex"),
            sanitationSupervisor = "Manoj Kumar Nayak",
            sanitationSupervisorPhone = "+91 94370 21004",
            estimatedHouseholds = 1750,
            slaResolutionRatePercent = 89
        ),
        BalasoreWard(
            wardNumber = 5,
            nameEn = "Angargadia",
            nameOd = "ଅଙ୍ଗାରଗଡ଼ିଆ",
            prominentLocalities = "Angargadia Main Chowk, Industrial Link Road, Residential Colony",
            prominentLocalitiesOd = "ଅଙ୍ଗାରଗଡ଼ିଆ ମୁଖ୍ୟ ଛକ, ଶିଳ୍ପ ସଂଯୋଗ ରୋଡ୍, ଆବାସିକ କଲୋନୀ",
            latitude = 21.5060,
            longitude = 86.9265,
            pinCode = "756001",
            zone = MunicipalZone.NORTH,
            keyLandmarks = listOf("Angargadia High School", "Panchayat Samiti Office", "Shiva Temple"),
            sanitationSupervisor = "Tapan Kumar Barik",
            sanitationSupervisorPhone = "+91 94370 21005",
            estimatedHouseholds = 1290,
            slaResolutionRatePercent = 84
        ),
        BalasoreWard(
            wardNumber = 6,
            nameEn = "Sovarampur",
            nameOd = "ଶୋଭାରାମପୁର",
            prominentLocalities = "Sovarampur Canal Colony, Municipal Water Works, School Road",
            prominentLocalitiesOd = "ଶୋଭାରାମପୁର କେନାଲ କଲୋନୀ, ଜଳ ଯୋଗାଣ କେନ୍ଦ୍ର, ସ୍କୁଲ ରୋଡ୍",
            latitude = 21.5042,
            longitude = 86.9310,
            pinCode = "756001",
            zone = MunicipalZone.NORTH,
            keyLandmarks = listOf("Municipal Water Pump House", "Sovarampur High School", "Canal Sluice"),
            sanitationSupervisor = "Ganesh Chandra Sahoo",
            sanitationSupervisorPhone = "+91 94370 21006",
            estimatedHouseholds = 1320,
            slaResolutionRatePercent = 83
        ),
        BalasoreWard(
            wardNumber = 7,
            nameEn = "Motiganj",
            nameOd = "ମୋତିଗଞ୍ଜ",
            prominentLocalities = "Motiganj Wholesale Market, Cloth Market, Gold Bazaar",
            prominentLocalitiesOd = "ମୋତିଗଞ୍ଜ ପାଇକାରୀ ବଜାର, ବସ୍ତ୍ର ବଜାର, ସୁନା ହାଟ",
            latitude = 21.4942,
            longitude = 86.9205,
            pinCode = "756001",
            zone = MunicipalZone.CENTRAL,
            keyLandmarks = listOf("Motiganj Market Complex", "Marwari Dharamshala", "Central Bank Chowk"),
            sanitationSupervisor = "Sunil Kumar Behera",
            sanitationSupervisorPhone = "+91 94370 21007",
            estimatedHouseholds = 1860,
            slaResolutionRatePercent = 87
        ),
        BalasoreWard(
            wardNumber = 8,
            nameEn = "Sunhat",
            nameOd = "ସୁନହଟ",
            prominentLocalities = "Sunhat Canal Catchment, Shahi Mohalla, Primary Health Post",
            prominentLocalitiesOd = "ସୁନହଟ କେନାଲ ଅବବାହିକା, ଶାହୀ ମହଲ୍ଲା, ପ୍ରାଥମିକ ସ୍ୱାସ୍ଥ୍ୟ କେନ୍ଦ୍ର",
            latitude = 21.4912,
            longitude = 86.9150,
            pinCode = "756002",
            zone = MunicipalZone.WEST,
            keyLandmarks = listOf("Sunhat Urban Health Center", "Sunhat Juma Masjid", "Canal Drainage Outfall"),
            sanitationSupervisor = "Sheikh Farooq",
            sanitationSupervisorPhone = "+91 94370 21008",
            estimatedHouseholds = 1640,
            slaResolutionRatePercent = 82
        ),
        BalasoreWard(
            wardNumber = 9,
            nameEn = "Patrapada",
            nameOd = "ପାତ୍ରପଡ଼ା",
            prominentLocalities = "Patrapada Bypass Link, Industrial Warehouses, Green Belt",
            prominentLocalitiesOd = "ପାତ୍ରପଡ଼ା ବାଇପାସ୍ ସଂଯୋଗ, ଶିଳ୍ପ ଗୋଦାମ, ଗ୍ରୀନ୍ ବେଲ୍ଟ",
            latitude = 21.4875,
            longitude = 86.9110,
            pinCode = "756002",
            zone = MunicipalZone.WEST,
            keyLandmarks = listOf("Patrapada Playground", "Hanuman Mandir", "Bypass Culvert"),
            sanitationSupervisor = "Subrat Kumar Panda",
            sanitationSupervisorPhone = "+91 94370 21009",
            estimatedHouseholds = 1180,
            slaResolutionRatePercent = 85
        ),
        BalasoreWard(
            wardNumber = 10,
            nameEn = "Mallikashpur",
            nameOd = "ମଲ୍ଲିକାଶପୁର",
            prominentLocalities = "Mallikashpur Residential Area, Govt ITI, River Embankment",
            prominentLocalitiesOd = "ମଲ୍ଲିକାଶପୁର ଆବାସିକ ଅଞ୍ଚଳ, ସରକାରୀ ଆଇଟିଆଇ, ନଦୀ ବନ୍ଧ",
            latitude = 21.4950,
            longitude = 86.9100,
            pinCode = "756002",
            zone = MunicipalZone.WEST,
            keyLandmarks = listOf("Govt ITI Balasore", "Mallikashpur Ghat", "Budhabalanga Embankment"),
            sanitationSupervisor = "Bikash Chandra Giri",
            sanitationSupervisorPhone = "+91 94370 21010",
            estimatedHouseholds = 1270,
            slaResolutionRatePercent = 84
        ),
        BalasoreWard(
            wardNumber = 11,
            nameEn = "Vivekanand Marg / ITI Chhak",
            nameOd = "ବିବେକାନନ୍ଦ ମାର୍ଗ / ଆଇଟିଆଇ ଛକ",
            prominentLocalities = "Vivekanand Marg, ITI Chowk, Technical Training Institute",
            prominentLocalitiesOd = "ବିବେକାନନ୍ଦ ମାର୍ଗ, ଆଇଟିଆଇ ଛକ, ବୈଷୟିକ ପ୍ରଶିକ୍ଷଣ କେନ୍ଦ୍ର",
            latitude = 21.4960,
            longitude = 86.9145,
            pinCode = "756001",
            zone = MunicipalZone.WEST,
            keyLandmarks = listOf("Swami Vivekananda Statue", "ITI Junction", "Public Library Branch"),
            sanitationSupervisor = "Rabindra Nath Biswal",
            sanitationSupervisorPhone = "+91 94370 21011",
            estimatedHouseholds = 1390,
            slaResolutionRatePercent = 88
        ),
        BalasoreWard(
            wardNumber = 12,
            nameEn = "Cinema Chhak / Nua Bazar West",
            nameOd = "ସିନେମା ଛକ / ନୂଆ ବଜାର ପଶ୍ଚିମ",
            prominentLocalities = "Cinema Chhak, Prakash Talkies Road, Main Stormwater Channel",
            prominentLocalitiesOd = "ସିନେମା ଛକ, ପ୍ରକାଶ ଟକିଜ୍ ରୋଡ୍, ମୁଖ୍ୟ ବର୍ଷା ଜଳ ଡ୍ରେନ୍",
            latitude = 21.4910,
            longitude = 86.9265,
            pinCode = "756001",
            zone = MunicipalZone.CENTRAL,
            keyLandmarks = listOf("Cinema Chhak Circle", "Prakash Talkies", "Municipal Commercial Complex"),
            sanitationSupervisor = "Ashok Kumar Sethi",
            sanitationSupervisorPhone = "+91 94370 21012",
            estimatedHouseholds = 1620,
            slaResolutionRatePercent = 86
        ),
        BalasoreWard(
            wardNumber = 13,
            nameEn = "Fakir Mohan Golaye / Town Hall",
            nameOd = "ଫକୀର ମୋହନ ଗୋଲେଇ / ଟାଉନ୍ ହଲ୍",
            prominentLocalities = "FM Golaye Rotary, Balasore Town Hall, District Library",
            prominentLocalitiesOd = "ଏଫଏମ ଗୋଲେଇ ରୋଟାରୀ, ବାଲେଶ୍ୱର ଟାଉନ୍ ହଲ୍, ଜିଲ୍ଲା ପାଠାଗାର",
            latitude = 21.4940,
            longitude = 86.9280,
            pinCode = "756001",
            zone = MunicipalZone.CENTRAL,
            keyLandmarks = listOf("Vyasakabi Fakir Mohan Statue", "Town Hall Balasore", "District Central Library"),
            sanitationSupervisor = "Niranjan Mohanty",
            sanitationSupervisorPhone = "+91 94370 21013",
            estimatedHouseholds = 1480,
            slaResolutionRatePercent = 90
        ),
        BalasoreWard(
            wardNumber = 14,
            nameEn = "OT Road / Phandi Chhak",
            nameOd = "ଓଟି ରୋଡ୍ / ଫାଣ୍ଡି ଛକ",
            prominentLocalities = "Old Trunk Road (OT Road), Phandi Chhak Traffic Post, Commercial Hub",
            prominentLocalitiesOd = "ପୁରୁଣା ଟ୍ରଙ୍କ୍ ରୋଡ୍ (ଓଟି ରୋଡ୍), ଫାଣ୍ଡି ଛକ ଟ୍ରାଫିକ୍ ପୋଷ୍ଟ, ବ୍ୟବସାୟିକ କେନ୍ଦ୍ର",
            latitude = 21.4925,
            longitude = 86.9312,
            pinCode = "756001",
            zone = MunicipalZone.CENTRAL,
            keyLandmarks = listOf("Phandi Chhak Outpost", "OT Road Commercial Strip", "Municipal Ward Office 14"),
            sanitationSupervisor = "Kartik Chandra Swain",
            sanitationSupervisorPhone = "+91 94370 21014",
            estimatedHouseholds = 1820,
            slaResolutionRatePercent = 92
        ),
        BalasoreWard(
            wardNumber = 15,
            nameEn = "Police Line / Jail Road",
            nameOd = "ପୋଲିସ ଲାଇନ୍ / ଜେଲ୍ ରୋଡ୍",
            prominentLocalities = "District Police Headquarters, Reserve Police Grounds, District Jail Road",
            prominentLocalitiesOd = "ଜିଲ୍ଲା ଆରକ୍ଷୀ ମୁଖ୍ୟାଳୟ, ରିଜର୍ଭ ପୋଲିସ ପଡ଼ିଆ, ଜିଲ୍ଲା କାରାଗାର ରୋଡ୍",
            latitude = 21.4900,
            longitude = 86.9330,
            pinCode = "756001",
            zone = MunicipalZone.SOUTH,
            keyLandmarks = listOf("Balasore Reserve Police Line", "Old District Jail Complex", "Police Hospital"),
            sanitationSupervisor = "Bhabani Sankar Rout",
            sanitationSupervisorPhone = "+91 94370 21015",
            estimatedHouseholds = 1310,
            slaResolutionRatePercent = 89
        ),
        BalasoreWard(
            wardNumber = 16,
            nameEn = "Aradabazar",
            nameOd = "ଅରଡ଼ାବଜାର",
            prominentLocalities = "Aradabazar Market, Bateswar Temple Road, Tidal Drain Corridor",
            prominentLocalitiesOd = "ଅରଡ଼ାବଜାର ହାଟ, ବାଟେଶ୍ୱର ମନ୍ଦିର ରୋଡ୍, ଟାଇଡାଲ୍ ଡ୍ରେନ୍ କଲୋନୀ",
            latitude = 21.4880,
            longitude = 86.9280,
            pinCode = "756001",
            zone = MunicipalZone.SOUTH,
            keyLandmarks = listOf("Aradabazar Daily Market", "Bateswar Shiva Temple", "Arada Sluice Gate"),
            sanitationSupervisor = "Siba Prasad Kar",
            sanitationSupervisorPhone = "+91 94370 21016",
            estimatedHouseholds = 1710,
            slaResolutionRatePercent = 84
        ),
        BalasoreWard(
            wardNumber = 17,
            nameEn = "Station Road / Railway Colony",
            nameOd = "ଷ୍ଟେସନ ରୋଡ୍ / ରେଳବାଇ କଲୋନୀ",
            prominentLocalities = "Railway Colony North, DRM Office Road, Railway Health Unit",
            prominentLocalitiesOd = "ରେଳବାଇ କଲୋନୀ ଉତ୍ତର, ଡିଆରଏମ ସଂଯୋଗ, ରେଳ ସ୍ୱାସ୍ଥ୍ୟ କେନ୍ଦ୍ର",
            latitude = 21.4970,
            longitude = 86.9360,
            pinCode = "756001",
            zone = MunicipalZone.EAST,
            keyLandmarks = listOf("SER Railway Colony", "Railway Officers Rest House", "Railway Community Hall"),
            sanitationSupervisor = "Dhaneswar Majhi",
            sanitationSupervisorPhone = "+91 94370 21017",
            estimatedHouseholds = 1450,
            slaResolutionRatePercent = 91
        ),
        BalasoreWard(
            wardNumber = 18,
            nameEn = "Station Square / Somnathpur Road",
            nameOd = "ଷ୍ଟେସନ ସ୍କୋୟାର୍ / ସୋମନାଥପୁର ରୋଡ୍",
            prominentLocalities = "Balasore Railway Station Frontage, Station Bazaar, Auto Stand",
            prominentLocalitiesOd = "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟେସନ ସମ୍ମୁଖ, ଷ୍ଟେସନ ବଜାର, ଅଟୋ ଷ୍ଟାଣ୍ଡ",
            latitude = 21.4990,
            longitude = 86.9350,
            pinCode = "756001",
            zone = MunicipalZone.EAST,
            keyLandmarks = listOf("Balasore Railway Station (BLS)", "Station Circle", "Indian Oil Petroleum Outlet"),
            sanitationSupervisor = "Pradeep Kumar Nanda",
            sanitationSupervisorPhone = "+91 94370 21018",
            estimatedHouseholds = 1680,
            slaResolutionRatePercent = 93
        ),
        BalasoreWard(
            wardNumber = 19,
            nameEn = "Proof Road / Kuruda",
            nameOd = "ପ୍ରୁଫ୍ ରୋଡ୍ / କୁରୁଡ଼ା",
            prominentLocalities = "Proof Road DRDO Approach, Kuruda Bypass, Residential Layouts",
            prominentLocalitiesOd = "ପ୍ରୁଫ୍ ରୋଡ୍ ଡିଆରଡିଓ ସଂଯୋଗ, କୁରୁଡ଼ା ବାଇପାସ୍, ନୂତନ ଆବାସିକ ଅଞ୍ଚଳ",
            latitude = 21.5030,
            longitude = 86.9390,
            pinCode = "756003",
            zone = MunicipalZone.EAST,
            keyLandmarks = listOf("DRDO Proof Road Checkpost", "Kuruda Junction", "Modern Public School"),
            sanitationSupervisor = "Chitta Ranjan Pal",
            sanitationSupervisorPhone = "+91 94370 21019",
            estimatedHouseholds = 1240,
            slaResolutionRatePercent = 87
        ),
        BalasoreWard(
            wardNumber = 20,
            nameEn = "Bampada / Industrial Estate",
            nameOd = "ବାମପଦା / ଶିଳ୍ପାଞ୍ଚଳ",
            prominentLocalities = "Bampada Industrial Area, MSME Hub, Balasore Alloy Link",
            prominentLocalitiesOd = "ବାମପଦା ଶିଳ୍ପାଞ୍ଚଳ, ଏମଏସଏମଇ ହବ୍, ଆଲୟ ସଂଯୋଗ",
            latitude = 21.5080,
            longitude = 86.9420,
            pinCode = "756003",
            zone = MunicipalZone.NORTH,
            keyLandmarks = listOf("Bampada Industrial Estate Gate", "NOCCI Business Park Link", "Transformer Substation"),
            sanitationSupervisor = "Kailash Chandra Patra",
            sanitationSupervisorPhone = "+91 94370 21020",
            estimatedHouseholds = 1150,
            slaResolutionRatePercent = 85
        ),
        BalasoreWard(
            wardNumber = 21,
            nameEn = "Kasafal Road / Gudipada",
            nameOd = "କାସାଫଳ ରୋଡ୍ / ଗୁଡ଼ିପଦା",
            prominentLocalities = "Gudipada, Kasafal Coastal Approach, Fish Merchant Quarter",
            prominentLocalitiesOd = "ଗୁଡ଼ିପଦା, କାସାଫଳ ଉପକୂଳ ରାସ୍ତା, ମତ୍ସ୍ୟ ବ୍ୟବସାୟୀ କଲୋନୀ",
            latitude = 21.4920,
            longitude = 86.9380,
            pinCode = "756003",
            zone = MunicipalZone.EAST,
            keyLandmarks = listOf("Gudipada Community Center", "Coastal Highway Link", "Jagannath High School"),
            sanitationSupervisor = "Hemanta Kumar Barik",
            sanitationSupervisorPhone = "+91 94370 21021",
            estimatedHouseholds = 1380,
            slaResolutionRatePercent = 83
        ),
        BalasoreWard(
            wardNumber = 22,
            nameEn = "Gopalgaon Town / Damodarpur",
            nameOd = "ଗୋପାଳଗାଁ ଟାଉନ୍ / ଦାମୋଦରପୁର",
            prominentLocalities = "Gopalgaon Town, Damodarpur Embankment, Primary School",
            prominentLocalitiesOd = "ଗୋପାଳଗାଁ ଟାଉନ୍, ଦାମୋଦରପୁର ନଦୀ ବନ୍ଧ, ପ୍ରାଥମିକ ବିଦ୍ୟାଳୟ",
            latitude = 21.4880,
            longitude = 86.9120,
            pinCode = "756001",
            zone = MunicipalZone.SOUTH,
            keyLandmarks = listOf("Gopalgaon High School", "Damodarpur River Ghat", "Bata Mangala Temple"),
            sanitationSupervisor = "Laxmidhar Rout",
            sanitationSupervisorPhone = "+91 94370 21022",
            estimatedHouseholds = 1460,
            slaResolutionRatePercent = 86
        ),
        BalasoreWard(
            wardNumber = 23,
            nameEn = "Remuna Golei / NH Junction",
            nameOd = "ରେମୁଣା ଗୋଲେଇ / ଏନଏଚ୍ ଛକ",
            prominentLocalities = "Remuna Golei Overbridge, NH-16 Balasore Bypass, Transport Yard",
            prominentLocalitiesOd = "ରେମୁଣା ଗୋଲେଇ ଓଭରବ୍ରିଜ୍, ଏନଏଚ୍-୧୬ ବାଇପାସ୍, ପରିବହନ ୟାର୍ଡ",
            latitude = 21.5120,
            longitude = 86.9200,
            pinCode = "756019",
            zone = MunicipalZone.NORTH,
            keyLandmarks = listOf("Remuna Golei Flyover", "Khirachora Gopinath Temple Road", "Highway Rest Stop"),
            sanitationSupervisor = "Deepak Kumar Biswal",
            sanitationSupervisorPhone = "+91 94370 21023",
            estimatedHouseholds = 1520,
            slaResolutionRatePercent = 88
        ),
        BalasoreWard(
            wardNumber = 24,
            nameEn = "Chhanpur / Collectorate Road",
            nameOd = "ଛାନପୁର / କଲେକ୍ଟରେଟ୍ ରୋଡ୍",
            prominentLocalities = "Chhanpur, District Collectorate Campus, DRDA Office",
            prominentLocalitiesOd = "ଛାନପୁର, ଜିଲ୍ଲାପାଳଙ୍କ କାର୍ଯ୍ୟାଳୟ ପରିସର, ଡିଆରଡିଏ ଅଫିସ୍",
            latitude = 21.4965,
            longitude = 86.9300,
            pinCode = "756001",
            zone = MunicipalZone.CENTRAL,
            keyLandmarks = listOf("Balasore District Collectorate", "DRDA Building", "SP Office Compound"),
            sanitationSupervisor = "Jayanta Kumar Mohanty",
            sanitationSupervisorPhone = "+91 94370 21024",
            estimatedHouseholds = 1410,
            slaResolutionRatePercent = 94
        ),
        BalasoreWard(
            wardNumber = 25,
            nameEn = "Manikham / Bhoisahi",
            nameOd = "ମାଣିକଖାମ୍ / ଭୋଇସାହି",
            prominentLocalities = "Manikham, Bhoisahi Weaver Cluster, Old Town South",
            prominentLocalitiesOd = "ମାଣିକଖାମ୍, ଭୋଇସାହି ବୁଣାକାର ବସ୍ତି, ପୁରୁଣା ସହର ଦକ୍ଷିଣ",
            latitude = 21.4860,
            longitude = 86.9180,
            pinCode = "756001",
            zone = MunicipalZone.SOUTH,
            keyLandmarks = listOf("Manikham Shiv Mandir", "Weavers Cooperative Society", "Bhoisahi Primary School"),
            sanitationSupervisor = "Surendra Nath Jena",
            sanitationSupervisorPhone = "+91 94370 21025",
            estimatedHouseholds = 1390,
            slaResolutionRatePercent = 85
        ),
        BalasoreWard(
            wardNumber = 26,
            nameEn = "Balisahi / Budhabalanga Ghat",
            nameOd = "ବାଲିସାହି / ବୁଢ଼ାବଳଙ୍ଗ ଘାଟ",
            prominentLocalities = "Balisahi, Budhabalanga River Ghat, Old Dutch Cemetery Link",
            prominentLocalitiesOd = "ବାଲିସାହି, ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ ଘାଟ, ଡଚ୍ ଶ୍ମଶାନ ସଂଯୋଗ",
            latitude = 21.4850,
            longitude = 86.9210,
            pinCode = "756001",
            zone = MunicipalZone.SOUTH,
            keyLandmarks = listOf("Balisahi River Ghat", "Colonial Dutch Memorial", "Old Port Embankment"),
            sanitationSupervisor = "Tarun Kumar Das",
            sanitationSupervisorPhone = "+91 94370 21026",
            estimatedHouseholds = 1530,
            slaResolutionRatePercent = 86
        ),
        BalasoreWard(
            wardNumber = 27,
            nameEn = "Puruna Baleshwar / Jagannath Temple",
            nameOd = "ପୁରୁଣା ବାଲେଶ୍ୱର / ଶ୍ରୀ ଜଗନ୍ନାଥ ମନ୍ଦିର",
            prominentLocalities = "Puruna Baleshwar, Ancient Jagannath Temple, Heritage Quarter",
            prominentLocalitiesOd = "ପୁରୁଣା ବାଲେଶ୍ୱର, ପ୍ରାଚୀନ ଶ୍ରୀ ଜଗନ୍ନାଥ ମନ୍ଦିର, ଐତିହ୍ୟ ଅଞ୍ଚଳ",
            latitude = 21.4820,
            longitude = 86.9240,
            pinCode = "756001",
            zone = MunicipalZone.SOUTH,
            keyLandmarks = listOf("Ancient Jagannath Temple Purunabaleswar", "Puruna Baleswar Tank", "Gargaria River Canal"),
            sanitationSupervisor = "Pitambar Behera",
            sanitationSupervisorPhone = "+91 94370 21027",
            estimatedHouseholds = 1600,
            slaResolutionRatePercent = 88
        ),
        BalasoreWard(
            wardNumber = 28,
            nameEn = "Khannagar / Mathasahi",
            nameOd = "ଖାନ୍‌ନଗର / ମଠସାହି",
            prominentLocalities = "Khannagar, Mathasahi, Ashram School Road",
            prominentLocalitiesOd = "ଖାନ୍‌ନଗର, ମଠସାହି, ଆଶ୍ରମ ବିଦ୍ୟାଳୟ ରୋଡ୍",
            latitude = 21.4800,
            longitude = 86.9290,
            pinCode = "756001",
            zone = MunicipalZone.SOUTH,
            keyLandmarks = listOf("Khannagar Shanti Matha", "Ashram School", "Municipal Solid Waste Transfer Station"),
            sanitationSupervisor = "Ajay Kumar Nayak",
            sanitationSupervisorPhone = "+91 94370 21028",
            estimatedHouseholds = 1340,
            slaResolutionRatePercent = 85
        ),
        BalasoreWard(
            wardNumber = 29,
            nameEn = "Telenga Sahi / Barabati",
            nameOd = "ତେଲେଙ୍ଗା ସାହି / ବାରବାଟୀ",
            prominentLocalities = "Telenga Sahi, Barabati South, Old French Loge Boundary",
            prominentLocalitiesOd = "ତେଲେଙ୍ଗା ସାହି, ବାରବାଟୀ ଦକ୍ଷିଣ, ପ୍ରାଚୀନ ଫ୍ରେଞ୍ଚ୍ ଲୋଜ୍ ସୀମା",
            latitude = 21.4840,
            longitude = 86.9320,
            pinCode = "756001",
            zone = MunicipalZone.SOUTH,
            keyLandmarks = listOf("French Loge Heritage Monument", "Telenga Sahi Mandir", "Barabati Canal Culvert"),
            sanitationSupervisor = "Suresh Chandra Panda",
            sanitationSupervisorPhone = "+91 94370 21029",
            estimatedHouseholds = 1290,
            slaResolutionRatePercent = 87
        ),
        BalasoreWard(
            wardNumber = 30,
            nameEn = "Rani Patna / Kalidaspur",
            nameOd = "ରାଣୀ ପାଟଣା / କାଳିଦାସପୁର",
            prominentLocalities = "Rani Patna, Kalidaspur Link, New Railway Overbridge",
            prominentLocalitiesOd = "ରାଣୀ ପାଟଣା, କାଳିଦାସପୁର ସଂଯୋଗ, ନୂତନ ରେଳ ଓଭରବ୍ରିଜ୍",
            latitude = 21.4930,
            longitude = 86.9420,
            pinCode = "756003",
            zone = MunicipalZone.EAST,
            keyLandmarks = listOf("Rani Patna Overbridge", "Kalidaspur High School", "Railway Siding Yard"),
            sanitationSupervisor = "Maheswar Barik",
            sanitationSupervisorPhone = "+91 94370 21030",
            estimatedHouseholds = 1570,
            slaResolutionRatePercent = 89
        ),
        BalasoreWard(
            wardNumber = 31,
            nameEn = "Srikrushnapur / Tamulia Bypass",
            nameOd = "ଶ୍ରୀକୃଷ୍ଣପୁର / ତମୁଳିଆ ବାଇପାସ୍",
            prominentLocalities = "Srikrushnapur, Tamulia Bypass Junction, Chandipur Road Link",
            prominentLocalitiesOd = "ଶ୍ରୀକୃଷ୍ଣପୁର, ତମୁଳିଆ ବାଇପାସ୍ ଛକ, ଚାନ୍ଦିପୁର ରୋଡ୍ ସଂଯୋଗ",
            latitude = 21.4950,
            longitude = 86.9480,
            pinCode = "756003",
            zone = MunicipalZone.EAST,
            keyLandmarks = listOf("Tamulia Gate Junction", "Chandipur Defense Corridor Link", "Srikrushnapur Shiva Temple"),
            sanitationSupervisor = "Satyanarayan Das",
            sanitationSupervisorPhone = "+91 94370 21031",
            estimatedHouseholds = 1430,
            slaResolutionRatePercent = 90
        )
    )

    /**
     * Suburban and regional administrative divisions adjoining Balasore Municipality.
     */
    val SUBURBAN_DIVISIONS = listOf(
        "Remuna NAC (Khirachora Gopinath)",
        "Soro Municipality",
        "Jaleswar Town Municipality",
        "Nilagiri NAC (Sub-Division)"
    )

    fun getAllWards(): List<BalasoreWard> = ALL_WARDS

    fun getWardByNumber(number: Int): BalasoreWard? {
        return ALL_WARDS.find { it.wardNumber == number }
    }

    /**
     * Formatted string list for Ward pickers and dropdowns covering all 31 wards plus adjoining blocks.
     */
    fun getAllWardDisplayNames(): List<String> {
        val municipalList = ALL_WARDS.map { it.formattedDisplayNameEn }
        return municipalList + SUBURBAN_DIVISIONS
    }

    /**
     * Geographic coordinate mapping for all 31 municipal wards and suburban blocks.
     */
    fun getWardGeoCoordinatesMap(): Map<String, Triple<Double, Double, String>> {
        val map = mutableMapOf<String, Triple<Double, Double, String>>()
        for (w in ALL_WARDS) {
            map[w.formattedDisplayNameEn] = Triple(w.latitude, w.longitude, w.geoAddress)
            // Also map shorthand
            map["Ward ${w.wardNumber}"] = Triple(w.latitude, w.longitude, w.geoAddress)
        }
        // Suburb mappings
        map["Remuna NAC (Khirachora Gopinath)"] = Triple(21.5280, 86.8720, "Remuna NAC, Khirachora Gopinath Area, Balasore")
        map["Remuna Block"] = Triple(21.5280, 86.8720, "Remuna NAC, Khirachora Gopinath Area, Balasore")
        map["Soro Municipality"] = Triple(21.2910, 86.6900, "Soro Town Main Market, Balasore")
        map["Jaleswar Town Municipality"] = Triple(21.8020, 87.2140, "Jaleswar Station Bazaar, Balasore")
        map["Jaleswar Town"] = Triple(21.8020, 87.2140, "Jaleswar Station Bazaar, Balasore")
        map["Nilagiri NAC (Sub-Division)"] = Triple(21.4600, 86.7650, "Nilagiri Sub-Divisional Road, Balasore")
        map["Nilagiri NAC"] = Triple(21.4600, 86.7650, "Nilagiri Sub-Divisional Road, Balasore")
        return map
    }
}
