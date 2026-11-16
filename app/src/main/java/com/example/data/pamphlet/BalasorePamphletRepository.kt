package com.example.data.pamphlet

/**
 * Repository containing curated and verified official Balasore tourism and emergency pamphlets.
 * Matches QR code scan payloads from pamphlets distributed at Balasore tourist counters,
 * railway stations, bus stands, and district administrative offices.
 */
object BalasorePamphletRepository {

    val OFFICIAL_PAMPHLETS: List<BalasorePamphlet> = listOf(
        BalasorePamphlet(
            id = "chandipur_beach_safety",
            titleEn = "Chandipur Beach Vanishing Sea & Tidal Safety Manual",
            titleOr = "ଚାନ୍ଦିପୁର ସମୁଦ୍ରକୂଳ ଓ ଜୁଆର-ଭଟ୍ଟା ସୁରକ୍ଷା ନିର୍ଦ୍ଦେଶାବଳୀ",
            category = PamphletCategory.TOURISM,
            issuingAuthorityEn = "Odisha Tourism & District Administration, Balasore",
            issuingAuthorityOr = "ଓଡ଼ିଶା ପର୍ଯ୍ୟଟନ ଓ ବାଲେଶ୍ୱର ଜିଲ୍ଲା ପ୍ରଶାସନ",
            summaryEn = "Official safety advisory explaining the unique 1-5 km low-tide phenomenon, safe walking zones, and sudden incoming tide precautions at Chandipur-on-Sea.",
            summaryOr = "ଚାନ୍ଦିପୁର ସମୁଦ୍ରକୂଳରେ ଭଟ୍ଟା ସମୟରେ ୧ ରୁ ୫ କିମି ପର୍ଯ୍ୟନ୍ତ ସମୁଦ୍ର ପଛକୁ ଯିବା ଏବଂ ଜୁଆର ଆସିବା ସମୟରେ ନିରାପତ୍ତା ନିୟମାବଳୀ।",
            fullContentEn = """
                Welcome to Chandipur-on-Sea, one of the world's most unique beaches where seawater recedes up to 5 kilometers during low tide twice a day!

                • TIDE SCHEDULE AWARENESS:
                Always consult the official Baleshwar 360 tide tracker before walking deep into the exposed seabed. Incoming tides advance faster than typical walking pace.

                • SAFE WALKING PROTOCOL:
                1. Never venture beyond 1.5 km without a local beach guide.
                2. Return to the dune line at least 45 minutes before projected High Tide.
                3. Avoid quicksand depressions near the river confluence (Budhabalanga estuary).
                4. Horseshoe crabs and red crabs inhabit the mudflats; please observe without handling.
                5. Quad biking and personal vehicular driving on the sea mud bed are strictly prohibited by order of the District Magistrate.

                • EMERGENCY LIFEGUARD POINTS:
                Lifeguards are deployed near Main Beach Watchtower and Panthanivas front.
            """.trimIndent(),
            fullContentOr = """
                ଚାନ୍ଦିପୁର ବେଳାଭୂମିକୁ ସ୍ୱାଗତ! ଏଠାରେ ଦିନକୁ ଦୁଇଥର ସମୁଦ୍ର ଜଳ ପ୍ରାୟ ୫ କିଲୋମିଟର ପଛକୁ ଚାଲିଯାଏ।

                • ଜୁଆର-ଭଟ୍ଟା ସମୟ ସଚେତନତା:
                ସମୁଦ୍ର ମଧ୍ୟକୁ ଯିବା ପୂର୍ବରୁ ଆପ୍ଲିକେସନରେ ଜୁଆର ସମୟ ଯାଞ୍ଚ କରନ୍ତୁ। ଜୁଆର ଆସିବା ସମୟରେ ପାଣି ଦ୍ରୁତ ଗତିରେ ମାଡ଼ିଆସେ।

                • ନିରାପତ୍ତା ନିୟମ:
                ୧. ୧.୫ କିମିରୁ ଅଧିକ ଦୂରକୁ ଯାଆନ୍ତୁ ନାହିଁ।
                ୨. ପୂର୍ଣ୍ଣ ଜୁଆର ଆସିବାର ୪୫ ମିନିଟ ପୂର୍ବରୁ କୂଳକୁ ଫେରିଆସନ୍ତୁ।
                ୩. ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ ମୁହାଣ ନିକଟରେ ଚୋରାବାଲି ଥାଏ, ସେଠାକୁ ଯାଆନ୍ତୁ ନାହିଁ।
                ୪. ଜୀବଜନ୍ତୁଙ୍କୁ କ୍ଷତି ପହଞ୍ଚାନ୍ତୁ ନାହିଁ।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Monitor Baleshwar 360 live tide clock",
                "Return 45 mins before high tide",
                "Do not drive motor vehicles on sea mud",
                "Beach Lifeguard post near Main Watchtower"
            ),
            keyDirectivesOr = listOf(
                "ଲାଇଭ୍ ଜୁଆର ଘଣ୍ଟା ଯାଞ୍ଚ କରନ୍ତୁ",
                "ଜୁଆର ଆସିବାର ୪୫ ମିନିଟ ପୂର୍ବରୁ ଫେରନ୍ତୁ",
                "ସମୁଦ୍ର କାଦୁଅରେ ଗାଡ଼ି ଚଳାନ୍ତୁ ନାହିଁ",
                "ମୁଖ୍ୟ ୱାଚ୍ ଟାୱାର ନିକଟରେ ଲାଇଫଗାର୍ଡ ମୁତୟନ"
            ),
            emergencyContactNumbers = listOf("06782-270054", "112", "9438916480"),
            officialHotlineLabel = "Chandipur Marine Police & Lifeguards",
            latitude = 21.4682,
            longitude = 87.0163,
            locationName = "Chandipur Sea Beach, Balasore",
            qrPayload = "balasore360://pamphlet/chandipur_beach_safety",
            officialReferenceNumber = "ODT-BLS-2026-CH01"
        ),
        BalasorePamphlet(
            id = "balasore_disaster_odraf_sop",
            titleEn = "Balasore DDMA Cyclone, Flood & Coastal Disaster Protocol",
            titleOr = "ବାଲେଶ୍ୱର ଜିଲ୍ଲା ବିପର୍ଯ୍ୟୟ ପରିଚାଳନା ଓ ବାତ୍ୟା ସୁରକ୍ଷା ନିର୍ଦ୍ଦେଶାବଳୀ",
            category = PamphletCategory.EMERGENCY,
            issuingAuthorityEn = "Balasore District Disaster Management Authority (DDMA) & ODRAF",
            issuingAuthorityOr = "ବାଲେଶ୍ୱର ଜିଲ୍ଲା ବିପର୍ଯ୍ୟୟ ପରିଚାଳନା ପ୍ରାଧିକରଣ ଓ ଓଡ୍ରାଫ୍ (ODRAF)",
            summaryEn = "Official contingency action plan for cyclones, coastal storm surges, and Budhabalanga river flooding in Balasore district.",
            summaryOr = "ବାଲେଶ୍ୱର ଉପକୂଳରେ ବାତ୍ୟା, ବନ୍ୟା ଓ ସାମୁଦ୍ରିକ ଜୁଆର ସମୟରେ ଜନସାଧାରଣଙ୍କ ସୁରକ୍ଷା ଓ ଆଶ୍ରୟସ୍ଥଳୀ ସୂଚନା।",
            fullContentEn = """
                BALASORE DISTRICT EMERGENCY OPERATIONS CENTRE (DEOC):
                Toll Free: 1077 | Landline: 06782-262674 | Mobile: 9437022674

                • CYCLONE ALERT LEVELS:
                - Yellow (Watch): Coastal preparation 48h in advance.
                - Orange (Warning): Evacuation of low-lying coastal villages within 5 km of coast (Chandipur, Bahanaga, Baliapal, Bhograi).
                - Red (Landfall Danger): Stay inside designated Multipurpose Cyclone Shelters (MPCS).

                • EMERGENCY EVACUATION CENTRES:
                Over 120 concrete Multipurpose Cyclone Shelters operate across Balasore with uninterrupted solar lighting, drinking water filtration, and community kitchens.

                • FLOOD WATCH ON BUDHABALANGA & JALAKA RIVERS:
                River gauge stations at Mathani and Balasore Town monitor danger levels continuously. In case of breach alert, move to high ground immediately.

                • EMERGENCY CONTACTS:
                ODRAF 4th Battalion: 06782-256024
                Fire & Rescue Control: 101 / 06782-262101
                Police Control Room: 112 / 06782-262222
            """.trimIndent(),
            fullContentOr = """
                ବାଲେଶ୍ୱର ଜିଲ୍ଲା ଜରୁରୀକାଳୀନ କାର୍ଯ୍ୟାଳୟ (DEOC):
                ଟୋଲ ଫ୍ରି: ୧୦୭୭ | ଫୋନ: ୦୬୭୮୨-୨୬୨୬୭୪

                • ବାତ୍ୟା ସତର୍କତା ସ୍ତର:
                - ହଳଦିଆ ସତର୍କତା: ୪୮ ଘଣ୍ଟା ପୂର୍ବରୁ ପ୍ରସ୍ତୁତି
                - କମଳା ସତର୍କତା: ଉପକୂଳବର୍ତ୍ତୀ ଲୋକଙ୍କୁ ସ୍ଥାନାନ୍ତରଣ
                - ଲାଲ ସତର୍କତା: ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀରେ ରୁହନ୍ତୁ

                • ବୁଢ଼ାବଳଙ୍ଗ ଓ ଜଳକା ନଦୀ ବନ୍ୟା ସତର୍କତା:
                ମଥାନୀ ଓ ବାଲେଶ୍ୱର ଠାରେ ବିପଦ ସଂକେତ ନିରୀକ୍ଷଣ କରାଯାଉଛି।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "District DEOC Toll-Free: 1077",
                "Move to Multipurpose Cyclone Shelter (MPCS) on Red Alert",
                "Stock 3-day drinking water, dry rations & torch",
                "Listen to official Baleshwar 360 alerts; ignore rumors"
            ),
            keyDirectivesOr = listOf(
                "ଜିଲ୍ଲା କଣ୍ଟ୍ରୋଲ ରୁମ ଟୋଲ ଫ୍ରି: ୧୦୭୭",
                "ଲାଲ ସତର୍କତାରେ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀକୁ ଯାଆନ୍ତୁ",
                "୩ ଦିନର ପାନୀୟ ଜଳ ଓ ଶୁଖିଲା ଖାଦ୍ୟ ମହଜୁଦ ରଖନ୍ତୁ",
                "ଗୁଜବରେ ବିଶ୍ୱାସ କରନ୍ତୁ ନାହିଁ"
            ),
            emergencyContactNumbers = listOf("1077", "06782-262674", "101", "112"),
            officialHotlineLabel = "District Emergency Operation Centre (DEOC)",
            latitude = 21.4934,
            longitude = 86.9332,
            locationName = "Collectorate Campus, Balasore",
            qrPayload = "balasore360://pamphlet/balasore_disaster_odraf_sop",
            officialReferenceNumber = "DDMA-BLS-SOP-2026-09"
        ),
        BalasorePamphlet(
            id = "kuldiha_wildlife_sanctuary",
            titleEn = "Kuldiha Wildlife Sanctuary Eco-Tourism & Forest Safaris",
            titleOr = "କୁଲଡିହା ବନ୍ୟପ୍ରାଣୀ ଅଭୟାରଣ୍ୟ ଇକୋ-ଟୁରିଜିମ୍ ଓ ସଫାରୀ ଗାଇଡ୍",
            category = PamphletCategory.TOURISM,
            issuingAuthorityEn = "Balasore Wildlife Division, Odisha Forest Dept",
            issuingAuthorityOr = "ବାଲେଶ୍ୱର ବନ୍ୟପ୍ରାଣୀ ବନଖଣ୍ଡ, ଓଡ଼ିଶା ବନ ବିଭାଗ",
            summaryEn = "Official rules for elephant corridor safaris, Gohirabhola salt-lick viewing, birdwatching, and night camping permits at Kuldiha.",
            summaryOr = "କୁଲଡିହା ଅଭୟାରଣ୍ୟରେ ହାତୀ ଦର୍ଶନ, ଗୋହିରାଭୋଳା ୱାଚ୍ ଟାୱାର, ପକ୍ଷୀ ନିରୀକ୍ଷଣ ଓ ଟେଣ୍ଟ ରହଣି ନିୟମ।",
            fullContentEn = """
                Kuldiha Wildlife Sanctuary covers 272.75 sq. km of dense sal forests connected to Similipal Biosphere Reserve.

                • ENTRY GATES & TIMINGS:
                Entry Gate: Risa (near Nilagiri).
                Timings: 06:00 AM to 04:00 PM (Winter) | 06:00 AM to 05:00 PM (Summer).
                Closed during Monsoon (June 16 to October 31).

                • CODE OF CONDUCT:
                1. Only registered 4x4 safari vehicles accompanied by certified forest trackers are permitted.
                2. Maximum speed limit inside sanctuary: 20 km/h. Strictly no horn or loud music.
                3. Single-use plastic bottles and plastic snack wrappers are strictly banned.
                4. Maintain a minimum 50-meter distance from wild elephant herds at all times.
                5. Smoking, open fires, and cooking inside the core forest are criminal offenses.

                • NIGHT STAY BOOKING:
                Eco-cottages at Gohirabhola and Jadachua can be reserved via www.ecotourodisha.com.
            """.trimIndent(),
            fullContentOr = """
                କୁଲଡିହା ବନ୍ୟପ୍ରାଣୀ ଅଭୟାରଣ୍ୟ (୨୭୨.୭୫ ବର୍ଗ କିମି) ଶିମିଳିପାଳ ସହ ସଂଯୁକ୍ତ।

                • ପ୍ରବେଶ ସମୟ:
                ସକାଳ ୬:୦୦ ରୁ ଅପରାହ୍ନ ୪:୦୦ (ଶୀତଦିନ) | ନୀଳଗିରି ରିସା ଗେଟ୍ ଦେଇ ପ୍ରବେଶ।

                • ନିୟମାବଳୀ:
                ୧. ପଞ୍ଜୀକୃତ ଜିପ୍ ଓ ଗାଇଡ୍ ସହ ପ୍ରବେଶ କରନ୍ତୁ।
                ୨. ଗତି ସୀମା ୨୦ କିମି/ଘଣ୍ଟା; ହର୍ଣ୍ଣ ବଜାନ୍ତୁ ନାହିଁ।
                ୩. ପ୍ଲାଷ୍ଟିକ ବ୍ୟବହାର ସମ୍ପୂର୍ଣ୍ଣ ନିଷିଦ୍ଧ।
                ୪. ହାତୀପଲଙ୍କ ଠାରୁ ଅତିକମରେ ୫୦ ମିଟର ଦୂରତା ରକ୍ଷା କରନ୍ତୁ।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Entry via Risa Gate near Nilagiri only",
                "Speed limit 20 km/h; zero horn policy",
                "Maintain 50m distance from wild elephants",
                "Single-use plastics strictly banned"
            ),
            keyDirectivesOr = listOf(
                "ନୀଳଗିରି ରିସା ଗେଟ୍ ଦେଇ ପ୍ରବେଶ କରନ୍ତୁ",
                "ଗାଡ଼ି ଗତିସୀମା ୨୦ କିମି; ହର୍ଣ୍ଣ ବଜାଇବା ମନା",
                "ହାତୀଙ୍କ ଠାରୁ ୫୦ ମିଟର ଦୂରରେ ରୁହନ୍ତୁ",
                "ପ୍ଲାଷ୍ଟିକ ସମ୍ପୂର୍ଣ୍ଣ ନିଷିଦ୍ଧ"
            ),
            emergencyContactNumbers = listOf("06782-262148", "9437298112"),
            officialHotlineLabel = "Kuldiha Forest Range Officer",
            latitude = 21.4167,
            longitude = 86.6833,
            locationName = "Kuldiha Forest Range, Nilagiri, Balasore",
            qrPayload = "balasore360://pamphlet/kuldiha_wildlife_sanctuary",
            officialReferenceNumber = "OFD-KWS-BLS-2026-04"
        ),
        BalasorePamphlet(
            id = "remuna_khirachora_gopinath",
            titleEn = "Remuna Khirachora Gopinath Temple Pilgrims' Heritage Guide",
            titleOr = "ରେମୁଣା ଖିରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର ତୀର୍ଥଯାତ୍ରୀ ନିର୍ଦ୍ଦେଶିକା",
            category = PamphletCategory.TOURISM,
            issuingAuthorityEn = "Odisha Endowments & Remuna Temple Trust Board",
            issuingAuthorityOr = "ଓଡ଼ିଶା ଦେବୋତ୍ତର ବିଭାଗ ଓ ରେମୁଣା ମନ୍ଦିର ଟ୍ରଷ୍ଟ ବୋର୍ଡ",
            summaryEn = "Pilgrimage etiquette, daily bhog timings (Amruta Keli Khira), historic significance of Sri Chaitanya Mahaprabhu & Madhavendra Puri at Remuna.",
            summaryOr = "ଖିରଚୋରା ଗୋପୀନାଥଙ୍କ ଦୈନିକ ଖିରି ଭୋଗ (ଅମୃତ କେଳି) ସମୟ, ମାଧବେନ୍ଦ୍ର ପୁରୀ ଓ ଶ୍ରୀ ଚୈତନ୍ୟଙ୍କ ଲୀଳାସ୍ଥଳୀ ଦର୍ଶନ ସୂଚନା।",
            fullContentEn = """
                Sri Khirachora Gopinath Temple at Remuna is a 12th-century shrine constructed by King Langula Narasingha Deva of the Eastern Ganga dynasty.

                • FAMOUS KHIRA BHOG (AMRUTA KELI):
                Offered daily in small clay pots. Available to pilgrims after Madhyanha Dhoopa (12:30 PM - 02:00 PM) and Sandhya Aarati (07:30 PM - 08:30 PM).

                • IMPORTANT PILGRIMAGE SITES ON-SITE:
                1. Main Shrine of Sri Gopinath, Govinda, and Madana Mohana.
                2. Sri Madhavendra Puri Samadhi Mandir & Kanak Stupa.
                3. Gaudiya Matha established by Srila Bhaktisiddhanta Sarasvati Thakura.
                4. Gargeswara Mahadeva Temple.

                • TEMPLE ETIQUETTE:
                Traditional Indian dress recommended. Footwear repository located outside the Singhadwara. Photography inside the sanctum sanctorum is forbidden.
            """.trimIndent(),
            fullContentOr = """
                ଦ୍ୱାଦଶ ଶତାବ୍ଦୀର ଐତିହାସିକ ରେମୁଣା ଖିରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର।

                • ବିଶ୍ୱପ୍ରସିଦ୍ଧ ଖିରି ଭୋଗ (ଅମୃତ କେଳି):
                ମଧ୍ୟାହ୍ନ ଧୂପ (୧୨:୩୦ - ୨:୦୦) ଏବଂ ସନ୍ଧ୍ୟା ଆଳତି (୭:୩୦ - ୮:୩୦) ପରେ ମିଳିଥାଏ।

                • ଦର୍ଶନୀୟ ସ୍ଥାନ:
                ମାଧବେନ୍ଦ୍ର ପୁରୀ ସମାଧି, ଗୌଡ଼ୀୟ ମଠ, ଗାର୍ଗେଶ୍ୱର ମହାଦେବ।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Khira Bhog tokens available at Office Counter",
                "Sanctum opens 06:00 AM to 12:30 PM & 04:00 PM to 08:30 PM",
                "Mobile photography forbidden inside sanctum",
                "Remuna is 9 km from Balasore Railway Station"
            ),
            keyDirectivesOr = listOf(
                "କାଉଣ୍ଟରରୁ ଖିରି ଟୋକନ ସଂଗ୍ରହ କରନ୍ତୁ",
                "ଦର୍ଶନ ସମୟ: ସକାଳ ୬:୦୦ - ୧୨:୩୦ ଓ ଅପରାହ୍ନ ୪:୦୦ - ୮:୩୦",
                "ଗର୍ଭଗୃହରେ ଫଟୋ ଉଠାଇବା ନିଷେଧ",
                "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟେସନ ଠାରୁ ରେମୁଣା ୯ କିମି"
            ),
            emergencyContactNumbers = listOf("06782-252724", "9437142820"),
            officialHotlineLabel = "Remuna Temple Trust & Info Desk",
            latitude = 21.5273,
            longitude = 86.8719,
            locationName = "Remuna, Balasore District",
            qrPayload = "balasore360://pamphlet/remuna_khirachora_gopinath",
            officialReferenceNumber = "OEB-REM-GOP-2026-11"
        ),
        BalasorePamphlet(
            id = "fm_medical_college_emergency",
            titleEn = "Fakir Mohan Medical College & Hospital (FM MCH) Emergency Directory",
            titleOr = "ଫକୀର ମୋହନ ମେଡିକାଲ କଲେଜ ଓ ହସ୍ପିଟାଲ ଜରୁରୀକାଳୀନ ସେବା ନିର୍ଦ୍ଦେଶିକା",
            category = PamphletCategory.HEALTHCARE,
            issuingAuthorityEn = "Fakir Mohan Medical College & Hospital & Health Dept, Odisha",
            issuingAuthorityOr = "ଫକୀର ମୋହନ ଭେଷଜ ମହାବିଦ୍ୟାଳୟ ଓ ଚିକିତ୍ସାଳୟ, ବାଲେଶ୍ୱର",
            summaryEn = "24x7 Trauma care protocols, blood bank availability, snakebite venom treatment, pediatric ICU, and Ayushman Odisha cashless services.",
            summaryOr = "୨୪x୭ ଟ୍ରମା କେୟାର, ରକ୍ତ ଭଣ୍ଡାର, ସର୍ପାଘାତ ଚିକିତ୍ସା, ଶିଶୁ ଆଇସିୟୁ ଓ ମାଗଣା ଚିକିତ୍ସା ପଞ୍ଜୀକରଣ।",
            fullContentEn = """
                FM MCH BALASORE 24x7 CASUALTY & TRAUMA CENTER:
                Location: Remuna Golanthara Campus, Balasore | Emergency Hotline: 06782-224401

                • 24x7 SPECIALIZED SERVICES:
                1. Level-2 Trauma & Accident Emergency Unit.
                2. Comprehensive Snakebite Anti-Venom Protocol (ASV available 24/7 free).
                3. Certified Model Blood Bank (Whole Blood, Platelets, FFP).
                4. Dialysis & Nephrology Emergency Unit.
                5. Neonatal & Pediatric Intensive Care (NICU/PICU).

                • EMERGENCY PATIENT ADMISSION PROCESS:
                No prior referral required for emergency trauma, acute cardiac episodes, or poisoning. Register at Casualty Triage Counter 1. Free emergency ambulance: Dial 108.
            """.trimIndent(),
            fullContentOr = """
                ଫକୀର ମୋହନ ମେଡିକାଲ କଲେଜ ଓ ହସ୍ପିଟାଲ ୨୪x୭ ଜରୁରୀକାଳୀନ କେନ୍ଦ୍ର:
                ଜରୁରୀକାଳୀନ ନମ୍ବର: ୦୬୭୮୨-୨୨୪୪୦୧ | ଆମ୍ବୁଲାନ୍ସ: ୧୦୮

                • ମୁଖ୍ୟ ସେବା:
                ୧. ୨୪ ଘଣ୍ଟିଆ ଟ୍ରମା କେୟାର
                ୨. ସର୍ପାଘାତ ଚିକିତ୍ସା (ମାଗଣା ଆଣ୍ଟି ଭେନମ)
                ୩. ରକ୍ତ ଭଣ୍ଡାର ଓ ପ୍ଲେଟଲେଟ
                ୪. ମାଗଣା ଔଷଧ ଓ ଡାଏଲିସିସ୍
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "24x7 Casualty Landline: 06782-224401",
                "Dial 108 for Free Emergency Government Ambulance",
                "Anti-Snake Venom (ASV) available 24x7 free",
                "Model Blood Bank phone: 06782-224405"
            ),
            keyDirectivesOr = listOf(
                "ଜରୁରୀକାଳୀନ ଫୋନ: ୦୬୭୮୨-୨୨୪୪୦୧",
                "ମାଗଣା ଆମ୍ବୁଲାନ୍ସ ପାଇଁ ୧୦୮ ଡାଏଲ କରନ୍ତୁ",
                "ସର୍ପାଘାତ ଇଞ୍ଜେକ୍ସନ (ASV) ୨୪ ଘଣ୍ଟା ଉପଲବ୍ଧ",
                "ରକ୍ତ ଭଣ୍ଡାର ଫୋନ: ୦୬୭୮୨-୨୨୪୪୦୫"
            ),
            emergencyContactNumbers = listOf("108", "06782-224401", "06782-224405", "102"),
            officialHotlineLabel = "FM MCH 24x7 Casualty & Blood Bank",
            latitude = 21.5160,
            longitude = 86.8970,
            locationName = "FM MCH, Remuna, Balasore",
            qrPayload = "balasore360://pamphlet/fm_medical_college_emergency",
            officialReferenceNumber = "HFW-BLS-FMMCH-2026-02"
        ),
        BalasorePamphlet(
            id = "panchalingeswar_ecotour",
            titleEn = "Panchalingeswar Hill Shrine & Spring Water Safety Advisory",
            titleOr = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପାହାଡ଼ ଝରଣା ଓ ଦର୍ଶନ ସୁରକ୍ଷା ନିର୍ଦ୍ଦେଶାବଳୀ",
            category = PamphletCategory.TOURISM,
            issuingAuthorityEn = "Odisha Tourism & Panchalingeswar Development Council",
            issuingAuthorityOr = "ଓଡ଼ିଶା ପର୍ଯ୍ୟଟନ ଓ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଉନ୍ନୟନ ପରିଷଦ",
            summaryEn = "Guide for climbing the 263 rock steps, touching the perennial stream submerged Shivalingas, and Nilagiri hill trek safety.",
            summaryOr = "୨୬୩ ପାହାଚ ଚଢ଼ି ପାହାଡ଼ି ଝରଣା ମଧ୍ୟରେ ଥିବା ପଞ୍ଚ ଲିଙ୍ଗଙ୍କ ଦର୍ଶନ ଓ ପର୍ବତାରୋହଣ ସତର୍କତା।",
            fullContentEn = """
                Panchalingeswar is situated atop the picturesque Nilagiri hills, famous for the five Shivalingas continually washed by a crystal-clear mountain spring.

                • STAIRCASE & WALKING SAFETY:
                1. 263 granite stairs lead up the temple. Steps can be slippery during rains; hold onto railings.
                2. Wear grip-friendly rubber footwear.
                3. Beware of resident rhesus macaque monkeys along the stairs; keep foodstuffs inside zip bags.
                4. Do not venture into unmarked deep forest ravines beyond the main stream bed.

                • DRINKING WATER & PANTHANIVAS:
                Pure natural spring water filtration points and Odisha Tourism Panthanivas accommodation available at hill base.
            """.trimIndent(),
            fullContentOr = """
                ନୀଳଗିରି ପାହାଡ଼ କୋଳରେ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପୀଠ। ନିରନ୍ତର ପାହାଡ଼ି ଝରଣା ପାଣିରେ ପଞ୍ଚଲିଙ୍ଗ ବୁଡ଼ି ରହିଛନ୍ତି।

                • ସୁରକ୍ଷା ନିୟମ:
                ୧. ୨୬୩ ଟି ପାହାଚ ପିଚ୍ଛିଳ ହୋଇପାରେ, ରେଲିଂ ଧରି ଚାଲନ୍ତୁ।
                ୨. ମାଙ୍କଡ଼ମାନଙ୍କ ଠାରୁ ଖାଦ୍ୟ ସୁରକ୍ଷିତ ରଖନ୍ତୁ।
                ୩. ଜଙ୍ଗଲର ଅଜଣା ଗଭୀର ଖାଲ ମଧ୍ୟକୁ ଯାଆନ୍ତୁ ନାହିଁ।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "263 stone steps; use stainless railings",
                "Beware of monkeys on stairway",
                "Touch Shivalingas gently under flowing spring water",
                "Nilagiri Police Station: 06782-233222"
            ),
            keyDirectivesOr = listOf(
                "୨୬୩ ପାହାଚ; ରେଲିଂ ଧରି ଚଢ଼ନ୍ତୁ",
                "ମାଙ୍କଡ଼ଙ୍କ ଠାରୁ ସତର୍କ ରୁହନ୍ତୁ",
                "ପାହାଡ଼ି ଝରଣାରେ ଲିଙ୍ଗ ସ୍ପର୍ଶ କରନ୍ତୁ",
                "ନୀଳଗିରି ଥାନା: ୦୬୭୮୨-୨୩୩୨୨୨"
            ),
            emergencyContactNumbers = listOf("06782-233222", "112"),
            officialHotlineLabel = "Nilagiri Sub-Divisional Police & Tourism Desk",
            latitude = 21.4678,
            longitude = 86.7212,
            locationName = "Panchalingeswar, Nilagiri, Balasore",
            qrPayload = "balasore360://pamphlet/panchalingeswar_ecotour",
            officialReferenceNumber = "ODT-BLS-PCH-2026-08"
        ),
        BalasorePamphlet(
            id = "talasari_udaipur_beach",
            titleEn = "Talasari & Udaipur Red Crab Beach Tourist Guide",
            titleOr = "ତାଳସାରୀ ଓ ଉଦୟପୁର ଲାଲ କଙ୍କଡ଼ା ବେଳାଭୂମି ପର୍ଯ୍ୟଟନ ନିର୍ଦ୍ଦେଶିକା",
            category = PamphletCategory.TOURISM,
            issuingAuthorityEn = "Bhograi Block Administration & Odisha Tourism",
            issuingAuthorityOr = "ଭୋଗରାଇ ବ୍ଲକ ପ୍ରଶାସନ ଓ ଓଡ଼ିଶା ପର୍ଯ୍ୟଟନ",
            summaryEn = "Subarnarekha estuary boat rides, cashew groves, red crab sanctuaries, and Odisha-Bengal border transit rules.",
            summaryOr = "ସୁବର୍ଣ୍ଣରେଖା ନଦୀ ମୁହାଣ ଡଙ୍ଗା ଚଳାଚଳ, କାଜୁ ବଗିଚା, ଲାଲ କଙ୍କଡ଼ା ସଂରକ୍ଷଣ ଓ ସୀମାନ୍ତ ଭ୍ରମଣ।",
            fullContentEn = """
                Talasari ('Tala' meaning Palm and 'Sari' meaning Row) is bordered by the pristine Subarnarekha river confluence and casuarina plantations.

                • BOAT CROSSING RULES:
                Country boat crossing across the estuary to reach the main beach sand spit is only allowed with certified life jackets provided by the Bhograi Fishermen Federation.

                • RED CRAB CONSERVATION ZONE:
                Udaipur beach is a breeding haven for red ghost crabs. Stepping heavily on crab colonies or catching them is punishable under wildlife protection laws.
            """.trimIndent(),
            fullContentOr = """
                ତାଳସାରୀ ସୁବର୍ଣ୍ଣରେଖା ନଦୀ ମୁହାଣ ନିକଟରେ ଅବସ୍ଥିତ ଏକ ମନୋରମ ଶାନ୍ତ ବେଳାଭୂମି।

                • ଡଙ୍ଗା ଚଳାଚଳ ନିୟମ:
                ଲାଇଫ୍ ଜ୍ୟାକେଟ୍ ପିନ୍ଧି ପ୍ରମାଣିତ ଡଙ୍ଗାରେ ନଦୀ ପାର ହୁଅନ୍ତୁ।

                • ଲାଲ କଙ୍କଡ଼ା ସଂରକ୍ଷଣ:
                ଉଦୟପୁର କୂଳରେ ଲାଲ କଙ୍କଡ଼ାଙ୍କୁ କ୍ଷତି ପହଞ୍ଚାନ୍ତୁ ନାହିଁ।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Mandatory lifejackets for estuary boats",
                "Protect rare red ghost crab colonies",
                "Bhograi Police Hotline: 06781-238222"
            ),
            keyDirectivesOr = listOf(
                "ଡଙ୍ଗାରେ ଲାଇଫ୍ ଜ୍ୟାକେଟ ବାଧ୍ୟତାମୂଳକ",
                "ଲାଲ କଙ୍କଡ଼ାଙ୍କୁ କ୍ଷତି ପହଞ୍ଚାନ୍ତୁ ନାହିଁ",
                "ଭୋଗରାଇ ଥାନା: ୦୬୭୮୧-୨୩୮୨୨୨"
            ),
            emergencyContactNumbers = listOf("06781-238222", "112"),
            officialHotlineLabel = "Talasari Marine Police Post",
            latitude = 21.6033,
            longitude = 87.4667,
            locationName = "Talasari, Bhograi, Balasore",
            qrPayload = "balasore360://pamphlet/talasari_udaipur_beach",
            officialReferenceNumber = "ODT-BLS-TLS-2026-14"
        ),
        BalasorePamphlet(
            id = "baleswar_mahotsav_2026",
            titleEn = "Baleswar Mahotsav & Expo 2026 Official Festival Poster",
            titleOr = "ବାଲେଶ୍ୱର ମହୋତ୍ସବ ଓ ଏକ୍ସପୋ ୨୦୨୬ ସରକାରୀ ପୋଷ୍ଟର",
            category = PamphletCategory.EVENT_POSTER,
            issuingAuthorityEn = "District Cultural Council & Balasore District Administration",
            issuingAuthorityOr = "ଜିଲ୍ଲା ସଂସ୍କୃତି ପରିଷଦ ଓ ବାଲେଶ୍ୱର ଜିଲ୍ଲା ପ୍ରଶାସନ",
            summaryEn = "Official schedule, cultural line-up, craft pavilions, and visitor directives for Balasore's premier winter socio-cultural festival.",
            summaryOr = "ବାଲେଶ୍ୱରର ପ୍ରମୁଖ ଶୀତକାଳୀନ ସାଂସ୍କୃତିକ ମହୋତ୍ସବ, କ୍ରାଫ୍ଟ ମେଳା ଓ ଓଡ଼ିଶୀ ନୃତ୍ୟ କାର୍ଯ୍ୟକ୍ରମ ନିର୍ଦ୍ଦେଶାବଳୀ।",
            fullContentEn = """
                BALESWAR MAHOTSAV & EXPO 2026:
                Venue: Gandhi Smruti Bhawan & ITI Grounds, Balasore
                Dates: Official District Winter Carnival

                • CULTURAL EVENINGS & STAGE SCHEDULE:
                - Daily Cultural Programs: 06:00 PM to 10:00 PM
                - Chhau Dance from Nilagiri & Odissi recitals by legendary gurus
                - Folk music, tribal dances, and national handloom exhibition

                • VISITOR GUIDELINES:
                1. Entire festival perimeter is a Single-Use Plastic Free Zone.
                2. Free drinking water kiosks (Pravasi Seva Kendra) available at all 4 quadrants.
                3. Dedicated parking at Remuna Golei approach and Station Road.
                4. CCTV surveillance and 24/7 Police Assistance Booth at Gate 1.
            """.trimIndent(),
            fullContentOr = """
                ବାଲେଶ୍ୱର ମହୋତ୍ସବ ଓ ଏକ୍ସପୋ ୨୦୨୬:
                ସ୍ଥାନ: ଗାନ୍ଧୀ ସ୍ମୃତି ଭବନ ଓ ଆଇଟିଆଇ ପଡ଼ିଆ, ବାଲେଶ୍ୱର।

                • ସାଂସ୍କୃତିକ କାର୍ଯ୍ୟକ୍ରମ:
                - ପ୍ରତି ସନ୍ଧ୍ୟା ୬:୦୦ ରୁ ରାତି ୧୦:୦୦ ପର୍ଯ୍ୟନ୍ତ।
                - ନୀଳଗିରିର ଛଉ ନୃତ୍ୟ, ଓଡ଼ିଶୀ ଓ ଲୋକକଳା ପରିବେଷଣ।
                - ଜାତୀୟ ହସ୍ତତନ୍ତ ଓ କୃଷି ମେଳା।

                • ନିୟମାବଳୀ:
                ୧. ପ୍ଲାଷ୍ଟିକ ବ୍ୟବହାର ସମ୍ପୂର୍ଣ୍ଣ ନିଷିଦ୍ଧ।
                ୨. ପାନୀୟ ଜଳ କେନ୍ଦ୍ର ଉପଲବ୍ଧ।
                ୩. ଗେଟ୍ ୧ ନିକଟରେ ପୋଲିସ ସହାୟତା କେନ୍ଦ୍ର।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Stage performances daily 6 PM to 10 PM",
                "Zero-plastic eco zone enforced",
                "Police Help Desk & First-Aid at Gate 1",
                "Mo Bus special festival shuttles active"
            ),
            keyDirectivesOr = listOf(
                "ପ୍ରତି ସନ୍ଧ୍ୟା ୬ ରୁ ୧୦ ସାଂସ୍କୃତିକ ମଞ୍ଚ",
                "ପ୍ଲାଷ୍ଟିକ ନିଷିଦ୍ଧ ଅଞ୍ଚଳ",
                "ଗେଟ୍ ୧ ନିକଟରେ ପ୍ରାଥମିକ ଚିକିତ୍ସା କେନ୍ଦ୍ର",
                "ମୋ ବସ୍ ସ୍ୱତନ୍ତ୍ର ମହୋତ୍ସବ ସଟଲ୍"
            ),
            emergencyContactNumbers = listOf("06782-262222", "112", "1077"),
            officialHotlineLabel = "Festival Control Room & DEOC",
            latitude = 21.4925,
            longitude = 86.9240,
            locationName = "ITI Grounds, Balasore Town",
            qrPayload = "balasore360://poster/baleswar_mahotsav_2026",
            officialReferenceNumber = "DCC-BLS-MHT-2026-01"
        ),
        BalasorePamphlet(
            id = "chandipur_beach_festival",
            titleEn = "Chandipur Beach Festival & Vanishing Sea Gala Poster",
            titleOr = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ମହୋତ୍ସବ ସରକାରୀ ପୋଷ୍ଟର",
            category = PamphletCategory.EVENT_POSTER,
            issuingAuthorityEn = "Odisha Tourism Development Corporation (OTDC) & Marine Police",
            issuingAuthorityOr = "ଓଡ଼ିଶା ପର୍ଯ୍ୟଟନ ନିଗମ (OTDC) ଓ ସାମୁଦ୍ରିକ ଥାନା",
            summaryEn = "Official poster for the Chandipur International Beach Festival celebrating coastal ecology, kite flying, and seafood heritage.",
            summaryOr = "ଚାନ୍ଦିପୁର ଅନ୍ତର୍ଜାତୀୟ ବେଳାଭୂମି ମହୋତ୍ସବ, ସମୁଦ୍ର କୂଳ ଗାଲା ଓ କାଇଟ୍ ଫ୍ଲାଇଂ ଉତ୍ସବ ସୂଚନା।",
            fullContentEn = """
                CHANDIPUR INTERNATIONAL BEACH FESTIVAL:
                Location: Chandipur-on-Sea Promenade

                • HIGHLIGHTS:
                - Vanishing Sea low-tide heritage walk with marine biologists
                - International Kite Flying displays over exposed seabed
                - Coastal seafood culinary championship & Balaramgadi prawn stalls
                - Evening cultural laser show on the Bay of Bengal coastline

                • SAFETY DIRECTIVES:
                1. Always verify the Low-Tide Safe Walk timer on Baleshwar 360.
                2. Do not enter water during High Tide warnings.
            """.trimIndent(),
            fullContentOr = """
                ଚାନ୍ଦିପୁର ବେଳାଭୂମି ମହୋତ୍ସବ:
                ସ୍ଥାନ: ଚାନ୍ଦିପୁର ପ୍ରୋମେନେଡ୍।

                • ପ୍ରମୁଖ ଆକର୍ଷଣ:
                - ସମୁଦ୍ର ଶଯ୍ୟାରେ କାଇଟ୍ ଫେଷ୍ଟିଭାଲ୍।
                - ସାମୁଦ୍ରିକ ଖାଦ୍ୟ ମହୋତ୍ସବ।
                - ଲେଜର ଲାଇଟ୍ ଶୋ।

                • ସୁରକ୍ଷା ନିର୍ଦ୍ଦେଶ:
                ୧. ଜୁଆର ସମୟ ପ୍ରତି ସର୍ବଦା ସତର୍କ ରୁହନ୍ତୁ।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Safe Walk Window strictly supervised by Lifeguards",
                "Marine Police help booth at OTDC Panthanivas front",
                "Parking along designated seawall avenue"
            ),
            keyDirectivesOr = listOf(
                "ଲାଇଫଗାର୍ଡ ତତ୍ତ୍ୱାବଧାନରେ ସମୁଦ୍ର ଚାଲିବା",
                "ପାନ୍ଥନିବାସ ସମ୍ମୁଖରେ ସାମୁଦ୍ରିକ ପୋଲିସ ସହାୟତା",
                "ନିର୍ଦ୍ଦିଷ୍ଟ ପାର୍କିଂ ବ୍ୟବହାର କରନ୍ତୁ"
            ),
            emergencyContactNumbers = listOf("06782-270054", "112"),
            officialHotlineLabel = "Chandipur Marine Police & Lifeguards",
            latitude = 21.4695,
            longitude = 87.0185,
            locationName = "Chandipur Beach Promenade",
            qrPayload = "balasore360://poster/chandipur_beach_festival",
            officialReferenceNumber = "OTDC-BLS-CBF-2026-02"
        ),
        BalasorePamphlet(
            id = "chandaneswar_chadak_festival",
            titleEn = "Chandaneswar Chadak Mela & Shaivite Pilgrimage Poster",
            titleOr = "ଚାନ୍ଦନେଶ୍ୱର ଚଡ଼କ ମେଳା ଓ ତୀର୍ଥଯାତ୍ରା ପୋଷ୍ଟର",
            category = PamphletCategory.EVENT_POSTER,
            issuingAuthorityEn = "Bhusandeswar-Chandaneswar Temple Trust & Police Administration",
            issuingAuthorityOr = "ଚାନ୍ଦନେଶ୍ୱର ମନ୍ଦିର ଟ୍ରଷ୍ଟ ଓ ପୋଲିସ ପ୍ରଶାସନ",
            summaryEn = "Official poster and pilgrim directives for the historic Chadak Mela during Pana Sankranti at Baba Chandaneswar Temple.",
            summaryOr = "ପଣା ସଂକ୍ରାନ୍ତି ଉପଲକ୍ଷେ ବାବା ଚାନ୍ଦନେଶ୍ୱରଙ୍କ ପ୍ରସିଦ୍ଧ ଚଡ଼କ ମେଳା ଓ ଭକ୍ତ ନିର୍ଦ୍ଦେଶାବଳୀ।",
            fullContentEn = """
                CHANDANESWAR CHADAK MELA:
                Dates: Maha Vishuva (Pana) Sankranti

                • PILGRIM PROTOCOLS:
                - Special queue complex for Nila Sanyasis and general devotees
                - 24/7 First Aid medical camp operated by CHC Bhograi
                - Free drinking water kiosks along temple perimeter
            """.trimIndent(),
            fullContentOr = """
                ବାବା ଚାନ୍ଦନେଶ୍ୱର ଚଡ଼କ ମେଳା:
                ପଣା ସଂକ୍ରାନ୍ତି ଉତ୍ସବ।
                - ନୀଳ ସନ୍ୟାସୀ ଓ ଭକ୍ତଙ୍କ ପାଇଁ ସ୍ୱତନ୍ତ୍ର ଧାଡ଼ି।
                - ୨୪ ଘଣ୍ଟିଆ ପ୍ରାଥମିକ ଚିକିତ୍ସା କେନ୍ଦ୍ର।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Follow barricaded queue lines to sanctum",
                "Emergency medical team near Temple Gate 2",
                "Police control post active round the clock"
            ),
            keyDirectivesOr = listOf(
                "ବାରିକେଡ୍ ଧାଡ଼ି ଅନୁସରଣ କରନ୍ତୁ",
                "ମନ୍ଦିର ଗେଟ୍ ୨ ନିକଟରେ ଚିକିତ୍ସା କେନ୍ଦ୍ର",
                "୨୪ ଘଣ୍ଟା ପୋଲିସ ନିୟନ୍ତ୍ରଣ କକ୍ଷ"
            ),
            emergencyContactNumbers = listOf("06781-238210", "112"),
            officialHotlineLabel = "Temple Control & Bhograi Police",
            latitude = 21.6167,
            longitude = 87.5167,
            locationName = "Baba Chandaneswar Temple, Bhograi",
            qrPayload = "balasore360://poster/chandaneswar_chadak_festival",
            officialReferenceNumber = "TDB-BLS-CDN-2026-03"
        ),
        BalasorePamphlet(
            id = "nilagiri_chhau_mahotsav_2026",
            titleEn = "Nilagiri Palace Royal Chhau Dance & Folk Heritage Utsav Poster",
            titleOr = "ନୀଳଗିରି ରାଜପ୍ରାସାଦ ରାଜକୀୟ ଛଉ ନୃତ୍ୟ ଓ ଲୋକକଳା ମହୋତ୍ସବ ପୋଷ୍ଟର",
            category = PamphletCategory.EVENT_POSTER,
            issuingAuthorityEn = "Nilagiri Sub-Divisional Administration & Cultural Board",
            issuingAuthorityOr = "ନୀଳଗିରି ଉପଖଣ୍ଡ ପ୍ରଶାସନ ଓ ସାଂସ୍କୃତିକ ପରିଷଦ",
            summaryEn = "Official event guidelines for the historic martial Chhau dance performance, royal palace courtyard stage, folk crafts pavilion, and heritage archery displays.",
            summaryOr = "ଐତିହାସିକ ନୀଳଗିରି ରାଜପ୍ରାସାଦ ପ୍ରାଙ୍ଗଣରେ ଯୁଦ୍ଧ ନୃତ୍ୟ ଛଉ, ପାରମ୍ପରିକ ତୀରନ୍ଦାଜୀ ଓ ହସ୍ତତନ୍ତ ପ୍ରଦର୍ଶନୀର ସରକାରୀ ନିର୍ଦ୍ଦେଶାବଳୀ।",
            fullContentEn = """
                NILAGIRI ROYAL CHHAU DANCE & FOLK CARNIVAL 2026:
                Venue: Royal Palace Courtyard & Nilagiri Stadium, Balasore

                • HISTORIC MARTIAL CHHAU HIGHLIGHTS:
                - Mayurbhanj & Nilagiri style open-air acrobatic Chhau performances
                - Martial shield and sword battle dance interpretations of Ramayana and Mahabharata
                - Tribal musical ensembles featuring Dhamsa, Dhol, and Mahuri instruments
                - Demonstration workshops for tourists on making traditional papier-mâché Chhau masks

                • OFFICIAL AUDIENCE DIRECTIVES:
                1. Entry via Royal Lion Gate (Singhadwara); security scanning active.
                2. Reserved seating enclosure for tourists, senior citizens, and visiting dignitaries.
                3. Nilagiri stone carving and bamboo craft demonstration stalls in Outer Court.
                4. Police assistance and first-aid ambulance station deployed near Palace Gate 1.
            """.trimIndent(),
            fullContentOr = """
                ନୀଳଗିରି ରାଜପ୍ରାସାଦ ଛଉ ମହୋତ୍ସବ ୨୦୨୬:
                ସ୍ଥାନ: ରାଜପ୍ରାସାଦ ପ୍ରାଙ୍ଗଣ ଓ ନୀଳଗିରି ଷ୍ଟାଡିୟମ୍।

                • ଆକର୍ଷଣ:
                - ରାଜକୀୟ ଛଉ ନୃତ୍ୟ, ପାରମ୍ପରିକ ଢୋଲ ଓ ମହୁରୀ ବାଦନ।
                - ଛଉ ମୁଖା ନିର୍ମାଣ କର୍ମଶାଳା।
                - ନୀଳଗିରି ପଥର କାରୁକାର୍ଯ୍ୟ ପ୍ରଦର୍ଶନୀ।

                • ନିୟମ:
                ୧. ସିଂହଦ୍ୱାର ଦେଇ ଶୃଙ୍ଖଳିତ ପ୍ରବେଶ କରନ୍ତୁ।
                ୨. ପର୍ଯ୍ୟଟକଙ୍କ ପାଇଁ ସ୍ୱତନ୍ତ୍ର ସହାୟତା କେନ୍ଦ୍ର।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Entry through Royal Singhadwara with valid pass",
                "Chhau performances daily 5:30 PM to 9:30 PM",
                "Stone craft workshops open from 10 AM",
                "First-aid post active near Palace Gate 1"
            ),
            keyDirectivesOr = listOf(
                "ସିଂହଦ୍ୱାର ଦେଇ ଶୃଙ୍ଖଳିତ ପ୍ରବେଶ କରନ୍ତୁ",
                "ପ୍ରତି ସନ୍ଧ୍ୟା ୫:୩୦ ରୁ ୯:୩୦ ଛଉ ନୃତ୍ୟ",
                "ସକାଳ ୧୦ଟାରୁ ପଥର କଳା କର୍ମଶାଳା",
                "ଗେଟ୍ ୧ ନିକଟରେ ଚିକିତ୍ସା କେନ୍ଦ୍ର"
            ),
            emergencyContactNumbers = listOf("06782-233222", "112"),
            officialHotlineLabel = "Nilagiri Festival Police Control",
            latitude = 21.4583,
            longitude = 86.7667,
            locationName = "Nilagiri Palace, Balasore",
            qrPayload = "balasore360://poster/nilagiri_chhau_mahotsav_2026",
            officialReferenceNumber = "DCC-NIL-CHH-2026-04"
        ),
        BalasorePamphlet(
            id = "balasore_international_sand_art_expo",
            titleEn = "Balasore Golden Beach International Sand Art & Coastal Expo Poster",
            titleOr = "ବାଲେଶ୍ୱର ସୁବର୍ଣ୍ଣ ବେଳାଭୂମି ଅନ୍ତର୍ଜାତୀୟ ବାଲୁକା କଳା ଓ ଉପକୂଳ ଏକ୍ସପୋ ପୋଷ୍ଟର",
            category = PamphletCategory.EVENT_POSTER,
            issuingAuthorityEn = "Odisha Tourism, Department of Tourism & Culture",
            issuingAuthorityOr = "ଓଡ଼ିଶା ପର୍ଯ୍ୟଟନ ଓ ସଂସ୍କୃତି ବିଭାଗ",
            summaryEn = "Official event directives for the grand intertidal sand sculpture gallery, coastal conservation exhibits, and evening cultural lights at Chandipur-on-Sea.",
            summaryOr = "ଚାନ୍ଦିପୁର ସମୁଦ୍ରକୂଳରେ ବିଶ୍ୱପ୍ରସିଦ୍ଧ ବାଲୁକା ମୂର୍ତ୍ତି କଳା ପ୍ରଦର୍ଶନୀ, ସାମୁଦ୍ରିକ ଜୀବ ସଂରକ୍ଷଣ ଓ ସାଂସ୍କୃତିକ କାର୍ଯ୍ୟକ୍ରମ ସୂଚନା।",
            fullContentEn = """
                BALASORE GOLDEN BEACH INTERNATIONAL SAND ART EXPO 2026:
                Location: Chandipur Beach Intertidal Zone (near Main Seawall)

                • EXPO HIGHLIGHTS:
                - National and international sand artists sculpting monumental sculptures on the exposed sea bottom
                - Environmental themes: Protection of Horseshoe Crabs, Mangrove Ecology, and Mission Shakti
                - Illuminated twilight viewing under marine-grade floodlights during receding tide
                - Interactive children's sand sculpting workshop by celebrated master sculptors

                • VISITOR SAFETY & TIDAL ADVISORY:
                1. Sand Art Arena operates strictly in coordination with the Baleshwar 360 Low-Tide timetable.
                2. Do not cross protective rope barricades around sand sculptures.
                3. Lifeguards and Odisha Coastal Police station active 24/7.
            """.trimIndent(),
            fullContentOr = """
                ବାଲେଶ୍ୱର ଅନ୍ତର୍ଜାତୀୟ ବାଲୁକା କଳା ଏକ୍ସପୋ ୨୦୨୬:
                ସ୍ଥାନ: ଚାନ୍ଦିପୁର ବେଳାଭୂମି।

                • ମୁଖ୍ୟ ଆକର୍ଷଣ:
                - ଅନ୍ତର୍ଜାତୀୟ ବାଲୁକା ଶିଳ୍ପୀଙ୍କ ଦ୍ୱାରା ଚିତ୍ତାକର୍ଷକ ମୂର୍ତ୍ତି ନିର୍ମାଣ।
                - ସମୁଦ୍ରକୂଳ ଓ ଜୀବଜନ୍ତୁ ସଂରକ୍ଷଣ ବାର୍ତ୍ତା।
                - ଲେଜର ଓ ଆଲୋକମୟ ରାତ୍ରି ପ୍ରଦର୍ଶନୀ।

                • ସତର୍କତା:
                ୧. ଭଟ୍ଟା ସମୟରେ ହିଁ ବାଲୁକା କଳା ପରିଦର୍ଶନ ଅନୁମୋଦିତ।
                ୨. ବାରିକେଡ୍ ଅତିକ୍ରମ କରନ୍ତୁ ନାହିଁ।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Follow designated intertidal walkway path",
                "Do not touch or lean on sand sculptures",
                "Consult tide schedule on Baleshwar 360",
                "Marine Police help booth at seawall center"
            ),
            keyDirectivesOr = listOf(
                "ନିର୍ଦ୍ଧାରିତ ପଥରେ ଚାଲି ବାଲୁକା କଳା ଦେଖନ୍ତୁ",
                "ବାଲୁକା ମୂର୍ତ୍ତି ସ୍ପର୍ଶ କରନ୍ତୁ ନାହିଁ",
                "ଆପ୍ଲିକେସନରେ ଜୁଆର-ଭଟ୍ଟା ସମୟ ଦେଖନ୍ତୁ",
                "ସାମୁଦ୍ରିକ ପୋଲିସ ସହାୟତା କେନ୍ଦ୍ର ଉପଲବ୍ଧ"
            ),
            emergencyContactNumbers = listOf("06782-270054", "112"),
            officialHotlineLabel = "Sand Art Festival & Marine Police Desk",
            latitude = 21.4690,
            longitude = 87.0175,
            locationName = "Chandipur Beach Promenade",
            qrPayload = "balasore360://poster/balasore_international_sand_art_expo",
            officialReferenceNumber = "ODT-BLS-SAE-2026-05"
        ),
        BalasorePamphlet(
            id = "raibania_fortress_heritage_gala",
            titleEn = "Raibania Medieval Fortress Light, Sound & Heritage Gala Poster",
            titleOr = "ଐତିହାସିକ ରାଇବଣିଆ ଦୁର୍ଗ ଆଲୋକ, ଧ୍ୱନି ଓ ଐତିହ୍ୟ ଉତ୍ସବ ପୋଷ୍ଟର",
            category = PamphletCategory.EVENT_POSTER,
            issuingAuthorityEn = "Odisha State Archaeology & Jaleswar Block Administration",
            issuingAuthorityOr = "ଓଡ଼ିଶା ରାଜ୍ୟ ପ୍ରତ୍ନତତ୍ତ୍ୱ ବିଭାଗ ଓ ଜଳେଶ୍ୱର ବ୍ଲକ ପ୍ରଶାସନ",
            summaryEn = "Official poster for Eastern India's largest medieval stone fortress festival celebrating Gajapati military architecture, archaeological walks, and sound spectacle.",
            summaryOr = "ପୂର୍ବ ଭାରତର ସର୍ବବୃହତ ମଧ୍ୟଯୁଗୀୟ ପଥର ଦୁର୍ଗ ରାଇବଣିଆରେ ଗଜପତି ସାମରିକ ଐତିହ୍ୟ, ଧ୍ୱନି ଓ ଆଲୋକ ଶୋ ପୋଷ୍ଟର।",
            fullContentEn = """
                RAIBANIA FORTRESS MEDIEVAL HERITAGE GALA 2026:
                Location: Raibania Fort Complex, Jaleswar, Balasore

                • FORTRESS HERITAGE SPECTACLE:
                - Evening Light & Sound show narrating King Langula Narasingha Deva's military fortress
                - Guided archaeological trail through the Jayachandi gate, moat fortifications, and underground chambers
                - Traditional martial Paika Akhada performances by local warrior artists
                - Regional culinary fare featuring authentic Jaleswar sweet delicacies

                • ARCHAEOLOGICAL PRESERVATION DIRECTIVES:
                1. Raibania is a protected state monument. Defacing stone ramparts or littering is strictly penalized.
                2. Carry comfortable walking shoes for the 2.5 km fortress trail.
                3. Emergency helpline and tourist assistance booth open at Western Gateway.
            """.trimIndent(),
            fullContentOr = """
                ରାଇବଣିଆ ଦୁର୍ଗ ଐତିହ୍ୟ ମହୋତ୍ସବ ୨୦୨୬:
                ସ୍ଥାନ: ରାଇବଣିଆ ଦୁର୍ଗ, ଜଳେଶ୍ୱର।

                • ଆକର୍ଷଣ:
                - ରାଜା ଲାଙ୍ଗୁଳା ନରସିଂହ ଦେବଙ୍କ ଦୁର୍ଗ ଉପରେ ଆଲୋକ ଓ ଧ୍ୱନି ପ୍ରଦର୍ଶନ।
                - ଐତିହାସିକ ଜୟଚଣ୍ଡୀ ଫାଟକ ଓ ଖାଇ ପରିକ୍ରମା।
                - ପାରମ୍ପରିକ ପାଇକ ଆଖଡ଼ା ଯୁଦ୍ଧ କଳା।

                • ନିୟମ:
                ୧. ପ୍ରାଚୀନ ପଥର କାନ୍ଥକୁ କ୍ଷତି ପହଞ୍ଚାନ୍ତୁ ନାହିଁ।
                ୨. ପଶ୍ଚିମ ଫାଟକ ନିକଟରେ ସହାୟତା କେନ୍ଦ୍ର।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Light & Sound show at 7:00 PM daily",
                "Stay on marked walking trails inside the fort",
                "Littering inside monument perimeter forbidden",
                "Jaleswar Police Station: 06781-222222"
            ),
            keyDirectivesOr = listOf(
                "ପ୍ରତି ସନ୍ଧ୍ୟା ୭:୦୦ ରେ ଆଲୋକ ଓ ଧ୍ୱନି ଶୋ",
                "ଦୁର୍ଗ ଭିତରେ ଚିହ୍ନିତ ରାସ୍ତାରେ ଚାଲନ୍ତୁ",
                "ଅଳିଆ ଆବର୍ଜନା ପକାଇବା ମନା",
                "ଜଳେଶ୍ୱର ଥାନା: ୦୬୭୮୧-୨୨୨୨୨୨"
            ),
            emergencyContactNumbers = listOf("06781-222222", "112"),
            officialHotlineLabel = "Archaeology Help Desk & Jaleswar Police",
            latitude = 21.9167,
            longitude = 87.1667,
            locationName = "Raibania Fortress, Jaleswar",
            qrPayload = "balasore360://poster/raibania_fortress_heritage_gala",
            officialReferenceNumber = "OSA-BLS-RBF-2026-06"
        ),
        BalasorePamphlet(
            id = "talsari_marine_eco_utsav",
            titleEn = "Talsari Subarnarekha Estuary & Red Crab Eco Utsav Poster",
            titleOr = "ତାଳସାରୀ ସୁବର୍ଣ୍ଣରେଖା ମୁହାଣ ଓ ଲାଲ କଙ୍କଡ଼ା ଇକୋ ଉତ୍ସବ ପୋଷ୍ଟର",
            category = PamphletCategory.EVENT_POSTER,
            issuingAuthorityEn = "Bhograi Block Administration & Odisha Eco-Tourism Society",
            issuingAuthorityOr = "ଭୋଗରାଇ ବ୍ଲକ ପ୍ରଶାସନ ଓ ଓଡ଼ିଶା ଇକୋ-ଟୁରିଜିମ୍ ସୋସାଇଟି",
            summaryEn = "Annual coastal mangrove conservation carnival, traditional fisherman boat races, cashew culinary competitions, and eco-tours at Udaipur beach.",
            summaryOr = "ସୁବର୍ଣ୍ଣରେଖା ମୁହାଣରେ ପାରମ୍ପରିକ ଡଙ୍ଗା ଚାଳନା ପ୍ରତିଯୋଗିତା, କାଜୁ ବଗିଚା ମେଳା ଓ ଲାଲ କଙ୍କଡ଼ା ସଂରକ୍ଷଣ ମହୋତ୍ସବ।",
            fullContentEn = """
                TALSARI MARINE ECO UTSAV 2026:
                Location: Talsari Beach & Bichitrapur Mangrove Sanctuary

                • FESTIVAL HIGHLIGHTS:
                - Subarnarekha estuary traditional wooden boat rowing regatta
                - Bichitrapur mangrove guided eco-boat cruises through coastal creeks
                - Educational displays on conservation of ghost red crabs and migratory coastal waders
                - Bhograi organic cashew and seafood culinary stalls

                • SAFETY GUIDELINES:
                1. Always wear official life vests during estuary boat excursions.
                2. Follow zero-plastic guidelines along the casuarina belt.
                3. Marine Police and coastal guards monitor boat departure ghats.
            """.trimIndent(),
            fullContentOr = """
                ତାଳସାରୀ ସାମୁଦ୍ରିକ ଇକୋ ଉତ୍ସବ ୨୦୨୬:
                ସ୍ଥାନ: ତାଳସାରୀ ବେଳାଭୂମି ଓ ବିଚିତ୍ରପୁର ହେନ୍ତାଳବଣ।

                • ମୁଖ୍ୟ ଆକର୍ଷଣ:
                - ସୁବର୍ଣ୍ଣରେଖା ମୁହାଣରେ ପାରମ୍ପରିକ ଡଙ୍ଗା ବାଇଚ ପ୍ରତିଯୋଗିତା।
                - ବିଚିତ୍ରପୁର ମାଙ୍ଗ୍ରୋଭ୍ ନୌକା ବିହାର।
                - ଲାଲ କଙ୍କଡ଼ା ଓ ସାମୁଦ୍ରିକ ପକ୍ଷୀ ସଂରକ୍ଷଣ ପ୍ରଦର୍ଶନୀ।

                • ସୁରକ୍ଷା ନିୟମ:
                ୧. ଡଙ୍ଗା ଚଢ଼ିବା ସମୟରେ ଲାଇଫ୍ ଜ୍ୟାକେଟ ବ୍ୟବହାର କରନ୍ତୁ।
                ୨. ପ୍ଲାଷ୍ଟିକ ମୁକ୍ତ ପରିବେଶ ରକ୍ଷା କରନ୍ତୁ।
            """.trimIndent(),
            keyDirectivesEn = listOf(
                "Mandatory lifejackets on all festival boat rides",
                "Eco-friendly zero plastic protocol enforced",
                "Talsari Marine Police Post active round the clock",
                "Estuary cruise timings: 7:00 AM to 5:00 PM"
            ),
            keyDirectivesOr = listOf(
                "ଡଙ୍ଗାରେ ଲାଇଫ୍ ଜ୍ୟାକେଟ୍ ବାଧ୍ୟତାମୂଳକ",
                "ପ୍ଲାଷ୍ଟିକ ସମ୍ପୂର୍ଣ୍ଣ ନିଷିଦ୍ଧ",
                "ତାଳସାରୀ ସାମୁଦ୍ରିକ ଥାନା ସତର୍କ",
                "ବୋଟିଂ ସମୟ: ସକାଳ ୭ ରୁ ଅପରାହ୍ନ ୫"
            ),
            emergencyContactNumbers = listOf("06781-238222", "112"),
            officialHotlineLabel = "Talsari Marine Festival Booth",
            latitude = 21.6050,
            longitude = 87.4680,
            locationName = "Talsari Beach, Bhograi",
            qrPayload = "balasore360://poster/talsari_marine_eco_utsav",
            officialReferenceNumber = "OET-BLS-TLS-2026-07"
        )
    )

    /**
     * Maps an official pamphlet or QR payload to its corresponding [com.example.data.model.Hotspot] in the app.
     */
    fun resolveLinkedHotspot(pamphletId: String?, rawPayload: String): com.example.data.model.Hotspot? {
        val allHotspots = com.example.data.repository.BalasoreRepository.hotspots
        val hotspotId = when (pamphletId) {
            "chandipur_beach_safety",
            "chandipur_beach_festival",
            "balasore_international_sand_art_expo" -> "chandipur_beach"
            "kuldiha_wildlife_sanctuary" -> "kuldiha_sanctuary"
            "remuna_khirachora_gopinath" -> "khirachora_gopinatha"
            "panchalingeswar_ecotour",
            "nilagiri_chhau_mahotsav_2026" -> "panchalingeswar"
            "talasari_udaipur_beach",
            "talsari_marine_eco_utsav" -> "talasari_beach"
            "chandaneswar_chadak_festival",
            "raibania_fortress_heritage_gala" -> "bhusandeshwar_temple"
            "baleswar_mahotsav_2026" -> "fakir_mohan_smruti"
            else -> null
        }

        if (hotspotId != null) {
            allHotspots.firstOrNull { it.id == hotspotId }?.let { return it }
        }

        val q = rawPayload.trim().lowercase()
        return allHotspots.firstOrNull { spot ->
            q.contains(spot.id.lowercase()) ||
                q.contains(spot.name.lowercase().take(10)) ||
                (q.contains("emami") && spot.id == "emami_jagannath") ||
                (q.contains("fakir") && spot.id == "fakir_mohan_smruti") ||
                (q.contains("bhusand") && spot.id == "bhusandeshwar_temple") ||
                (q.contains("chandipur") && spot.id == "chandipur_beach") ||
                (q.contains("panchaling") && spot.id == "panchalingeswar") ||
                (q.contains("khirachora") && spot.id == "khirachora_gopinatha") ||
                (q.contains("remuna") && spot.id == "khirachora_gopinatha") ||
                (q.contains("kuldiha") && spot.id == "kuldiha_sanctuary") ||
                (q.contains("talasari") && spot.id == "talasari_beach") ||
                (q.contains("talsari") && spot.id == "talasari_beach")
        }
    }

    /**
     * Resolves a QR code raw string payload into an official pamphlet, tourism hotspot, or event poster scan result.
     */
    fun resolveQrPayload(raw: String): QrScanResult {
        val trimmed = raw.trim()

        // 1. Direct match on official Balasore URI scheme, poster URI or pamphlet ID
        val matched = OFFICIAL_PAMPHLETS.firstOrNull { pamphlet ->
            trimmed.equals(pamphlet.qrPayload, ignoreCase = true) ||
                trimmed.equals(pamphlet.id, ignoreCase = true) ||
                trimmed.contains("pamphlet/${pamphlet.id}", ignoreCase = true) ||
                trimmed.contains("poster/${pamphlet.id}", ignoreCase = true) ||
                trimmed.contains("tourism/${pamphlet.id}", ignoreCase = true) ||
                trimmed.contains("event/${pamphlet.id}", ignoreCase = true) ||
                trimmed.contains(pamphlet.id, ignoreCase = true) ||
                trimmed.equals(pamphlet.officialReferenceNumber, ignoreCase = true)
        }

        if (matched != null) {
            val linkedHotspot = resolveLinkedHotspot(matched.id, trimmed)
            val isEventPoster = matched.category == PamphletCategory.EVENT_POSTER
            val (eventDate, eventStageTime) = when (matched.id) {
                "baleswar_mahotsav_2026" -> "Jan 12 – Jan 18, 2027" to "06:00 PM – 10:00 PM (Main Cultural Stage)"
                "chandipur_beach_festival" -> "Dec 24 – Dec 28, 2026" to "04:00 PM – 09:30 PM (Seawall Promenade)"
                "chandaneswar_chadak_festival" -> "Apr 01 – Apr 14, 2027 (Pana Sankranti)" to "05:00 AM – 10:00 PM (Sanctum Complex)"
                "nilagiri_chhau_mahotsav_2026" -> "Apr 14 – Apr 16, 2027" to "05:30 PM – 09:30 PM (Royal Palace Courtyard)"
                "balasore_international_sand_art_expo" -> "Dec 25 – Dec 30, 2026" to "08:00 AM – 08:00 PM (Low-Tide Arena)"
                "raibania_fortress_heritage_gala" -> "Jan 23 – Jan 25, 2027" to "06:30 PM – 09:00 PM (Light & Sound Arena)"
                "talsari_marine_eco_utsav" -> "Feb 06 – Feb 08, 2027" to "07:00 AM – 06:00 PM (Subarnarekha Estuary)"
                else -> "Daily Visitor Schedule (2026-27)" to (linkedHotspot?.operatingHours ?: "06:00 AM – 08:00 PM")
            }

            return QrScanResult(
                rawValue = trimmed,
                matchedPamphlet = matched,
                isOfficialBalasorePamphlet = true,
                detectedType = QrPayloadType.OFFICIAL_PAMPHLET,
                matchedHotspot = linkedHotspot,
                matchedEventDate = eventDate,
                matchedEventStageTime = eventStageTime,
                matchedAppTargetType = if (isEventPoster) "EVENT_POSTER" else "HOTSPOT"
            )
        }

        // 1B. Direct match on any Hotspot ID or Hotspot QR URI (e.g., balasore360://hotspot/emami_jagannath)
        val directHotspot = resolveLinkedHotspot(null, trimmed)
        if (directHotspot != null) {
            val synthesizedPamphlet = BalasorePamphlet(
                id = directHotspot.id,
                titleEn = "${directHotspot.name} — Official Tourism Poster",
                titleOr = "${directHotspot.odiaName} — ସରକାରୀ ପର୍ଯ୍ୟଟନ ପୋଷ୍ଟର",
                category = PamphletCategory.TOURISM,
                issuingAuthorityEn = "Odisha Tourism & District Tourist Office, Balasore",
                issuingAuthorityOr = "ଓଡ଼ିଶା ପର୍ଯ୍ୟଟନ ଓ ବାଲେଶ୍ୱର ଜିଲ୍ଲା ପର୍ଯ୍ୟଟନ କାର୍ଯ୍ୟାଳୟ",
                summaryEn = directHotspot.description,
                summaryOr = directHotspot.odiaName,
                fullContentEn = "${directHotspot.detailedDescription}\n\nHistorical Significance:\n${directHotspot.historicalSignificance}",
                fullContentOr = directHotspot.detailedDescription,
                keyDirectivesEn = listOf(
                    "Operating Hours: ${directHotspot.operatingHours.ifBlank { directHotspot.timing }}",
                    "Location: ${directHotspot.location} (${directHotspot.distanceKm} km)",
                    "Visitor Rating: ★ ${directHotspot.rating} (${directHotspot.reviewCount} reviews)"
                ),
                keyDirectivesOr = listOf(
                    "ଦର୍ଶନ ସମୟ: ${directHotspot.timing}",
                    "ଅବସ୍ଥିତି: ${directHotspot.location}"
                ),
                emergencyContactNumbers = listOf("06782-262066", "112"),
                officialHotlineLabel = "Balasore District Tourist Help Desk",
                latitude = directHotspot.latitude,
                longitude = directHotspot.longitude,
                locationName = directHotspot.location,
                qrPayload = "balasore360://hotspot/${directHotspot.id}",
                officialReferenceNumber = "ODT-BLS-HSP-${directHotspot.id.take(4).uppercase()}",
                linkedHotspotId = directHotspot.id
            )
            return QrScanResult(
                rawValue = trimmed,
                matchedPamphlet = synthesizedPamphlet,
                isOfficialBalasorePamphlet = true,
                detectedType = QrPayloadType.OFFICIAL_PAMPHLET,
                matchedHotspot = directHotspot,
                matchedEventDate = "Open Daily",
                matchedEventStageTime = directHotspot.operatingHours.ifBlank { directHotspot.timing },
                matchedAppTargetType = "HOTSPOT"
            )
        }

        // 2. Emergency phone number / tel URI
        if (trimmed.startsWith("tel:") || trimmed.matches(Regex("^[0-9+\\- ]{3,14}$"))) {
            return QrScanResult(
                rawValue = trimmed,
                isOfficialBalasorePamphlet = false,
                detectedType = QrPayloadType.EMERGENCY_HOTLINE
            )
        }

        // 3. Geo URI
        if (trimmed.startsWith("geo:") || trimmed.contains("maps.google.com") || trimmed.contains("goo.gl/maps")) {
            return QrScanResult(
                rawValue = trimmed,
                isOfficialBalasorePamphlet = false,
                detectedType = QrPayloadType.GEO_COORDINATES
            )
        }

        // 4. URL
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return QrScanResult(
                rawValue = trimmed,
                isOfficialBalasorePamphlet = false,
                detectedType = QrPayloadType.OFFICIAL_URL
            )
        }

        return QrScanResult(
            rawValue = trimmed,
            isOfficialBalasorePamphlet = false,
            detectedType = QrPayloadType.TEXT
        )
    }
}
