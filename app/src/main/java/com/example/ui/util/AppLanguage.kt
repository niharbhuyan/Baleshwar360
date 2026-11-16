package com.example.ui.util

import com.example.data.model.AppLanguage

typealias AppLanguage = com.example.data.model.AppLanguage

/**
 * Centralized multi-language dictionary for real-time UI localization across
 * all 22 Eighth Schedule Indian languages plus English.
 */
object AppStrings {

    fun appName(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବାଲେଶ୍ୱର ୩୬୦"
        AppLanguage.HINDI -> "बालेश्वर ३६०"
        AppLanguage.BENGALI -> "বালেশ্বর ৩৬০"
        AppLanguage.TELUGU -> "బాలేశ్వర్ 360"
        AppLanguage.TAMIL -> "பாலேஸ்வர் 360"
        AppLanguage.MARATHI -> "बाळेश्वर ३६०"
        AppLanguage.GUJARATI -> "બાલેશ્વર ૩૬૦"
        AppLanguage.KANNADA -> "ಬಾಲೇಶ್ವರ 360"
        AppLanguage.MALAYALAM -> "ബാലേശ്വർ 360"
        AppLanguage.PUNJABI -> "ਬਾਲੇਸ਼ਵਰ ੩੬੦"
        AppLanguage.ASSAMESE -> "বালেশ্বৰ ৩৬০"
        AppLanguage.MAITHILI -> "बालेश्वर ३६०"
        AppLanguage.SANTALI -> "ᱵᱟᱞᱮᱥᱚᱨ ᱓᱖᱐"
        AppLanguage.SANSKRIT -> "बालेश्वरः ३६०"
        AppLanguage.URDU -> "بالیشور ۳۶۰"
        AppLanguage.NEPALI -> "बालेश्वर ३६०"
        AppLanguage.KONKANI -> "बाळेश्वर ३६०"
        AppLanguage.SINDHI -> "बालेश्वर ३६०"
        AppLanguage.DOGRI -> "बालेश्वर ३६०"
        AppLanguage.KASHMIRI -> "बालेश्वर ३६०"
        AppLanguage.MANIPURI -> "বালেশ্বর ৩৬০"
        AppLanguage.BODO -> "बालेश्वर ३६०"
        else -> "Baleshwar 360"
    }

    fun brandName(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ନିହାର ସେଲ୍ସ"
        AppLanguage.HINDI -> "निहार सेल्स"
        AppLanguage.BENGALI -> "নিহার সেলস"
        AppLanguage.TELUGU -> "నిహార్ సేల్స్"
        AppLanguage.TAMIL -> "நிஹார் சேல்ஸ்"
        AppLanguage.MARATHI -> "निहार सेल्स"
        AppLanguage.GUJARATI -> "નિહાર સેલ્સ"
        AppLanguage.KANNADA -> "ನಿಹಾರ್ ಸೇಲ್ಸ್"
        AppLanguage.MALAYALAM -> "നിഹാർ സെയിൽസ്"
        AppLanguage.PUNJABI -> "ਨਿਹਾਰ ਸੇਲਜ਼"
        AppLanguage.ASSAMESE -> "নিহাৰ চেলছ"
        AppLanguage.SANTALI -> "ᱱᱤᱦᱟᱨ ᱥᱮᱞᱥ"
        AppLanguage.URDU -> "نہار سیلز"
        AppLanguage.SANSKRIT -> "निहार विक्रयणम्"
        else -> "Nihar Sales"
    }

    fun appSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବାଲେଶ୍ୱର ଡିଜିଟାଲ୍ ସୂଚନା ଓ ପର୍ଯ୍ୟଟନ ପୋର୍ଟାଲ୍"
        AppLanguage.HINDI -> "बालेश्वर डिजिटल हब एवं पर्यटन पोर्टल"
        AppLanguage.BENGALI -> "বালেশ্বর ডিজিটাল হাব ও পর্যটন পোর্টাল"
        AppLanguage.TELUGU -> "బాలేశ్వర్ డిజిటల్ హబ్ & పర్యాటక పోర్టల్"
        AppLanguage.TAMIL -> "பாலேஸ்வர் டிஜிட்டல் மையம் & சுற்றுலா போர்டல்"
        AppLanguage.MARATHI -> "बाळेश्वर डिजिटल हब आणि पर्यटन पोर्टल"
        AppLanguage.GUJARATI -> "બાલેશ્વર ડિજિટલ હબ અને પ્રવાસન પોર્ટલ"
        AppLanguage.KANNADA -> "ಬಾಲೇಶ್ವರ ಡಿಜಿಟಲ್ ಹಬ್ & ಪ್ರವಾಸೋದ್ಯಮ ಪೋರ್ಟಲ್"
        AppLanguage.MALAYALAM -> "ബാലേശ്വർ ഡിജിറ്റൽ ഹബ് & ടൂറിസം പോർട്ടൽ"
        AppLanguage.PUNJABI -> "ਬਾਲੇਸ਼ਵਰ ਡਿਜੀਟਲ ਹੱਬ ਅਤੇ ਸੈਰ-ਸਪਾਟਾ ਪੋਰਟਲ"
        AppLanguage.ASSAMESE -> "বালেশ্বৰ ডিজিটেল হাব আৰু পৰ্যটন পৰ্টেল"
        AppLanguage.SANTALI -> "ᱵᱟᱞᱮᱥᱚᱨ ᱰᱤᱡᱤᱴᱟᱞ ᱦᱟᱵᱽ ᱟᱨ ᱫᱟᱬᱟᱱ ᱯᱚᱨᱴᱟᱞ"
        AppLanguage.URDU -> "بالیشور ڈیجیٹل ہب اور سیاحتی پورٹل"
        else -> "Digital Hub & Tourism Portal"
    }

    // Navigation Tabs
    fun tabTourism(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ପର୍ଯ୍ୟଟନ"
        AppLanguage.HINDI -> "पर्यटन स्थल"
        AppLanguage.BENGALI -> "পর্যটন"
        AppLanguage.TELUGU -> "పర్యాటకం"
        AppLanguage.TAMIL -> "சுற்றுலா"
        AppLanguage.MARATHI -> "पर्यटन"
        AppLanguage.GUJARATI -> "પ્રવાસન"
        AppLanguage.KANNADA -> "ಪ್ರವಾಸೋದ್ಯಮ"
        AppLanguage.MALAYALAM -> "ടൂറിസം"
        AppLanguage.PUNJABI -> "ਸੈਰ-ਸਪਾਟਾ"
        AppLanguage.ASSAMESE -> "পৰ্যটন"
        AppLanguage.SANTALI -> "ᱫᱟᱬᱟᱱ ᱴᱷᱟᱶ"
        AppLanguage.URDU -> "سیاحت"
        AppLanguage.SANSKRIT -> "पर्यटनम्"
        else -> "Tourism"
    }

    fun tabNews(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବାଲେଶ୍ୱର ଖବର"
        AppLanguage.HINDI -> "ताज़ा समाचार"
        AppLanguage.BENGALI -> "বালেশ্বর খবর"
        AppLanguage.TELUGU -> "తాజా వార్తలు"
        AppLanguage.TAMIL -> "செய்திகள்"
        AppLanguage.MARATHI -> "बातम्या"
        AppLanguage.GUJARATI -> "સમાચાર"
        AppLanguage.KANNADA -> "ಸುದ್ದಿ"
        AppLanguage.MALAYALAM -> "വാർത്തകൾ"
        AppLanguage.PUNJABI -> "ਖ਼ਬਰਾਂ"
        AppLanguage.ASSAMESE -> "বাতিৰি"
        AppLanguage.SANTALI -> "ᱠᱷᱚᱵᱚᱨ"
        AppLanguage.URDU -> "خبریں"
        AppLanguage.SANSKRIT -> "वार्ताः"
        else -> "Local News"
    }

    fun tabWeather(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ପାଣିପାଗ ଓ ସତର୍କତା"
        AppLanguage.HINDI -> "मौसम एवं अलर्ट"
        AppLanguage.BENGALI -> "আবহাওয়া ও সতর্কতা"
        AppLanguage.TELUGU -> "వాతావరణం & అలర్ట్"
        AppLanguage.TAMIL -> "வானிலை & எச்சரிக்கைகள்"
        AppLanguage.MARATHI -> "हवामान आणि सतर्कता"
        AppLanguage.GUJARATI -> "હવામાન અને એલર્ટ"
        AppLanguage.KANNADA -> "ಹವಾಮಾನ & ಎಚ್ಚರಿಕೆಗಳು"
        AppLanguage.MALAYALAM -> "കാലാവസ്ഥ & ജാഗ്രത"
        AppLanguage.PUNJABI -> "ਮੌਸਮ ਅਤੇ ਅਲਰਟ"
        AppLanguage.ASSAMESE -> "বতৰ আৰু সতৰ্কতা"
        AppLanguage.SANTALI -> "ᱦᱚᱭ-ᱦᱤᱥᱤᱫ ᱟᱨ ᱥᱟᱛᱟᱨ"
        AppLanguage.URDU -> "موسم اور الرٹ"
        AppLanguage.SANSKRIT -> "ऋतुमानं सूचना च"
        else -> "Weather & Alerts"
    }

    fun tabEssentials(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଜରୁରୀ ସେବା"
        AppLanguage.HINDI -> "आवश्यक सेवाएं"
        AppLanguage.BENGALI -> "জরুরি সেবা"
        AppLanguage.TELUGU -> "అత్యవసర సేవలు"
        AppLanguage.TAMIL -> "அத்தியாவசிய சேவைகள்"
        AppLanguage.MARATHI -> "अत्यावश्यक सेवा"
        AppLanguage.GUJARATI -> "આવશ્યક સેવાઓ"
        AppLanguage.KANNADA -> "ಅಗತ್ಯ ಸೇವೆಗಳು"
        AppLanguage.MALAYALAM -> "അത്യാവശ്യ സേവനങ്ങൾ"
        AppLanguage.PUNJABI -> "ਜ਼ਰੂਰੀ ਸੇਵਾਵਾਂ"
        AppLanguage.ASSAMESE -> "জৰুৰী সেৱা"
        AppLanguage.SANTALI -> "ᱞᱟᱹᱠᱛᱤᱭᱟᱱ ᱥᱮᱣᱟ"
        AppLanguage.URDU -> "ضروری خدمات"
        AppLanguage.SANSKRIT -> "आवश्यकसेवाः"
        else -> "Directory & Info"
    }

    // Search and Filters
    fun searchHint(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଖବର, ବେଳାଭୂମି, ମନ୍ଦିର, ପାଣିପାଗ ଖୋଜନ୍ତୁ..."
        AppLanguage.HINDI -> "समाचार, समुद्र तट, मंदिर, मौसम खोजें..."
        AppLanguage.BENGALI -> "খবর, সৈকত, মন্দির, আবহাওয়া খুঁজুন..."
        AppLanguage.TELUGU -> "వార్తలు, బీచ్‌లు, దేవాలయాలు, వాతావరణం వెతకండి..."
        AppLanguage.TAMIL -> "செய்திகள், கடற்கரைகள், கோயில்கள், வானிலை தேடுக..."
        AppLanguage.MARATHI -> "बातम्या, किनारे, मंदिरे, हवामान शोधा..."
        AppLanguage.GUJARATI -> "સમાચાર, દરિયાકિનારા, મંદિરો, હવામાન શોધો..."
        AppLanguage.KANNADA -> "ಸುದ್ದಿ, ಕಡಲತೀರಗಳು, ದೇವಾಲಯಗಳು, ಹವಾಮಾನ ಹುಡುಕಿ..."
        AppLanguage.MALAYALAM -> "വാർത്തകൾ, ബീച്ചുകൾ, ക്ഷേത്രങ്ങൾ, കാലാവസ്ഥ തിരയുക..."
        AppLanguage.PUNJABI -> "ਖ਼ਬਰਾਂ, ਬੀਚ, ਮੰਦਰ ਅਤੇ ਮੌਸਮ ਖੋਜੋ..."
        AppLanguage.ASSAMESE -> "খবৰ, সমুদ্ৰতীৰ, মন্দিৰ সন্ধান কৰক..."
        AppLanguage.SANTALI -> "ᱠᱷᱚᱵᱚᱨ, ᱫᱚᱨᱭᱟ ᱟᱲᱮ, ᱢᱩᱸᱫᱤᱞ ᱥᱮᱸᱫᱽᱨᱟᱭ ᱢᱮ..."
        AppLanguage.URDU -> "خبریں، ساحل، مندر اور موسم تلاش کریں..."
        else -> "Search news, beaches, temples, weather..."
    }

    fun quickFilterLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଦ୍ରୁତ ଫିଲ୍ଟର୍:"
        AppLanguage.HINDI -> "त्वरित फ़िल्टर:"
        AppLanguage.BENGALI -> "দ্রুত ফিল্টার:"
        AppLanguage.TELUGU -> "త్వరిత ఫిల్టర్:"
        AppLanguage.TAMIL -> "விரைவு வடிகட்டி:"
        AppLanguage.MARATHI -> "जलद फिल्टर:"
        AppLanguage.GUJARATI -> "ઝડપી ફિલ્ટર:"
        AppLanguage.KANNADA -> "ತ್ವರಿತ ಫಿಲ್ಟರ್:"
        AppLanguage.MALAYALAM -> "ദ്രുത ഫിൽട്ടർ:"
        AppLanguage.PUNJABI -> "ਤੇਜ਼ ਫਿਲਟਰ:"
        AppLanguage.ASSAMESE -> "দ্ৰুত ফিল্টাৰ:"
        AppLanguage.SANTALI -> "ᱞᱚᱜᱚᱱ ᱯᱷᱤᱞᱴᱟᱨ:"
        AppLanguage.URDU -> "فوری فلٹر:"
        else -> "Quick filter:"
    }

    fun favorites(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ପସନ୍ଦିତା ସ୍ଥାନ"
        AppLanguage.HINDI -> "पसंदीदा स्थल"
        AppLanguage.BENGALI -> "প্রিয় স্থান"
        AppLanguage.TELUGU -> "ఇష్టమైన ప్రదేశాలు"
        AppLanguage.TAMIL -> "விருப்பமானவை"
        AppLanguage.MARATHI -> "आवडती ठिकाणे"
        AppLanguage.GUJARATI -> "પસંદગીના સ્થળો"
        AppLanguage.KANNADA -> "ಮೆಚ್ಚಿನವುಗಳು"
        AppLanguage.MALAYALAM -> "പ്രിയപ്പെട്ടവ"
        AppLanguage.PUNJABI -> "ਮਨਪਸੰਦ ਸਥਾਨ"
        AppLanguage.ASSAMESE -> "প্ৰিয় স্থান"
        AppLanguage.SANTALI -> "ᱠᱩᱥᱤᱭᱟᱱ ᱴᱷᱟᱶ"
        AppLanguage.URDU -> "پسندیدہ"
        else -> "Favorites"
    }

    fun allCategory(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ସମସ୍ତ"
        AppLanguage.HINDI -> "सभी"
        AppLanguage.BENGALI -> "সব"
        AppLanguage.TELUGU -> "అన్నీ"
        AppLanguage.TAMIL -> "அனைத்தும்"
        AppLanguage.MARATHI -> "सर्व"
        AppLanguage.GUJARATI -> "બધા"
        AppLanguage.KANNADA -> "ಎಲ್ಲಾ"
        AppLanguage.MALAYALAM -> "എല്ലാം"
        AppLanguage.PUNJABI -> "ਸਾਰੇ"
        AppLanguage.ASSAMESE -> "সকলো"
        AppLanguage.SANTALI -> "ᱡᱚᱛᱚ"
        AppLanguage.URDU -> "سب"
        else -> "All"
    }

    fun beachCategory(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବେଳାଭୂମି"
        AppLanguage.HINDI -> "समुद्र तट"
        AppLanguage.BENGALI -> "সমুদ্র সৈকত"
        AppLanguage.TELUGU -> "బీచ్"
        AppLanguage.TAMIL -> "கடற்கரை"
        AppLanguage.MARATHI -> "समुद्रकिनारा"
        AppLanguage.GUJARATI -> "દરિયાકિનારો"
        AppLanguage.KANNADA -> "ಕಡಲತೀರ"
        AppLanguage.MALAYALAM -> "ബീച്ച്"
        AppLanguage.PUNJABI -> "ਬੀਚ"
        AppLanguage.ASSAMESE -> "সমুদ্ৰতীৰ"
        AppLanguage.SANTALI -> "ᱫᱚᱨᱭᱟ ᱟᱲᱮ"
        AppLanguage.URDU -> "ساحل"
        else -> "Beach"
    }

    fun templeCategory(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ମନ୍ଦିର"
        AppLanguage.HINDI -> "मंदिर"
        AppLanguage.BENGALI -> "মন্দির"
        AppLanguage.TELUGU -> "దేవాలయం"
        AppLanguage.TAMIL -> "கோயில்"
        AppLanguage.MARATHI -> "मंदिर"
        AppLanguage.GUJARATI -> "મંદિર"
        AppLanguage.KANNADA -> "ದೇವಾಲಯ"
        AppLanguage.MALAYALAM -> "ക്ഷേത്രം"
        AppLanguage.PUNJABI -> "ਮੰਦਰ"
        AppLanguage.ASSAMESE -> "মন্দিৰ"
        AppLanguage.SANTALI -> "ᱢᱩᱸᱫᱤᱞ"
        AppLanguage.URDU -> "مندر"
        else -> "Temple"
    }

    fun wildlifeCategory(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବନ୍ୟପ୍ରାଣୀ"
        AppLanguage.HINDI -> "वन्यजीव"
        AppLanguage.BENGALI -> "বন্যপ্রাণী"
        AppLanguage.TELUGU -> "వన్యప్రాణులు"
        AppLanguage.TAMIL -> "வனவிலங்கு"
        AppLanguage.MARATHI -> "वन्यजीव"
        AppLanguage.GUJARATI -> "વન્યજીવ"
        AppLanguage.KANNADA -> "ವನ್ಯಜೀವಿ"
        AppLanguage.MALAYALAM -> "വന്യജീവി"
        AppLanguage.PUNJABI -> "ਜੰਗਲੀ ਜੀਵ"
        AppLanguage.ASSAMESE -> "বন্যপ্ৰাণী"
        AppLanguage.SANTALI -> "ᱵᱤᱨ ᱡᱤᱭᱟᱹᱞᱤ"
        AppLanguage.URDU -> "جنگلی حیات"
        else -> "Wildlife"
    }

    fun heritageCategory(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଐତିହ୍ୟ"
        AppLanguage.HINDI -> "धरोहर"
        AppLanguage.BENGALI -> "ঐতিহ্য"
        AppLanguage.TELUGU -> "వారసత్వం"
        AppLanguage.TAMIL -> "பாரம்பரியம்"
        AppLanguage.MARATHI -> "वारसा"
        AppLanguage.GUJARATI -> "વારસો"
        AppLanguage.KANNADA -> "ಪರಂಪರೆ"
        AppLanguage.MALAYALAM -> "പൈതൃകം"
        AppLanguage.PUNJABI -> "ਵਿਰਸਾ"
        AppLanguage.ASSAMESE -> "ঐতিহ্য"
        AppLanguage.SANTALI -> "ᱥᱮᱫᱟᱭ ᱥᱟᱨᱣᱟ"
        AppLanguage.URDU -> "ورثہ"
        else -> "Heritage"
    }

    fun portCategory(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବନ୍ଦର"
        AppLanguage.HINDI -> "बंदरगाह"
        AppLanguage.BENGALI -> "বন্দর"
        AppLanguage.TELUGU -> "ఓడరేవు"
        AppLanguage.TAMIL -> "துறைமுகம்"
        AppLanguage.MARATHI -> "बंदर"
        AppLanguage.GUJARATI -> "બંદર"
        AppLanguage.KANNADA -> "ಬಂದರು"
        AppLanguage.MALAYALAM -> "തുറമുഖം"
        AppLanguage.PUNJABI -> "ਬੰਦਰਗਾਹ"
        AppLanguage.ASSAMESE -> "বন্দৰ"
        AppLanguage.SANTALI -> "ᱜᱷᱟᱴ"
        AppLanguage.URDU -> "بندرگاہ"
        else -> "Port"
    }

    fun categoryLabel(category: String, lang: AppLanguage): String {
        return when (category.lowercase()) {
            "all" -> allCategory(lang)
            "beach" -> beachCategory(lang)
            "temple" -> templeCategory(lang)
            "wildlife" -> wildlifeCategory(lang)
            "heritage" -> heritageCategory(lang)
            "port" -> portCategory(lang)
            else -> category
        }
    }

    // Offline / Connectivity
    fun retry(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ପୁନର୍ବାର ଚେଷ୍ଟା କରନ୍ତୁ"
        AppLanguage.HINDI -> "पुनः प्रयास करें"
        AppLanguage.BENGALI -> "পুনরায় চেষ্টা করুন"
        AppLanguage.TELUGU -> "మళ్ళీ ప్రయత్నించండి"
        AppLanguage.TAMIL -> "மீண்டும் முயற்சிக்கவும்"
        AppLanguage.MARATHI -> "पुन्हा प्रयत्न करा"
        AppLanguage.GUJARATI -> "ફરી પ્રયાસ કરો"
        AppLanguage.KANNADA -> "ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ"
        AppLanguage.MALAYALAM -> "വീണ്ടും ശ്രമിക്കുക"
        AppLanguage.PUNJABI -> "ਦੁਬਾਰਾ ਕੋਸ਼ਿਸ਼ ਕਰੋ"
        AppLanguage.ASSAMESE -> "পুনৰ চেষ্টা কৰক"
        AppLanguage.SANTALI -> "ᱟᱨᱦᱚᱸ ᱠᱩᱨᱩᱢᱩᱴᱩᱭ ᱢᱮ"
        AppLanguage.URDU -> "دوبارہ کوشش کریں"
        else -> "Retry"
    }

    fun offlineTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ସଂଯୋଗ ନାହିଁ • ଅଫଲାଇନ୍ ମୋଡ୍ ସକ୍ରିୟ"
        AppLanguage.HINDI -> "इंटरनेट नहीं है • ऑफ़लाइन मोड सक्रिय"
        AppLanguage.BENGALI -> "ইন্টারনেট সংযোগ নেই • অফলাইন মোড সক্রিয়"
        AppLanguage.TELUGU -> "ఇంటర్నెట్ లేదు • ఆఫ్‌లైన్ మోడ్ సక్రియం"
        AppLanguage.TAMIL -> "இணையம் இல்லை • ஆஃப்லைன் பயன்முறை செயலில்"
        AppLanguage.MARATHI -> "इंटरनेट नाही • ऑफलाइन मोड सक्रिय"
        AppLanguage.GUJARATI -> "ઇન્ટરનેટ નથી • ઑફલાઇન મોડ ચાલુ છે"
        AppLanguage.KANNADA -> "ಇಂಟರ್ನೆಟ್ ಇಲ್ಲ • ಆಫ್‌ಲೈನ್ ಮೋಡ್ ಸಕ್ರಿಯ"
        AppLanguage.MALAYALAM -> "ഇന്റർനെറ്റ് ഇല്ല • ഓഫ്‌ലൈൻ മോഡ് സജീവം"
        AppLanguage.PUNJABI -> "ਇੰਟਰਨੈੱਟ ਨਹੀਂ ਹੈ • ਔਫਲਾਈਨ ਮੋਡ ਚਾਲੂ"
        AppLanguage.ASSAMESE -> "ইন্টাৰনেট নাই • অফলাইন ম'ড সক্ৰিয়"
        AppLanguage.SANTALI -> "ᱤᱱᱴᱚᱨᱱᱮᱴ ᱵᱟᱹᱱᱩᱜ-ᱟ • ᱚᱯᱷᱞᱟᱭᱤᱱ ᱢᱚᱰ"
        AppLanguage.URDU -> "انٹرنیٹ نہیں ہے • آف لائن موڈ فعال"
        else -> "No Connection • Offline Mode Active"
    }

    fun offlineSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ରୁମ୍ କ୍ୟାସ୍ ସାଇତା ଖବର, ପାଣିପାଗ ଓ ଦର୍ଶନୀୟ ସ୍ଥାନ ଉପଲବ୍ଧ"
        AppLanguage.HINDI -> "रूम डेटाबेस में सुरक्षित समाचार, मौसम और पर्यटन स्थल"
        AppLanguage.BENGALI -> "রুম ডেটাবেসে সংরক্ষিত খবর, আবহাওয়া ও পর্যটন কেন্দ্র"
        AppLanguage.TELUGU -> "రూమ్ డేటాబేస్‌లో సేవ్ చేసిన వార్తలు & వాతావరణం"
        AppLanguage.TAMIL -> "ரூம் தரவுத்தளத்தில் சேமிக்கப்பட்ட செய்திகள் & வானிலை"
        else -> "Showing Room cached news, weather & hotspots"
    }

    fun roomCachedPill(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ରୁମ୍ରେ ସାଇତା"
        AppLanguage.HINDI -> "सुरक्षित डेटा"
        AppLanguage.BENGALI -> "রুম সংরক্ষিত"
        else -> "Room Cached"
    }

    fun offlineRoomPill(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଅଫଲାଇନ୍ (ରୁମ୍)"
        AppLanguage.HINDI -> "ऑफ़लाइन (रूम)"
        AppLanguage.BENGALI -> "অফলাইন (রুম)"
        else -> "Offline (Room)"
    }

    fun syncingPill(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଅପଡେଟ୍ ହେଉଛି..."
        AppLanguage.HINDI -> "सिंक हो रहा है..."
        AppLanguage.BENGALI -> "সিঙ্ক হচ্ছে..."
        AppLanguage.TELUGU -> "సింక్ అవుతోంది..."
        AppLanguage.TAMIL -> "ஒத்திசைக்கப்படுகிறது..."
        else -> "Syncing..."
    }

    // Distance and Landmark details
    fun kmFromBalasore(km: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବାଲେଶ୍ୱରଠାରୁ $km କି.ମି."
        AppLanguage.HINDI -> "बालेश्वर से $km कि.मी."
        AppLanguage.BENGALI -> "বালেশ্বর থেকে $km কিমি"
        AppLanguage.TELUGU -> "బాలేశ్వర్ నుండి $km కి.మీ"
        AppLanguage.TAMIL -> "பாலேஸ்வரிலிருந்து $km கி.மீ"
        AppLanguage.MARATHI -> "बाळेश्वरपासून $km किमी"
        AppLanguage.GUJARATI -> "બાલેશ્વરથી $km કિમી"
        AppLanguage.KANNADA -> "ಬಾಲೇಶ್ವರದಿಂದ $km ಕಿ.ಮೀ"
        AppLanguage.MALAYALAM -> "ബാലേശ്വറിൽ നിന്ന് $km കി.മീ"
        AppLanguage.PUNJABI -> "ਬਾਲੇਸ਼ਵਰ ਤੋਂ $km ਕਿਮੀ"
        AppLanguage.ASSAMESE -> "বালেশ্বৰৰ পৰା $km কিমি"
        AppLanguage.SANTALI -> "ᱵᱟᱞᱮᱥᱚᱨ ᱠᱷᱚᱱ $km ᱠᱤ.ᱢᱤ."
        AppLanguage.URDU -> "بالیشور سے $km کلومیٹر"
        else -> "$km km from Baleshwar"
    }

    fun bestTimeToVisit(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଉତ୍ତମ ସମୟ"
        AppLanguage.HINDI -> "सर्वोत्तम समय"
        AppLanguage.BENGALI -> "ভ্রমণের সেরা সময়"
        AppLanguage.TELUGU -> "ఉత్తమ సమయం"
        AppLanguage.TAMIL -> "சிறந்த நேரம்"
        AppLanguage.MARATHI -> "उत्तम वेळ"
        AppLanguage.GUJARATI -> "શ્રેષ્ઠ સમય"
        AppLanguage.KANNADA -> "ಉತ್ತಮ ಸಮಯ"
        AppLanguage.MALAYALAM -> "മികച്ച സമയം"
        AppLanguage.PUNJABI -> "ਵਧੀਆ ਸਮਾਂ"
        AppLanguage.ASSAMESE -> "উপযুক্ত সময়"
        AppLanguage.SANTALI -> "ᱵᱷᱟᱹᱜᱤ ᱚᱠᱛᱚ"
        AppLanguage.URDU -> "بہترین وقت"
        else -> "Best Time"
    }

    fun timings(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ସମୟ"
        AppLanguage.HINDI -> "समय"
        AppLanguage.BENGALI -> "সময়সূচী"
        AppLanguage.TELUGU -> "సమయాలు"
        AppLanguage.TAMIL -> "நேரம்"
        AppLanguage.MARATHI -> "वेळ"
        AppLanguage.GUJARATI -> "સમય"
        AppLanguage.KANNADA -> "ಸಮಯ"
        AppLanguage.MALAYALAM -> "സമയം"
        AppLanguage.PUNJABI -> "ਸਮਾਂ"
        AppLanguage.ASSAMESE -> "সময়"
        AppLanguage.SANTALI -> "ᱚᱠᱛᱚ"
        AppLanguage.URDU -> "اوقات"
        else -> "Timings"
    }

    fun entryFee(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ପ୍ରବେଶ ଶୁଳ୍କ"
        AppLanguage.HINDI -> "प्रवेश शुल्क"
        AppLanguage.BENGALI -> "প্রবেশ মূল্য"
        AppLanguage.TELUGU -> "ప్రవేశ రుసుము"
        AppLanguage.TAMIL -> "நுழைவுக் கட்டணம்"
        AppLanguage.MARATHI -> "प्रवेश शुल्क"
        AppLanguage.GUJARATI -> "પ્રવેશ ફી"
        AppLanguage.KANNADA -> "ಪ್ರವೇಶ ಶುಲ್ಕ"
        AppLanguage.MALAYALAM -> "പ്രവേശന ഫീസ്"
        AppLanguage.PUNJABI -> "ਦਾਖਲਾ ਫੀਸ"
        AppLanguage.ASSAMESE -> "প্ৰৱেশ মাছুল"
        AppLanguage.SANTALI -> "ᱵᱚᱞᱚᱱ ᱯᱷᱤ"
        AppLanguage.URDU -> "داخلہ فیس"
        else -> "Entry Fee"
    }

    fun specialty(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବିଶେଷତ୍ୱ"
        AppLanguage.HINDI -> "विशेषता"
        AppLanguage.BENGALI -> "বিশেষত্ব"
        AppLanguage.TELUGU -> "ప్రత్యేకత"
        AppLanguage.TAMIL -> "சிறப்பம்சம்"
        AppLanguage.MARATHI -> "वैशिष्ट्य"
        AppLanguage.GUJARATI -> "ખાસિયત"
        AppLanguage.KANNADA -> "ವಿಶೇಷತೆ"
        AppLanguage.MALAYALAM -> "പ്രത്യേകത"
        AppLanguage.PUNJABI -> "ਖਾਸਿਅਤ"
        AppLanguage.ASSAMESE -> "বিশেষত্ব"
        AppLanguage.SANTALI -> "ᱵᱤᱥᱮᱥ ᱜᱩᱱ"
        AppLanguage.URDU -> "خصوصیت"
        else -> "Specialty"
    }

    fun localTip(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ସ୍ଥାନୀୟ ପରାମର୍ଶ"
        AppLanguage.HINDI -> "स्थानीय सुझाव"
        AppLanguage.BENGALI -> "স্থানীয় পরামর্শ"
        AppLanguage.TELUGU -> "స్థానిక చిట్కా"
        AppLanguage.TAMIL -> "உள்ளூர் குறிப்பு"
        AppLanguage.MARATHI -> "स्थानिक सल्ला"
        AppLanguage.GUJARATI -> "સ્થાનિક ટિપ"
        AppLanguage.KANNADA -> "ಸ್ಥಳೀಯ ಸಲಹೆ"
        AppLanguage.MALAYALAM -> "പ്രാദേശിക നിർദ്ദേശം"
        AppLanguage.PUNJABI -> "ਸਥਾਨਕ ਸਲਾਹ"
        AppLanguage.ASSAMESE -> "স্থানীয় পৰামৰ্শ"
        AppLanguage.SANTALI -> "ᱟᱹᱛᱩ ᱫᱤᱥᱚᱢ ᱫᱤᱥ-ᱦᱩᱫᱤᱥ"
        AppLanguage.URDU -> "مقامی مشورہ"
        else -> "Local Tip"
    }

    fun openInMaps(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ମ୍ୟାପ୍ରେ ଦେଖନ୍ତୁ"
        AppLanguage.HINDI -> "मानचित्र पर देखें"
        AppLanguage.BENGALI -> "মানচিত্রে দেখুন"
        AppLanguage.TELUGU -> "మ్యాప్‌లో చూడండి"
        AppLanguage.TAMIL -> "வரைபடத்தில் காண்க"
        AppLanguage.MARATHI -> "नकाशावर पहा"
        AppLanguage.GUJARATI -> "નકશા પર જુઓ"
        AppLanguage.KANNADA -> "ನಕ್ಷೆಯಲ್ಲಿ ನೋಡಿ"
        AppLanguage.MALAYALAM -> "മാപ്പിൽ കാണുക"
        AppLanguage.PUNJABI -> "ਨਕਸ਼ੇ 'ਤੇ ਵੇਖੋ"
        AppLanguage.ASSAMESE -> "মানচিত্ৰত চাওক"
        AppLanguage.SANTALI -> "ᱢᱮᱯ ᱨᱮ ᱧᱮᱞ ᱢᱮ"
        AppLanguage.URDU -> "نقشے پر دیکھیں"
        else -> "Explore on Maps"
    }

    // Language Section Settings
    fun languageSettingTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଭାଷା ଓ ଲିପି ପସନ୍ଦ (୨୨+ ଭାରତୀୟ ଭାଷା)"
        AppLanguage.HINDI -> "भाषा एवं लिपि प्राथमिकता (२२+ भारतीय भाषाएं)"
        AppLanguage.BENGALI -> "ভাষা ও লিপি পছন্দ (২২+ ভারতীয় ভাষা)"
        AppLanguage.TELUGU -> "భాష & లిపి ప్రాధాన్యత (22+ భారతీయ భాషలు)"
        AppLanguage.TAMIL -> "மொழி மற்றும் எழுத்துரு விருப்பம் (22+ இந்திய மொழிகள்)"
        AppLanguage.MARATHI -> "भाषा आणि लिपी प्राधान्य (२२+ भारतीय भाषा)"
        AppLanguage.GUJARATI -> "ભાષા અને લિપિ પસંદગી (૨૨+ ભારતીય ભાષાઓ)"
        AppLanguage.KANNADA -> "ಭಾಷೆ ಮತ್ತು ಲಿಪಿ ಆದ್ಯತೆ (22+ ಭಾರತೀಯ ಭಾಷೆಗಳು)"
        AppLanguage.MALAYALAM -> "ഭാഷയും ലിപിയും (22+ ഇന്ത്യൻ ഭാഷകൾ)"
        AppLanguage.PUNJABI -> "ਭਾਸ਼ਾ ਅਤੇ ਲਿਪੀ ਪਸੰਦ (੨੨+ ਭਾਰਤੀ ਭਾਸ਼ਾਵਾਂ)"
        AppLanguage.ASSAMESE -> "ভাষা আৰু লিপি পছন্দ (২২+ ভাৰতীয় ভাষা)"
        AppLanguage.SANTALI -> "ᱯᱟᱹᱨᱥᱤ ᱟᱨ ᱚᱞ ᱠᱩᱥᱤ (᱒᱒+ ᱵᱷᱟᱨᱚᱛᱤᱭᱟᱹ ᱯᱟᱹᱨᱥᱤ)"
        AppLanguage.URDU -> "زبان اور رسم الخط کا انتخاب (۲۲+ ہندوستانی زبانیں)"
        AppLanguage.SANSKRIT -> "भाषायाः लिपेः च प्राधान्यम् (२२+ भारतीयभाषाः)"
        else -> "Language & Script Preference (22+ Indian Languages)"
    }

    fun languageSettingDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଆପଣଙ୍କ ପସନ୍ଦର ଭାରତୀୟ ଭାଷା ବାଛନ୍ତୁ (ଓଡ଼ିଆ, ହିନ୍ଦୀ, ବଙ୍ଗଳା, ତେଲୁଗୁ, ତାମିଲ, ସାନ୍ତାଳୀ ଇତ୍ୟାଦି)"
        AppLanguage.HINDI -> "शीर्षक, विवरण एवं सांस्कृतिक सूचनाओं हेतु अपनी भाषा चुनें (२२+ भाषाएं)"
        AppLanguage.BENGALI -> "আপনার পছন্দের ভারতীয় ভাষা বেছে নিন (বাংলা, ওড়িয়া, হিন্দি, তেলুগু, তামিল, সাঁওতালি ইত্যাদি)"
        AppLanguage.TELUGU -> "మీ ప్రాధాన్యత గల భారతీయ భాషను ఎంచుకోండి (22+ భాషలు)"
        AppLanguage.TAMIL -> "உங்கள் விருப்பமான இந்திய மொழியைத் தேர்வுசெய்யவும் (22+ மொழிகள்)"
        AppLanguage.SANTALI -> "ᱟᱢᱟᱜ ᱠᱩᱥᱤ ᱵᱷᱟᱨᱚᱛᱤᱭᱟᱹ ᱯᱟᱹᱨᱥᱤ ᱵᱟᱪᱷᱟᱣ ᱢᱮ"
        else -> "Choose your display language from all 22 official Indian languages + English"
    }

    // Weather Metrics
    fun feelsLike(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଅନୁଭୂତ ତାପମାତ୍ରା"
        AppLanguage.HINDI -> "अनुभूत तापमान"
        AppLanguage.BENGALI -> "অনুভূত তাপমাত্রা"
        AppLanguage.TELUGU -> "అనిపించే ఉష్ణోగ్రత"
        AppLanguage.TAMIL -> "உணரப்படும் வெப்பநிலை"
        AppLanguage.MARATHI -> "जावणारे तापमान"
        AppLanguage.GUJARATI -> "અનુભવાતું તાપમાન"
        AppLanguage.KANNADA -> "ಅನುಭವವಾಗುವ ತಾಪಮಾನ"
        AppLanguage.MALAYALAM -> "അനുഭവപ്പെടുന്ന താപനില"
        AppLanguage.PUNJABI -> "ਮਹਿਸੂਸ ਹੁੰਦਾ ਤਾਪਮਾਨ"
        AppLanguage.ASSAMESE -> "অনুভৱ হোৱা উষ্ণতা"
        AppLanguage.SANTALI -> "ᱟᱹᱭᱠᱟᱹᱣᱜ ᱞᱚᱞᱚ"
        AppLanguage.URDU -> "محسوس شدہ درجہ حرارت"
        else -> "Feels Like"
    }

    fun humidity(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଆର୍ଦ୍ରତା"
        AppLanguage.HINDI -> "आर्द्रता"
        AppLanguage.BENGALI -> "আর্দ্রতা"
        AppLanguage.TELUGU -> "తేమ"
        AppLanguage.TAMIL -> "ஈரப்பதம்"
        AppLanguage.MARATHI -> "आर्द्रता"
        AppLanguage.GUJARATI -> "ભેજ"
        AppLanguage.KANNADA -> "ಆರ್ದ್ರತೆ"
        AppLanguage.MALAYALAM -> "ഈർപ്പം"
        AppLanguage.PUNJABI -> "ਨਮੀ"
        AppLanguage.ASSAMESE -> "আৰ্দ্ৰতা"
        AppLanguage.SANTALI -> "ᱫᱟᱜ-ᱦᱚᱭ ᱚᱫᱟ"
        AppLanguage.URDU -> "نمی"
        else -> "Humidity"
    }

    fun windSpeed(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ପବନର ଗତି"
        AppLanguage.HINDI -> "हवा की गति"
        AppLanguage.BENGALI -> "বাতাসের গতি"
        AppLanguage.TELUGU -> "గాలి వేగం"
        AppLanguage.TAMIL -> "காற்றின் வேகம்"
        AppLanguage.MARATHI -> "वाऱ्याचा वेग"
        AppLanguage.GUJARATI -> "પવનની ગતિ"
        AppLanguage.KANNADA -> "ಗಾಳಿಯ ವೇಗ"
        AppLanguage.MALAYALAM -> "കാറ്റിന്റെ വേഗത"
        AppLanguage.PUNJABI -> "ਹਵਾ ਦੀ ਰਫ਼ਤਾਰ"
        AppLanguage.ASSAMESE -> "বতাহৰ গতি"
        AppLanguage.SANTALI -> "ᱦᱚᱭ ᱨᱮᱭᱟᱜ ᱜᱟᱹᱛ"
        AppLanguage.URDU -> "ہوا کی رفتار"
        else -> "Wind Speed"
    }

    fun rainProbability(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବର୍ଷା ସମ୍ଭାବନା"
        AppLanguage.HINDI -> "बारिश की संभावना"
        AppLanguage.BENGALI -> "বৃষ্টির সম্ভাবনা"
        AppLanguage.TELUGU -> "వర్షం సంభావ్యత"
        AppLanguage.TAMIL -> "மழை சாத்தியம்"
        AppLanguage.MARATHI -> "पावसाची शक्यता"
        AppLanguage.GUJARATI -> "વરસાદની શક્યતા"
        AppLanguage.KANNADA -> "ಮಳೆಯ ಸಂಭವನೀಯತೆ"
        AppLanguage.MALAYALAM -> "മഴയ്ക്കുള്ള സാധ്യത"
        AppLanguage.PUNJABI -> "ਮੀਂਹ ਦੀ ਸੰਭਾਵਨਾ"
        AppLanguage.ASSAMESE -> "বৰষুণৰ সম্ভাৱনা"
        AppLanguage.SANTALI -> "ᱫᱟᱜ ᱦᱩᱭᱩᱜ ᱨᱮᱭᱟᱜ ᱟᱥ"
        AppLanguage.URDU -> "بارش کا امکان"
        else -> "Rain Probability"
    }

    fun tideVanishingSea(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଚାନ୍ଦିପୁର: ଅଦୃଶ୍ୟ ସମୁଦ୍ର (ସମୁଦ୍ର ଭଟ୍ଟା)"
        AppLanguage.HINDI -> "चांदीपुर: लुप्त होता समुद्र (कम ज्वार)"
        AppLanguage.BENGALI -> "চাঁদিপুর: অদৃশ্য সমুদ্র (ভাটা)"
        AppLanguage.TELUGU -> "చాందీపూర్: అదృశ్యమయ్యే సముద్రం (తక్కువ అలలు)"
        AppLanguage.TAMIL -> "சாந்திபூர்: மறையும் கடல் (குறைந்த அலை)"
        AppLanguage.MARATHI -> "चांदीपूर: अदृश्य समुद्र (ओहोटी)"
        AppLanguage.GUJARATI -> "ચાંદીપુર: અદ્રશ્ય સમુદ્ર (ઓટ)"
        AppLanguage.KANNADA -> "ಚಾಂದಿಪುರ: ಮಾಯವಾಗುವ ಸಮುದ್ರ"
        AppLanguage.MALAYALAM -> "ചന്ദിപ്പൂർ: അപ്രത്യക്ഷമാകുന്ന കടൽ"
        AppLanguage.PUNJABI -> "ਚਾਂਦੀਪੁਰ: ਗਾਇਬ ਹੁੰਦਾ ਸਮੁੰਦਰ"
        AppLanguage.ASSAMESE -> "চান্দীপুৰ: অদৃশ্য সাগৰ"
        AppLanguage.SANTALI -> "ᱪᱟᱸᱫᱤᱯᱩᱨ: ᱟᱫᱚᱜ ᱠᱟᱱ ᱫᱚᱨᱭᱟ"
        AppLanguage.URDU -> "چاندی پور: غائب ہوتا سمندر (جزر)"
        else -> "Chandipur: Vanishing Sea (Low Tide)"
    }

    fun tideIncoming(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଚାନ୍ଦିପୁର: ଜୁଆର ଆସୁଛି (କୂଳକୁ ଫେରନ୍ତୁ)"
        AppLanguage.HINDI -> "चांदीपुर: ज्वार आ रहा है (तट पर लौटें)"
        AppLanguage.BENGALI -> "চাঁদিপুর: জোয়ার আসছে (তীরে ফিরে আসুন)"
        AppLanguage.TELUGU -> "చాందీపూర్: అలలు వస్తున్నాయి (తీరానికి రండి)"
        AppLanguage.TAMIL -> "சாந்திபூர்: அலை வருகிறது (கரைக்கு திரும்பவும்)"
        AppLanguage.MARATHI -> "चांदीपूर: भरती येत आहे (किनाऱ्यावर परत या)"
        AppLanguage.GUJARATI -> "ચાંદીપુર: ભરતી આવી રહી છે (કિનારે પાછા ફરો)"
        AppLanguage.KANNADA -> "ಚಾಂದಿಪುರ: ಉಬ್ಬರವಿಳಿತ ಬರುತ್ತಿದೆ"
        AppLanguage.MALAYALAM -> "ചന്ദിപ്പൂർ: വേലിയേറ്റം വരുന്നു"
        AppLanguage.PUNJABI -> "ਚਾਂਦੀਪੁਰ: ਲਹਿਰਾਂ ਆ ਰਹੀਆਂ ਹਨ (ਕੰਢੇ ਵਾਪਸ ਆਓ)"
        AppLanguage.ASSAMESE -> "চান্দীপুৰ: জোৱাৰ আহি আছে"
        AppLanguage.SANTALI -> "ᱪᱟᱸᱫᱤᱯᱩᱨ: ᱡᱩᱣᱟᱹᱨ ᱦᱤᱡᱩᱜ ᱠᱟᱱᱟ (ᱟᱲᱮ ᱛᱮ ᱨᱩᱣᱟᱹᱲ ᱢᱮ)"
        AppLanguage.URDU -> "چاندی پور: مد آ رہا ہے (ساحل پر واپس آئیں)"
        else -> "Chandipur: Incoming Tide (Return to Shore)"
    }

    fun tideReceding(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଚାନ୍ଦିପୁର: ସମୁଦ୍ର ପଛକୁ ଯାଉଛି"
        AppLanguage.HINDI -> "चांदीपुर: समुद्र पीछे हट रहा है"
        AppLanguage.BENGALI -> "চাঁদিপুর: সমুদ্র পিছিয়ে যাচ্ছে"
        AppLanguage.TELUGU -> "చాందీపూర్: సముద్రం వెనక్కి తగ్గుతోంది"
        AppLanguage.TAMIL -> "சாந்திபூர்: கடல் பின்வாங்குகிறது"
        AppLanguage.MARATHI -> "चांदीपूर: समुद्र मागे हटत आहे"
        AppLanguage.GUJARATI -> "ચાંદીપુર: સમુદ્ર પાછો ફરી રહ્યો છે"
        AppLanguage.KANNADA -> "ಚಾಂದಿಪುರ: ಸಮುದ್ರವು ಹಿಂದಕ್ಕೆ ಸರಿಯುತ್ತಿದೆ"
        AppLanguage.MALAYALAM -> "ചന്ദിപ്പൂർ: കടൽ പിന്നോട്ട് പോകുന്നു"
        AppLanguage.PUNJABI -> "ਚਾਂਦੀਪੁਰ: ਸਮੁੰਦਰ ਪਿੱਛੇ ਹਟ ਰਿਹਾ ਹੈ"
        AppLanguage.ASSAMESE -> "চান্দীপুৰ: সাগৰ পিছলৈ গৈ আছে"
        AppLanguage.SANTALI -> "ᱪᱟᱸᱫᱤᱯᱩᱨ: ᱫᱚᱨᱭᱟ ᱛᱟᱭᱚᱢ ᱥᱮᱫ ᱥᱮᱱᱚᱜ ᱠᱟᱱᱟ"
        AppLanguage.URDU -> "چاندی پور: سمندر پیچھے ہٹ رہا ہے"
        else -> "Chandipur: Sea Receding Outwards"
    }

    // AI Assistant
    fun askAiButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ବାଲେଶ୍ୱର AI କୁ ପଚାରନ୍ତୁ"
        AppLanguage.HINDI -> "बालेश्वर AI से पूछें"
        AppLanguage.BENGALI -> "বালেশ্বর AI-কে জিজ্ঞাসা করুন"
        AppLanguage.TELUGU -> "బాలేశ్వర్ AI ని అడగండి"
        AppLanguage.TAMIL -> "பாலேஸ்வர் AI-யிடம் கேளுங்கள்"
        AppLanguage.MARATHI -> "बाळेश्वर AI ला विचारा"
        AppLanguage.GUJARATI -> "બાલેશ્વર AI ને પૂછો"
        AppLanguage.KANNADA -> "ಬಾಲೇಶ್ವರ AI ಅನ್ನು ಕೇಳಿ"
        AppLanguage.MALAYALAM -> "ബാലേശ്വർ AI-യോട് ചോദിക്കൂ"
        AppLanguage.PUNJABI -> "ਬਾਲੇਸ਼ਵਰ AI ਨੂੰ ਪੁੱਛੋ"
        AppLanguage.ASSAMESE -> "বালেশ্বৰ AI ক সোধক"
        AppLanguage.SANTALI -> "ᱵᱟᱞᱮᱥᱚᱨ AI ᱠᱩᱞᱤᱭᱮ ᱢᱮ"
        AppLanguage.URDU -> "بالیشور AI سے پوچھیں"
        AppLanguage.SANSKRIT -> "बालेश्वर-AI पृच्छन्तु"
        else -> "Ask Baleshwar AI"
    }

    fun aiAssistantSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ODIA -> "ଜିଲ୍ଲା ସମ୍ପର୍କିତ ନିର୍ଭରଯୋଗ୍ୟ ସୂଚନା"
        AppLanguage.HINDI -> "ज़िला स्तरीय प्रामाणिक जानकारी"
        AppLanguage.BENGALI -> "জেলা সংক্রান্ত নির্ভরযোগ্য তথ্য"
        AppLanguage.TELUGU -> "జిల్లాల ఆధారిత ప్రామాణిక సమాచారం"
        AppLanguage.TAMIL -> "மாவட்டம் சார்ந்த நம்பகமான தகவல்"
        AppLanguage.MARATHI -> "जिल्हास्तरीय प्रामाणिक माहिती"
        AppLanguage.GUJARATI -> "જિલ્લા સ્તરીય અધિકૃત માહિતી"
        AppLanguage.KANNADA -> "ಜಿಲ್ಲಾ ಮಟ್ಟದ ವಿಶ್ವಾಸಾರ್ಹ ಮಾಹಿತಿ"
        AppLanguage.MALAYALAM -> "ജില്ലാതല ആധികാരിക വിവരങ്ങൾ"
        AppLanguage.PUNJABI -> "ਜ਼ਿਲ੍ਹਾ ਪੱਧਰੀ ਪ੍ਰਮਾਣਿਕ ਜਾਣਕਾਰੀ"
        AppLanguage.ASSAMESE -> "জিলা ভিত্তিক নিৰ্ভৰযোগ্য তথ্য"
        AppLanguage.SANTALI -> "ᱡᱤᱞᱟᱹ ᱥᱟᱹᱜᱟᱹᱭᱟᱱ ᱯᱟᱹᱛᱭᱟᱹᱣᱟᱱ ᱵᱟᱰᱟᱭ"
        AppLanguage.URDU -> "ضلعی سطح کی مستند معلومات"
        else -> "Grounded District Guide & Live Info"
    }
}
