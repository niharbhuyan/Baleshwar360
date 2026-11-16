package com.example.data.repository

import java.util.Calendar

data class DoctorProfile(
    val id: String,
    val name: String,
    val odiaName: String,
    val specialty: String,
    val qualification: String,
    val experienceYears: Int,
    val affiliationHospital: String,
    val chamberName: String,
    val chamberAddress: String,
    val consultationFee: Int,
    val opdTimings: String = "09:00 AM - 01:00 PM & 05:30 PM - 08:30 PM",
    val phone: String,
    val latitude: Double = 21.4934,
    val longitude: Double = 86.9325
)

data class MedicineStore(
    val id: String,
    val name: String,
    val odiaName: String,
    val areaLocation: String,
    val fullAddress: String,
    val is24x7: Boolean,
    val isJanAushadhiGeneric: Boolean,
    val hasHomeDelivery: Boolean,
    val deliveryRadiusKm: Int = 5,
    val openingHours: String,
    val licensedPharmacistName: String,
    val emergencyMedicines: List<String>,
    val phone: String,
    val alternatePhone: String? = null,
    val latitude: Double = 21.4934,
    val longitude: Double = 86.9325
)

data class PathologyPackageItem(
    val name: String,
    val description: String,
    val sampleType: String,
    val reportTurnaroundHours: Int,
    val estimatedPrice: String
)

data class PathologyLab(
    val id: String,
    val name: String,
    val odiaName: String,
    val address: String,
    val landmark: String,
    val accreditation: String,
    val openingHours: String,
    val fastingCollectionHours: String,
    val homeSampleCollectionAvailable: Boolean,
    val popularPackages: List<PathologyPackageItem>,
    val phone: String,
    val homeCollectionPhone: String? = null,
    val latitude: Double = 21.4934,
    val longitude: Double = 86.9325
)

data class PolyclinicCenter(
    val id: String,
    val name: String,
    val odiaName: String,
    val category: String,
    val address: String,
    val landmark: String,
    val openHours: String,
    val sampleCollectionTimings: String,
    val facilities: List<String>,
    val visitingSpecialistsList: List<String>,
    val phone: String,
    val latitude: Double = 21.4934,
    val longitude: Double = 86.9325
)

data class PrivateHospital(
    val id: String,
    val name: String,
    val odiaName: String,
    val category: String,
    val address: String,
    val landmark: String,
    val totalBeds: Int,
    val icuBedsCount: Int,
    val is24x7Emergency: Boolean,
    val hasDialysisUnit: Boolean,
    val hasNicuPicU: Boolean,
    val hasBloodStorageOrBank: Boolean,
    val acceptedSchemes: List<String>,
    val specialtiesAvailable: List<String>,
    val keySurgeonsAndDoctors: List<String>,
    val opdTimings: String,
    val visitingHours: String,
    val emergencyPhone: String,
    val receptionPhone: String,
    val latitude: Double = 21.4934,
    val longitude: Double = 86.9325
)

object HealthcareRepository {
    val doctorsList: List<DoctorProfile> = listOf(
        DoctorProfile(
            id = "doc_1",
            name = "Dr. Debashish Mohanty",
            odiaName = "ଡା. ଦେବାଶିଷ ମହାନ୍ତି",
            specialty = "General Medicine",
            qualification = "MD (Medicine), SCB Cuttack",
            experienceYears = 16,
            affiliationHospital = "FMMCH Medical College & DHH Balasore",
            chamberName = "Balasore Care Clinic",
            chamberAddress = "Hospital Road, Kacheri Bazar, Balasore",
            consultationFee = 300,
            phone = "06782-262007",
            latitude = 21.4922,
            longitude = 86.9248
        ),
        DoctorProfile(
            id = "doc_2",
            name = "Dr. Subrat Kumar Behera",
            odiaName = "ଡା. ସୁବ୍ରତ କୁମାର ବେହେରା",
            specialty = "Cardiology",
            qualification = "MD, DM (Cardiology)",
            experienceYears = 14,
            affiliationHospital = "FMMCH Super Specialty Wing",
            chamberName = "Apollo Heart & Diagnostics",
            chamberAddress = "OT Road, Near Police Line Square, Balasore",
            consultationFee = 500,
            phone = "06782-264512",
            latitude = 21.4945,
            longitude = 86.9310
        ),
        DoctorProfile(
            id = "doc_3",
            name = "Dr. Ipsita Das",
            odiaName = "ଡା. ଇପ୍ସିତା ଦାସ",
            specialty = "Pediatrics",
            qualification = "MD (Pediatrics)",
            experienceYears = 12,
            affiliationHospital = "Jyoti Hospital Kuruda",
            chamberName = "Shishu Care Polyclinic",
            chamberAddress = "Vivekananda Marg, Balasore",
            consultationFee = 400,
            phone = "06782-255890",
            latitude = 21.4880,
            longitude = 86.9290
        ),
        DoctorProfile(
            id = "doc_4",
            name = "Dr. Ranjan Kumar Rout",
            odiaName = "ଡା. ରଞ୍ଜନ କୁମାର ରାଉତ",
            specialty = "Orthopedics",
            qualification = "MS (Ortho), AIIMS",
            experienceYears = 15,
            affiliationHospital = "Life Care Hospital Balasore",
            chamberName = "Balasore Bone & Joint Centre",
            chamberAddress = "Station Square, Balasore",
            consultationFee = 400,
            phone = "06782-263110",
            latitude = 21.4990,
            longitude = 86.9275
        )
    )

    val medicineStoresList: List<MedicineStore> = listOf(
        MedicineStore(
            id = "med_1",
            name = "Pradhan Mantri Bhartiya Janaushadhi Kendra (DHH)",
            odiaName = "ପ୍ରଧାନମନ୍ତ୍ରୀ ଜନୌଷଧି କେନ୍ଦ୍ର (ମୁଖ୍ୟ ଚିକିତ୍ସାଳୟ)",
            areaLocation = "DHH Campus, Kacheri Road",
            fullAddress = "Inside District Headquarters Hospital Gate-1, Balasore",
            is24x7 = true,
            isJanAushadhiGeneric = true,
            hasHomeDelivery = false,
            openingHours = "Open 24 Hours (24x7 Emergency Counter)",
            licensedPharmacistName = "Sri P. K. Panda (B.Pharm)",
            emergencyMedicines = listOf("Insulin Glargine", "Anti-Rabies Vaccine", "ORS & IV Fluids", "Cardiac Emergency Statins"),
            phone = "06782-262108",
            alternatePhone = "9437112233",
            latitude = 21.4925,
            longitude = 86.9252
        ),
        MedicineStore(
            id = "med_2",
            name = "Apollo Pharmacy 24x7 Station Square",
            odiaName = "ଆପୋଲୋ ଫାର୍ମାସୀ (ଷ୍ଟେସନ ଛକ)",
            areaLocation = "Station Road & OT Road",
            fullAddress = "Fakir Mohan Golai, OT Road, Balasore",
            is24x7 = true,
            isJanAushadhiGeneric = false,
            hasHomeDelivery = true,
            deliveryRadiusKm = 8,
            openingHours = "Open 24 Hours",
            licensedPharmacistName = "Smt. S. Mohapatra (M.Pharm)",
            emergencyMedicines = listOf("Nebulizer Respules", "Pediatric Antibiotics", "Surgical Dressings", "Glucometer Strips"),
            phone = "06782-265500",
            latitude = 21.4978,
            longitude = 86.9305
        ),
        MedicineStore(
            id = "med_3",
            name = "FMMCH Gate-2 24x7 Life Saving Medico",
            odiaName = "ଏଫ.ଏମ. ମେଡିକାଲ କଲେଜ ମେଡିସିନ୍ ଷ୍ଟୋର",
            areaLocation = "Remuna FMMCH & Kuruda",
            fullAddress = "Opposite FMMCH Medical College Gate 2, Remuna, Balasore",
            is24x7 = true,
            isJanAushadhiGeneric = true,
            hasHomeDelivery = true,
            deliveryRadiusKm = 10,
            openingHours = "Open 24 Hours",
            licensedPharmacistName = "Sri R. K. Senapati (B.Pharm)",
            emergencyMedicines = listOf("ICU Critical Injectables", "Nephrology & Dialysis Consumables", "Low-Molecular Heparin"),
            phone = "9437288901",
            latitude = 21.5190,
            longitude = 86.8850
        )
    )

    val pathologyLabsList: List<PathologyLab> = listOf(
        PathologyLab(
            id = "path_1",
            name = "Dr. Lal PathLabs Regional Hub Balasore",
            odiaName = "ଡା. ଲାଲ ପାଥଲାବ୍ସ ବାଲେଶ୍ୱର କେନ୍ଦ୍ର",
            address = "Vivekananda Marg, Near Cinema Square",
            landmark = "Opposite Town Hall, Balasore",
            accreditation = "NABL & CAP Accredited • ISO 9001",
            openingHours = "06:30 AM - 08:30 PM",
            fastingCollectionHours = "06:30 AM - 10:30 AM",
            homeSampleCollectionAvailable = true,
            popularPackages = listOf(
                PathologyPackageItem("SwasthFit Super Full Body (72 Tests)", "CBC, HbA1c, Lipid, LFT, KFT, Thyroid TSH & Vitamin D", "Fasting Blood & Urine", 6, "₹1,299"),
                PathologyPackageItem("Monsoon Coastal Fever Panel", "CBC, Malaria Antigen, Dengue NS1 & Typhoid IgM", "EDTA Whole Blood", 4, "₹650")
            ),
            phone = "06782-266111",
            homeCollectionPhone = "9437011223",
            latitude = 21.4910,
            longitude = 86.9330
        ),
        PathologyLab(
            id = "path_2",
            name = "Thyrocare Wellness & Balasore Diagnostic Centre",
            odiaName = "ଥାଇରୋକେୟାର ଓ ବାଲେଶ୍ୱର ଡାଇଗ୍ନୋଷ୍ଟିକ୍",
            address = "Station Road, Fakir Mohan Golai",
            landmark = "Near Police Line Square, OT Road",
            accreditation = "NABL Accredited Central Reference Lab",
            openingHours = "06:00 AM - 09:00 PM",
            fastingCollectionHours = "06:00 AM - 11:00 AM",
            homeSampleCollectionAvailable = true,
            popularPackages = listOf(
                PathologyPackageItem("Aarogyam B Thyroid & Diabetic Profile", "TSH, T3, T4, HbA1c, Fasting Blood Sugar & Lipid", "Fasting Serum", 8, "₹899"),
                PathologyPackageItem("Complete Renal & Electrolytes Panel", "Urea, Creatinine, Uric Acid, Sodium & Potassium", "Blood Sample", 4, "₹450")
            ),
            phone = "06782-264499",
            homeCollectionPhone = "9437344556",
            latitude = 21.4960,
            longitude = 86.9295
        ),
        PathologyLab(
            id = "path_5",
            name = "FMMCH Nidan 24x7 Regional Diagnostic Wing",
            odiaName = "ଏଫ.ଏମ. ମେଡିକାଲ କଲେଜ ନିଦାନ କେନ୍ଦ୍ର",
            address = "DHH Campus, Kacheri Road",
            landmark = "Main Casualty Block, Balasore",
            accreditation = "Govt of Odisha Nidan Free Diagnostics • NABL",
            openingHours = "Open 24 Hours (24x7 STAT Emergency)",
            fastingCollectionHours = "07:00 AM - 11:00 AM",
            homeSampleCollectionAvailable = false,
            popularPackages = listOf(
                PathologyPackageItem("Emergency STAT CBC, Trop-I & Coagulation", "Rapid trauma & cardiac screening for emergency patients", "Whole Blood", 2, "Free (Nidan)"),
                PathologyPackageItem("Complete Biochemistry & Thyroid Panel", "LFT, RFT, Lipid & TSH under Odisha Nidan Scheme", "Fasting Blood", 6, "Free (Nidan)")
            ),
            phone = "06782-262007",
            homeCollectionPhone = null,
            latitude = 21.4922,
            longitude = 86.9248
        )
    )

    val polyclinicsList: List<PolyclinicCenter> = listOf(
        PolyclinicCenter(
            id = "poly_1",
            name = "Apollo Clinic & Multi-Specialty Polyclinic",
            odiaName = "ଆପୋଲୋ ପଲି-କ୍ଲିନିକ୍ ଓ ନିଦାନ କେନ୍ଦ୍ର",
            category = "Multi-Specialty OPD & Digital X-Ray / USG",
            address = "OT Road, Police Line Square",
            landmark = "Near ITI Square, Balasore",
            openHours = "07:00 AM - 09:00 PM",
            sampleCollectionTimings = "07:00 AM - 11:30 AM (Fasting)",
            facilities = listOf("2D Echo & TMT", "Digital X-Ray & 4D Ultrasound", "Specialist Evening Chambers", "Pharmacy"),
            visitingSpecialistsList = listOf("Dr. S. K. Behera (Cardiology)", "Dr. A. K. Das (Endocrinology)", "Dr. P. Mohanty (Gynecology)"),
            phone = "06782-264512",
            latitude = 21.4945,
            longitude = 86.9310
        ),
        PolyclinicCenter(
            id = "poly_2",
            name = "Utkal Scan & Polyclinic Centre",
            odiaName = "ଉତ୍କଳ ସ୍କାନ୍ ଓ ପଲି-କ୍ଲିନିକ୍",
            category = "Radiology, CT/MRI & Specialist Chambers",
            address = "Hospital Road, Kacheri Bazar",
            landmark = "Opposite DHH Main Gate, Balasore",
            openHours = "06:30 AM - 09:30 PM",
            sampleCollectionTimings = "06:30 AM - 11:00 AM",
            facilities = listOf("High-Speed CT Scan", "Color Doppler USG", "ECG & PFT", "NABL Blood Collection"),
            visitingSpecialistsList = listOf("Dr. D. Mohanty (Medicine)", "Dr. R. K. Rout (Orthopedics)", "Dr. M. Panda (Neurology)"),
            phone = "06782-262888",
            latitude = 21.4920,
            longitude = 86.9255
        )
    )

    val privateHospitalsList: List<PrivateHospital> = listOf(
        PrivateHospital(
            id = "hosp_1",
            name = "Jyoti Hospital (Multi-Speciality & Trauma)",
            odiaName = "ଜ୍ୟୋତି ହସ୍ପିଟାଲ (କୁରୁଡ଼ା, ବାଲେଶ୍ୱର)",
            category = "Multi-Speciality & 24x7 Trauma Care",
            address = "NH-16 Bypass, Kuruda, Balasore",
            landmark = "Near Kuruda Chhak Overbridge",
            totalBeds = 180,
            icuBedsCount = 28,
            is24x7Emergency = true,
            hasDialysisUnit = true,
            hasNicuPicU = true,
            hasBloodStorageOrBank = true,
            acceptedSchemes = listOf("BSKY Nabin Card", "Ayushman Bharat", "Star Health & Cashless TPA"),
            specialtiesAvailable = listOf("24x7 Trauma & Neuro Surgery", "Cardiology & ICU", "Nephrology & Dialysis", "Obstetrics & NICU", "Orthopedics"),
            keySurgeonsAndDoctors = listOf("Dr. S. K. Nayak (Chief Surgeon)", "Dr. Ipsita Das (Pediatrics)", "Dr. B. K.Lenka (Critical Care)"),
            opdTimings = "08:30 AM - 02:00 PM & 05:00 PM - 08:00 PM",
            visitingHours = "04:30 PM - 06:30 PM",
            emergencyPhone = "06782-255890",
            receptionPhone = "06782-255891",
            latitude = 21.4640,
            longitude = 86.8980
        ),
        PrivateHospital(
            id = "hosp_2",
            name = "Life Care Hospital & Maternity Centre",
            odiaName = "ଲାଇଫ୍ କେୟାର ହସ୍ପିଟାଲ ବାଲେଶ୍ୱର",
            category = "Multi-Speciality & Maternity Nursing Home",
            address = "OT Road, Near Fakir Mohan Golai",
            landmark = "Station Square Corridor, Balasore",
            totalBeds = 110,
            icuBedsCount = 16,
            is24x7Emergency = true,
            hasDialysisUnit = true,
            hasNicuPicU = true,
            hasBloodStorageOrBank = false,
            acceptedSchemes = listOf("BSKY Card", "ESIC", "Major Private Health Insurers"),
            specialtiesAvailable = listOf("Laparoscopic Surgery", "Maternity & High-Risk Obstetrics", "Orthopedics", "General Medicine"),
            keySurgeonsAndDoctors = listOf("Dr. R. K. Rout (Ortho Surgeon)", "Dr. S. Pattnaik (Obs & Gyn)"),
            opdTimings = "09:00 AM - 01:30 PM & 05:30 PM - 08:30 PM",
            visitingHours = "05:00 PM - 07:00 PM",
            emergencyPhone = "06782-263110",
            receptionPhone = "06782-263112",
            latitude = 21.4985,
            longitude = 86.9288
        )
    )

    fun getDiagnosticCollectionStatus(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..10 -> "Fasting Blood Draws Open Now (06:30 AM - 10:30 AM) • Home Phlebotomy Active"
            in 11..19 -> "Routine Pathology & Diagnostic Imaging Active • Same-Day Digital Reports"
            else -> "24x7 STAT Emergency Diagnostics Open at DHH & FMMCH Nidan Centre"
        }
    }

    fun getDoctorDailyStatus(doctor: DoctorProfile): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 8..13 -> "Morning OPD Open Now (${doctor.opdTimings})"
            in 17..20 -> "Evening Chamber Active • Call ${doctor.phone} for token"
            else -> "Next OPD Slot Available • 24x7 Emergency On-Call at ${doctor.affiliationHospital}"
        }
    }

    fun isStoreOpenNow(store: MedicineStore): Boolean {
        if (store.is24x7) return true
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 7..22
    }

    fun isHomeCollectionActiveNow(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (hour in 6..11) {
            "Active Now (Phlebotomists on route across Balasore Town, Remuna & Kuruda)"
        } else {
            "Pre-booking Open for Tomorrow 06:30 AM Fasting Slot"
        }
    }

    fun getPathologyLabDailyStatus(lab: PathologyLab): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (lab.openingHours.contains("24 Hours", ignoreCase = true) || hour in 6..20) {
            "Open Now • Reports in 4-6 hrs"
        } else {
            "Opens at 06:30 AM for Fasting Sample Draw"
        }
    }

    fun getHospitalDailyStatus(hospital: PrivateHospital): String {
        return if (hospital.is24x7Emergency) {
            "24x7 Casualty, Trauma & ICU Active (${hospital.icuBedsCount} ICU Beds)"
        } else {
            "OPD & Inpatient Wing Open (${hospital.opdTimings})"
        }
    }
}
