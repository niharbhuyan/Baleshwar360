const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read or write weather preferences", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("preferences")
      .doc("weather")
      .get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("preferences")
      .doc("weather")
      .set({
        userId: ALICE_UID,
        notifyCyclone: true,
        notifyHighTide: true,
        notifySevereHeat: true,
        notifyRainfall: true,
        updatedAt: Date.now(),
      })
  );
});

test("Authenticated user: can read and write their own full weather preferences", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("preferences")
      .doc("weather")
      .set({
        userId: ALICE_UID,
        notifyCyclone: true,
        notifyHighTide: true,
        notifySevereHeat: false,
        notifyRainfall: true,
        notifyLocalEvents: true,
        notifyTempleFestivals: true,
        notifyBreakingNews: true,
        notifyTourismHotspots: true,
        notifyDefenseAdvisories: true,
        notifyFloodAlerts: true,
        notifyKuldihaSafari: true,
        notifyPanchalingeswar: true,
        notifySafeTideWindow: true,
        highTideAlertHoursBefore: 2,
        minTempThreshold: 12,
        maxTempThreshold: 38,
        preferredNewsCategories: ["Civic", "Weather", "Defense"],
        updatedAt: Date.now(),
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("preferences")
      .doc("weather")
      .get()
  );
});

test("Authenticated user: cannot read or write another user's weather preferences", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("preferences")
      .doc("weather")
      .set({
        userId: BOB_UID,
        notifyCyclone: true,
        notifyHighTide: true,
        notifySevereHeat: true,
        notifyRainfall: true,
        updatedAt: Date.now(),
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("preferences")
      .doc("weather")
      .get()
  );
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("preferences")
      .doc("weather")
      .set({
        userId: BOB_UID,
        notifyCyclone: false,
        notifyHighTide: false,
        notifySevereHeat: false,
        notifyRainfall: false,
        updatedAt: Date.now(),
      })
  );
});

test("Shadow update test: rejects ghost fields in weather preferences", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("preferences")
      .doc("weather")
      .set({
        userId: ALICE_UID,
        notifyCyclone: true,
        notifyHighTide: true,
        notifySevereHeat: true,
        notifyRainfall: true,
        ghostField: "hacked",
        updatedAt: Date.now(),
      })
  );
});

test("Authenticated user: can create and read their own profile and bookmarks", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = Date.now();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("bookmarks")
      .doc("spot_1")
      .set({
        spotId: "spot_1",
        userId: ALICE_UID,
        spotName: "Chandipur Beach",
        category: "Beach",
        location: "Chandipur",
        rating: 4.8,
        bookmarkedAt: now,
      })
  );
});

test("Weather alerts and snapshots: public read allowed, non-admin writes denied", async () => {
  const now = Date.now();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("weather_alerts").doc("alert_chandipur_cyclone").set({
      alertId: "alert_chandipur_cyclone",
      regionCode: "CHANDIPUR",
      eventType: "CYCLONE",
      severity: "WARNING",
      headline: "Coastal Wind Alert across Balasore",
      description: "High wind warning for Chandipur and Balaramgadi coast.",
      affectedBlocks: ["Chandipur", "Balaramgadi"],
      source: "OpenWeatherMap",
      startsAtMs: now,
      expiresAtMs: now + 43200000,
      isActive: true,
      updatedAtMs: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("weather_alerts").doc("alert_chandipur_cyclone").get());
  await assertFails(
    aliceDb.collection("weather_alerts").doc("alert_forged").set({
      alertId: "alert_forged",
      regionCode: "BLS_SADAR",
      eventType: "FLOOD",
      severity: "EMERGENCY",
      headline: "Forged Alert",
      description: "Unauthorized write attempt",
      affectedBlocks: ["Balasore Sadar"],
      source: "Spoofed",
      startsAtMs: now,
      expiresAtMs: now + 3600000,
      isActive: true,
      updatedAtMs: now,
    })
  );
  await assertFails(
    aliceDb.collection("weather_snapshots").doc("BLS_SADAR").set({
      regionCode: "BLS_SADAR",
      provider: "OpenWeatherMap",
      temperatureC: 32.0,
      feelsLikeC: 35.0,
      humidityPct: 75,
      windSpeedKmh: 14.0,
      rainMm3h: 0.0,
      weatherCode: "Clear",
      conditionSummary: "Clear Sky",
      floodRiskLevel: "NORMAL",
      observedAtMs: now,
    })
  );
});

test("Tourism hotspots: public read allowed, non-admin writes denied", async () => {
  const now = Date.now();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("tourism_hotspots").doc("spot_chandipur_beach").set({
      spotId: "spot_chandipur_beach",
      regionCode: "CHANDIPUR",
      nameEn: "Chandipur Beach (Vanishing Sea)",
      nameOr: "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
      shortDescription: "Sea recedes up to 5 km twice daily during low tide.",
      description: "Iconic Bay of Bengal vanishing beach in Balasore.",
      category: "BEACH",
      heroImageUrl: "drawable://hero_chandipur_beach",
      latitude: 21.4416,
      longitude: 87.0149,
      address: "Chandipur Sea Beach, Balasore, Odisha 756025",
      rating: 4.8,
      isFeatured: true,
      updatedAtMs: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("tourism_hotspots").doc("spot_chandipur_beach").get());
  await assertFails(
    aliceDb.collection("tourism_hotspots").doc("spot_forged").set({
      spotId: "spot_forged",
      regionCode: "REMUNA",
      nameEn: "Forged Spot",
      nameOr: "ନକଲି",
      shortDescription: "Unauthorized write",
      description: "Should be denied",
      category: "TEMPLE",
      heroImageUrl: "https://example.com/img.jpg",
      latitude: 21.529,
      longitude: 86.8712,
      address: "Remuna",
      rating: 4.5,
      isFeatured: false,
      updatedAtMs: now,
    })
  );
});


