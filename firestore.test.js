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
