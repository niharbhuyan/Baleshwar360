package com.example.data.model

/**
 * Data model representing a historical landmark with curated multi-language
 * audio guide narration scripts for text-to-speech voice playback.
 */
data class HistoricalLandmarkAudioGuide(
    val id: String,
    val name: String,
    val odiaName: String,
    val hindiName: String,
    val category: String,
    val era: String,
    val location: String,
    val audioScriptEn: String,
    val audioScriptOr: String,
    val audioScriptHi: String,
    val durationSeconds: Int,
    val keyHighlights: List<String>,
    val associatedHotspotId: String? = null
)

/**
 * Catalog of historical landmarks across Balasore district with informative,
 * concise audio narration scripts crafted for text-to-speech audio guides.
 */
object HistoricalLandmarksCatalog {

    val landmarks: List<HistoricalLandmarkAudioGuide> = listOf(
        HistoricalLandmarkAudioGuide(
            id = "khirachora",
            name = "Khirachora Gopinatha Temple",
            odiaName = "କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର",
            hindiName = "क्षीरचोरा गोपीनाथ मंदिर, रेमुणा",
            category = "Ancient Temple",
            era = "10th–12th Century CE • Ganga Dynasty",
            location = "Remuna, 9 km from Balasore City",
            audioScriptEn = "Welcome to the historic Khirachora Gopinatha Temple in Remuna, dating back over eight centuries. Revered across eastern India, this sacred Vaishnavite shrine is famed for the legend of Lord Krishna affectionately stealing a pot of delicious condensed milk, known as Amruta Keli, for His devoted ascetic follower Madhavendra Puri. The great sixteenth-century saint Sri Chaitanya Mahaprabhu worshipped here on his divine journey to Puri. As you step into the stone sanctum, admire the black chlorite idol of Lord Gopinatha playing the flute, surrounded by the sweet aroma of freshly prepared thickened milk bhoga offered daily.",
            audioScriptOr = "ବାଲେଶ୍ୱରର ପ୍ରସିଦ୍ଧ କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ପୀଠ ରେମୁଣାକୁ ଆପଣଙ୍କୁ ସ୍ୱାଗତ। ଦ୍ୱାଦଶ ଶତାବ୍ଦୀର ଏହି ପ୍ରାଚୀନ ଶ୍ରୀକୃଷ୍ଣ ମନ୍ଦିର ଏହାର ଅମୃତ କେଳି ଭୋଗ ଏବଂ ଭକ୍ତ ମାଧବେନ୍ଦ୍ର ପୁରୀଙ୍କ ପାଇଁ ପ୍ରଭୁଙ୍କ କ୍ଷୀର ଚୋରି ଲୀଳା ପାଇଁ ବିଶ୍ୱବିଦିତ। ଶ୍ରୀ ଚୈତନ୍ୟ ମହାପ୍ରଭୁ ଏଠାକୁ ଆସି ପୂଜାର୍ଚ୍ଚନା କରିଥିଲେ। ମନ୍ଦିରର ଶାନ୍ତ ପରିବେଶ ଓ କଳିଙ୍ଗ ସ୍ଥାପତ୍ୟ କଳା ଭକ୍ତମାନଙ୍କୁ ଭକ୍ତିରସରେ ଆପ୍ଳୁତ କରେ।",
            audioScriptHi = "रेमुणा स्थित ऐतिहासिक क्षीरचोरा गोपीनाथ मंदिर में आपका स्वागत है। बारहवीं शताब्दी का यह पावन धाम भगवान श्रीकृष्ण द्वारा अपने परम भक्त माधवेन्द्र पुरी के लिए स्वादिष्ट खीर चुराने की अमर गाथा के लिए प्रसिद्ध है। यहाँ प्रतिदिन तैयार होने वाला अमृत केली भोग और कलिंग शैली की प्राचीन वास्तुकला अत्यंत दर्शनीय है।",
            durationSeconds = 54,
            keyHighlights = listOf("Amruta Keli Prasad", "Madhavendra Puri Legend", "Chaitanya Mahaprabhu Visit", "Ganga Dynasty Architecture"),
            associatedHotspotId = "khirachora"
        ),
        HistoricalLandmarkAudioGuide(
            id = "chandipur",
            name = "Chandipur Beach & Vanishing Sea",
            odiaName = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଓ ଲୁଚକାଳି ସମୁଦ୍ର",
            hindiName = "चांदीपुर समुद्र तट एवं लुप्त होता सागर",
            category = "Coastal Marvel",
            era = "Natural Marine Phenomenon & Defense Hub",
            location = "Chandipur, 16 km from Balasore Station",
            audioScriptEn = "You are standing before one of the natural wonders of the Indian coastline: Chandipur Beach. Twice every day during low tide, the Bay of Bengal recedes up to five kilometers into the horizon, leaving behind a vast, glistening seabed. Visitors can safely walk or cycle upon the exposed ocean floor to discover rare horseshoe crabs and colorful seashells. Chandipur is also historically celebrated as India's premier missile testing launchpad, where Dr. APJ Abdul Kalam guided historic defense milestones like Agni and Prithvi from the nearby Integrated Test Range.",
            audioScriptOr = "ଭାରତର ଏକ ଅନନ୍ୟ ପ୍ରାକୃତିକ ବିସ୍ମୟ ଚାନ୍ଦିପୁର ବେଳାଭୂମିକୁ ସ୍ୱାଗତ। ପ୍ରତ୍ୟେକ ଦିନ ଭଟ୍ଟା ସମୟରେ ଏଠାରେ ସମୁଦ୍ର ପ୍ରାୟ ପାଞ୍ଚ କିଲୋମିଟର ପଛକୁ ଘୁଞ୍ଚିଯାଏ, ଯାହା ପର୍ଯ୍ୟଟକଙ୍କୁ ସମୁଦ୍ର ଗର୍ଭରେ ଚାଲିବାର ଏକ ଅଭୁଲା ଅନୁଭୂତି ପ୍ରଦାନ କରେ। ଏଥିସହ ଚାନ୍ଦିପୁର ହେଉଛି ଭାରତର କ୍ଷେପଣାସ୍ତ୍ର ପରୀକ୍ଷଣର ଐତିହାସିକ ଭୂମି, ଯେଉଁଠାରେ ଡ଼କ୍ଟର ଏ.ପି.ଜେ. ଅବ୍ଦୁଲ କଲାମ ଅଗ୍ନି ଓ ପୃଥ୍ବୀ କ୍ଷେପଣାସ୍ତ୍ର ପରୀକ୍ଷାର ମାର୍ଗଦର୍ଶନ କରିଥିଲେ।",
            audioScriptHi = "चांदीपुर बीच पर आपका स्वागत है। यहाँ प्रतिदिन भाटे के समय समुद्र का पानी लगभग 5 किलोमीटर पीछे चला जाता है। पर्यटक सूखे समुद्री धरातल पर चलकर दुर्लभ समुद्री जीवों को देख सकते हैं। यह स्थान भारत के मिसाइल परीक्षण केंद्र और डॉ. कलाम के रक्षा अनुसंधान योगदान के लिए भी प्रसिद्ध है।",
            durationSeconds = 52,
            keyHighlights = listOf("5 km Tidal Recession", "Horseshoe Crab Habitat", "Integrated Test Range Heritage", "Dr. APJ Abdul Kalam Legacy"),
            associatedHotspotId = "chandipur"
        ),
        HistoricalLandmarkAudioGuide(
            id = "emami_jagannath",
            name = "Emami Jagannath Temple",
            odiaName = "ଇମାମି ଜଗନ୍ନାଥ ମନ୍ଦିର",
            hindiName = "इमामी जगन्नाथ मंदिर, बालेश्वर",
            category = "Spiritual Architecture",
            era = "Modern Kalinga Sandstone Marvel",
            location = "Remuna Januganj Road, Balasore",
            audioScriptEn = "Rising seventy-eight feet into the sky, the Emami Jagannath Temple in Balasore is a masterwork of classical Kalinga temple architecture. Constructed using authentic hand-carved sandstone from Odisha and Rajasthan quarries, the temple faithfully mirrors the sacred proportions of the iconic Puri Jagannath temple. The complex features manicured spiritual gardens, a musical fountain, and magnificent statues depicting the divine pastimes of the Dasavatara. At dusk, golden illuminations transform the stone carvings into a radiant beacon of peace.",
            audioScriptOr = "ରେମୁଣା ରୋଡ୍‌ରେ ପ୍ରତିଷ୍ଠିତ ଏମାମି ଜଗନ୍ନାଥ ମନ୍ଦିର କଳିଙ୍ଗ ସ୍ଥାପତ୍ୟ କଳାର ଏକ ଚିତ୍ତାକର୍ଷକ ନିଦର୍ଶନ। ୭୮ ଫୁଟ ଉଚ୍ଚତା ବିଶିଷ୍ଟ ଏହି ପଥର ମନ୍ଦିର ପୁରୀ ଶ୍ରୀମନ୍ଦିର ଶୈଳୀରେ ନିର୍ମିତ ହୋଇଛି। ସୁନ୍ଦର ଉଦ୍ୟାନ, ସଙ୍ଗୀତମୟ ଝରଣା ଏବଂ ପ୍ରଭୁ ଜଗନ୍ନାଥ, ବଳଭଦ୍ର ଓ ଦେବୀ ସୁଭଦ୍ରାଙ୍କ ଦିବ୍ୟ ଦର୍ଶନ ଏଠାରେ ଶ୍ରଦ୍ଧାଳୁମାନଙ୍କୁ ଭକ୍ତି ଓ ଶାନ୍ତି ପ୍ରଦାନ କରେ।",
            audioScriptHi = "इमामी जगन्नाथ मंदिर बालेश्वर का एक भव्य स्थापत्य चमत्कार है। 78 फीट ऊंचा यह बलुआ पत्थर का मंदिर पुरी के श्रीमंदिर की वास्तुशैली को जीवंत करता है। सुंदर फव्वारे और संगीतमय उद्यान वातावरण को अत्यंत आध्यात्मिक बनाते हैं।",
            durationSeconds = 48,
            keyHighlights = listOf("78-ft Sandstone Shikhara", "Puri Srimandir Proportions", "Musical Fountain Gardens", "Dasavatara Sculptures"),
            associatedHotspotId = "emami_jagannath"
        ),
        HistoricalLandmarkAudioGuide(
            id = "panchalingeswar",
            name = "Panchalingeswar Temple",
            odiaName = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଶୈବ ପୀଠ",
            hindiName = "पंचलिंगेश्वर मंदिर, नीलगिरि",
            category = "Ancient Temple",
            era = "Treta Yuga Lore & Nilagiri Hills",
            location = "Devagiri Hill, Nilagiri, Balasore",
            audioScriptEn = "Perched amid the lush sal forests of Devagiri Hill in Nilagiri, Panchalingeswar is a sanctuary where sacred spirituality unites with pristine nature. Here, five natural granite Shiva Lingams lie submerged beneath a clear, continuous mountain spring. Devotees ascend over two hundred stone steps through dense hill greenery to reach the rock face, where they can touch the submerged lingams under the flowing cold stream. Legend recounts that King Banasura installed these lingams to perform penance to Lord Shiva.",
            audioScriptOr = "ନୀଳଗିରିର ଦେବଗିରି ପାହାଡ଼ ଉପରେ ଅବସ୍ଥିତ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଏକ ପବିତ୍ର ପ୍ରାକୃତିକ ଶୈବ ପୀଠ। ଏଠାରେ ପାହାଡ଼ର ଶୀତଳ ପ୍ରବାହିତ ଝରଣା ଜଳ ମଧ୍ୟରେ ପାଞ୍ଚଟି ପ୍ରାକୃତିକ ଶିବଲିଙ୍ଗ ସର୍ବଦା ନିମଜ୍ଜିତ ରହିଥାନ୍ତି। ଭକ୍ତମାନେ ଶାଳ ବଣ ମଧ୍ୟ ଦେଇ ପାହାଚ ଚଢ଼ି ପ୍ରବାହିତ ଜଳଧାରା ମଧ୍ୟରେ ଶିବଲିଙ୍ଗ ସ୍ପର୍ଶ କରି ଆଶୀର୍ବାଦ ଲାଭ କରନ୍ତି।",
            audioScriptHi = "नीलगिरि की हरी-भरी पहाड़ियों पर स्थित पंचलिंगेश्वर मंदिर प्रकृति और श्रद्धा का संगम है। यहाँ बहते हुए पहाड़ी झरने के नीचे पांच प्राकृतिक शिवलिंग स्थापित हैं, जिन्हें श्रद्धालु बहते जल में स्पर्श कर पूजन करते हैं।",
            durationSeconds = 50,
            keyHighlights = listOf("Perennial Hill Spring", "Five Submerged Lingams", "Devagiri Hill Trek", "Banasura Legend"),
            associatedHotspotId = "panchalingeswar"
        ),
        HistoricalLandmarkAudioGuide(
            id = "bagha_jatin",
            name = "Bagha Jatin Freedom Memorial, Chasakhand",
            odiaName = "ବାଘା ଯତୀନ ସ୍ମୃତିପୀଠ, ଚାଷାଖଣ୍ଡ",
            hindiName = "बाघा जतिन शहीद स्मारक, चाषाखंड",
            category = "Freedom & Defense",
            era = "1915 Indian Independence Battle",
            location = "Chasakhand, on Budhabalanga River Bank",
            audioScriptEn = "You are listening to the heroic saga of Chasakhand on the banks of the Budhabalanga river. In September 1915, legendary Indian revolutionary Jatindranath Mukherjee, affectionately known as Bagha Jatin, along with four young comrades, fought an epic seventy-five minute gun battle against heavily armed British colonial forces. Armed only with Mauser pistols from a makeshift trench, they demonstrated unmatched bravery. Bagha Jatin attained martyrdom at the Balasore hospital, uttering the immortal words: 'Did I do my duty?' This memorial honors their supreme sacrifice.",
            audioScriptOr = "ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ କୂଳ ଚାଷାଖଣ୍ଡରେ ୧୯୧୫ ମସିହା ସେପ୍ଟେମ୍ବର ମାସରେ ସ୍ୱାଧୀନତା ସଂଗ୍ରାମୀ ବାଘା ଯତୀନ ଓ ତାଙ୍କ ସାଥୀମାନେ ବ୍ରିଟିଶ ଫୌଜ ସହ ସାହସିକତାର ସହ ଲଢ଼ିଥିଲେ। ମାଉଜର ପିସ୍ତଲ ସାହାଯ୍ୟରେ ସେମାନେ ଇଂରେଜ ସେନା ବିରୋଧରେ କରିଥିବା ଏହି ଐତିହାସିକ ସଂଗ୍ରାମ ଭାରତୀୟ ସ୍ୱାଧୀନତା ଆନ୍ଦୋଳନର ଏକ ଅଭୁଲା ଅଧ୍ୟାୟ।",
            audioScriptHi = "चाषाखंड में बुढ़ाबलंग नदी के तट पर क्रांतिकारी बाघा जतिन और उनके साथियों ने 1915 में ब्रिटिश सेना के खिलाफ ऐतिहासिक संघर्ष किया था। यह स्मारक उनके अदम्य साहस और देश के लिए दिए गए सर्वोच्च बलिदान का प्रतीक है।",
            durationSeconds = 55,
            keyHighlights = listOf("1915 Chasakhand Battle", "Budhabalanga Trench Fight", "Mauser Pistol Resistance", "Indian Revolutionary Heritage"),
            associatedHotspotId = "bagha_jatin"
        ),
        HistoricalLandmarkAudioGuide(
            id = "fakir_mohan",
            name = "Vyasakabi Fakir Mohan Shanti Kanan",
            odiaName = "ବ୍ୟାସକବି ଫକୀର ମୋହନ ଶାନ୍ତିକାନନ",
            hindiName = "व्यासकवि फकीर मोहन शांतिकानन",
            category = "Literary Heritage",
            era = "19th Century Odia Renaissance (1843–1918)",
            location = "Mallikashpur, Balasore City",
            audioScriptEn = "Step into Shanti Kanan, the peaceful mango grove where the father of modern Odia literature, Vyasakabi Fakir Mohan Senapati, lived and wrote. Born in Balasore in 1843, Senapati championed the survival of the Odia language during colonial pressures and set up the historic Utkal Press in 1868. His landmark novel 'Chha Mana Atha Guntha' pioneered social realism in Indian literature. Here in Shanti Kanan, literary greats met under the canopy of ancient trees to inspire a cultural awakening.",
            audioScriptOr = "ବାଲେଶ୍ୱରର ଶାନ୍ତିକାନନ ହେଉଛି ବ୍ୟାସକବି ଫକୀର ମୋହନ ସେନାପତିଙ୍କ ସାଧନା ପୀଠ। ୧୮୪୩ରେ ବାଲେଶ୍ୱରରେ ଜନ୍ମିତ ଫକୀର ମୋହନ ଓଡ଼ିଆ ଭାଷା ଆନ୍ଦୋଳନର ଅଗ୍ରଦୂତ ଥିଲେ। ତାଙ୍କ କାଳଜୟୀ ଉପନ୍ୟାସ 'ଛ ମାଣ ଆଠ ଗୁଣ୍ଠ' ଏବଂ ୧୮୬୮ରେ ପ୍ରତିଷ୍ଠିତ ଉତ୍କଳ ପ୍ରେସ୍ ଓଡ଼ିଆ ଜାତିର ସ୍ୱାଭିମାନକୁ ନୂତନ ଜୀବନ ଦେଇଥିଲା।",
            audioScriptHi = "शांतिकानन वह पावन स्थल है जहाँ आधुनिक ओड़िया साहित्य के जनक व्यासकवि फकीर मोहन सेनापति ने अपनी अमर कृतियाँ रचीं। उनके उपन्यास 'छह एकड़ और एक तिहाई' ने भारतीय साहित्य में सामाजिक यथार्थवाद की नींव रखी।",
            durationSeconds = 49,
            keyHighlights = listOf("Father of Odia Prose", "1868 Utkal Press", "Chha Mana Atha Guntha", "Mallikashpur Memorial"),
            associatedHotspotId = "fakir_mohan"
        ),
        HistoricalLandmarkAudioGuide(
            id = "talasari",
            name = "Talasari Beach & Subarnarekha Delta",
            odiaName = "ତାଳସାରୀ ବେଳାଭୂମି ଓ ସୁବର୍ଣ୍ଣରେଖା ମୁହାଣ",
            hindiName = "तालसारी समुद्र तट एवं सुवर्णरेखा मुहाना",
            category = "Coastal Marvel",
            era = "Ancient Maritime Trade & Estuary",
            location = "Bhograi Block, 85 km from Balasore",
            audioScriptEn = "Talasari Beach is a tranquil coastal retreat bordered by swaying palm and casuarina groves where the sacred Subarnarekha river meets the Bay of Bengal. Famous for millions of tiny red ghost crabs that carpet the shore at low tide, Talasari offers peaceful sandbars reachable by local wooden country boats. In ancient times, the Subarnarekha delta served as a thriving maritime passage for Kalinga merchants sailing to Southeast Asia.",
            audioScriptOr = "ତାଳସାରୀ ବେଳାଭୂମି ଏହାର ଶାନ୍ତ ନୀଳ ସମୁଦ୍ର, ଝାଉଁ ଓ ନଡ଼ିଆ ବଣ ତଥା ଲାଲ କଙ୍କଡ଼ାଙ୍କ ଚଳପ୍ରଚଳ ପାଇଁ ବିଖ୍ୟାତ। ସୁବର୍ଣ୍ଣରେଖା ନଦୀ ଓ ବଙ୍ଗୋପସାଗରର ମିଳନ ସ୍ଥଳୀରେ ଥିବା ଏହି ବେଳାଭୂମି ପ୍ରାଚୀନ କାଳରେ କଳିଙ୍ଗର ନୌବାଣିଜ୍ୟର ପ୍ରମୁଖ କେନ୍ଦ୍ର ଥିଲା।",
            audioScriptHi = "तालसारी बीच सुवर्णरेखा नदी और बंगाल की खाड़ी का शांत मुहाना है। यहाँ लाल केकड़ों की बहुतायत और शांत बालू के टीले पर्यटकों को मंत्रमुग्ध कर देते हैं।",
            durationSeconds = 44,
            keyHighlights = listOf("Red Ghost Crabs", "Subarnarekha River Delta", "Country Boat Rides", "Ancient Kalinga Maritime Trade"),
            associatedHotspotId = "talasari"
        ),
        HistoricalLandmarkAudioGuide(
            id = "chandaneswar",
            name = "Chandaneswar Shiva Temple",
            odiaName = "ଚନ୍ଦନେଶ୍ୱର ଶିବ ମନ୍ଦିର",
            hindiName = "चंदनेश्वर शिव मंदिर, भोगराई",
            category = "Ancient Temple",
            era = "Ancient Shaivite Pilgrimage",
            location = "Bhograi Block, near Odisha-Bengal border",
            audioScriptEn = "Dedicated to Lord Shiva, the sacred Chandaneswar Temple in Bhograi is celebrated for the massive annual Chadak Mela and Uda Parba held during the Odia month of Chaitra. Millions of devotees, known as Bhaktas, undertake intense spiritual penance, walking upon hot coals and participating in age-old ascetic rites. The temple's sacred pond and peaceful courtyard represent a centuries-old cultural bridge connecting the communities of coastal Odisha and Bengal.",
            audioScriptOr = "ଭୋଗରାଇ ବ୍ଲକରେ ଅବସ୍ଥିତ ଚନ୍ଦନେଶ୍ୱର ଶିବ ମନ୍ଦିର ଏହାର ପ୍ରସିଦ୍ଧ ଚୈତ୍ର ଚଡ଼କ ମେଳା ଓ ଉଡ଼ା ପର୍ବ ପାଇଁ ପ୍ରସିଦ୍ଧ। ଲକ୍ଷ ଲକ୍ଷ ଭକ୍ତ ଏଠାକୁ ଆସି କଠୋର ବ୍ରତ ପାଳନ କରନ୍ତି। ଏହି ପ୍ରାଚୀନ ଶୈବ ପୀଠ ଓଡ଼ିଶା ଓ ପଶ୍ଚିମବଙ୍ଗର ଭକ୍ତମାନଙ୍କ ଏକ ପ୍ରମୁଖ ମିଳନ କେନ୍ଦ୍ର।",
            audioScriptHi = "भोगराई में स्थित चंदनेश्वर शिव मंदिर अपने प्रसिद्ध चड़क मेले और उदा पर्व के लिए प्रसिद्ध है। चैत्र मास में लाखों श्रद्धालु यहाँ कठोर तपस्या और दर्शन के लिए आते हैं।",
            durationSeconds = 46,
            keyHighlights = listOf("Chadak Mela & Uda Parba", "Sacred Temple Pond", "Ancient Shaivite Traditions", "Odisha-Bengal Cultural Harmony"),
            associatedHotspotId = "chandaneswar"
        ),
        HistoricalLandmarkAudioGuide(
            id = "bhusandeshwar",
            name = "Bhusandeshwar Temple & Giant Monolith",
            odiaName = "ଭୂଷଣ୍ଡେଶ୍ୱର ମହାଦେବ ମନ୍ଦିର",
            hindiName = "भूषण्डेश्वर मंदिर, विशाल शिवलिंग",
            category = "Ancient Temple",
            era = "Ancient Megalithic Black Granite",
            location = "Bhograi Block, 100 km from Balasore",
            audioScriptEn = "Bhusandeshwar Temple houses one of the largest black granite Shiva Lingams in the entire Asian continent. Measuring over twelve feet in height and twelve feet in diameter, this colossal monolithic stone lingam is steeped in Treta Yuga mythology. Legend says demon king Ravana was gifted this massive lingam by Lord Shiva to carry to Lanka, but set it down here upon the soil of Balasore, after which it became permanently rooted into the earth.",
            audioScriptOr = "ଭୂଷଣ୍ଡେଶ୍ୱର ମନ୍ଦିରରେ ଏସିଆର ଅନ୍ୟତମ ବୃହତ୍ତମ କଳା ମୁଗୁନି ପଥରର ଶିବଲିଙ୍ଗ ପ୍ରତିଷ୍ଠିତ। ପ୍ରାୟ ୧୨ ଫୁଟ ଉଚ୍ଚତା ବିଶିଷ୍ଟ ଏହି ବିଶାଳ ଲିଙ୍ଗ ସହ ରାବଣଙ୍କ ତ୍ରେତୟା ଯୁଗୀୟ ପୌରାଣିକ କିମ୍ବଦନ୍ତୀ ଜଡ଼ିତ। ଏଠାରେ ପୂଜାର୍ଚ୍ଚନା କଲେ ମନସ୍କାମନା ପୂରଣ ହେଉଥିବା ବିଶ୍ୱାସ ରହିଛି।",
            audioScriptHi = "भूषण्डेश्वर मंदिर में एशिया का एक विशालतम काला ग्रेनाइट शिवलिंग स्थापित है जिसकी ऊंचाई लगभग 12 फीट है। पौराणिक मान्यतानुसार रावण ने इस शिवलिंग को लंका ले जाते समय यहाँ स्थापित किया था।",
            durationSeconds = 47,
            keyHighlights = listOf("12-Foot Monolithic Lingam", "Asia's Largest Black Granite Lingam", "Treta Yuga Ravana Legend"),
            associatedHotspotId = "bhusandeshwar"
        ),
        HistoricalLandmarkAudioGuide(
            id = "kuldiha",
            name = "Kuldiha Wildlife Sanctuary & Rissia",
            odiaName = "କୁଲଡିହା ବନ୍ୟପ୍ରାଣୀ ଅଭୟାରଣ୍ୟ",
            hindiName = "कुलडीहा वन्यजीव अभयारण्य",
            category = "Eco-Heritage",
            era = "Nilagiri Forest Biosphere Reserve",
            location = "Nilagiri range, 45 km from Balasore",
            audioScriptEn = "Spread across two hundred and seventy square kilometers of dense sal and deciduous hill forests, Kuldiha Wildlife Sanctuary is a vital ecological lifeline of northern Odisha. It serves as a historical corridor for wild Asian elephants travelling between Similipal and Jharkhand. Travelers can stay at the Rissia nature camp, explore serene forest watchtowers, and watch elephant herds gathered peacefully at evening salt licks.",
            audioScriptOr = "କୁଲଡିହା ବନ୍ୟପ୍ରାଣୀ ଅଭୟାରଣ୍ୟ ନୀଳଗିରିର ୨୭୨ ବର୍ଗ କିଲୋମିଟର ଘଞ୍ଚ ଶାଳ ଜଙ୍ଗଲରେ ବ୍ୟାପ୍ତ। ଏହା ଏସୀୟ ହାତୀ, କଲରାପତରିଆ ବାଘ ଓ ବିରଳ ପକ୍ଷୀଙ୍କ ବାସସ୍ଥଳୀ। ଋଷିଆ ନେଚର କ୍ୟାମ୍ପ ଓ ୱାଚ୍ ଟାୱାରରୁ ବନ୍ୟପ୍ରାଣୀ ଦର୍ଶନ ଏକ ରୋମାଞ୍ଚକର ଅନୁଭୂତି ପ୍ରଦାନ କରେ।",
            audioScriptHi = "कुलडीहा वन्यजीव अभयारण्य नीलगिरि की पहाड़ियों में हाथियों और विविध वन्यजीवों का सुंदर प्राकृतिक आवास है। यहाँ का ऋषिया नेचर कैंप प्रकृति प्रेमियों के लिए विशेष आकर्षण है।",
            durationSeconds = 45,
            keyHighlights = listOf("Asian Elephant Corridor", "Rissia Nature Camp", "Similipal Biosphere Link", "Sal Forest Canopy"),
            associatedHotspotId = "kuldiha"
        )
    )

    fun findById(id: String): HistoricalLandmarkAudioGuide? {
        return landmarks.find { it.id.equals(id, ignoreCase = true) }
    }

    fun findByHotspotId(hotspotId: String): HistoricalLandmarkAudioGuide? {
        return landmarks.find {
            it.associatedHotspotId.equals(hotspotId, ignoreCase = true) ||
            it.id.equals(hotspotId, ignoreCase = true)
        }
    }
}
