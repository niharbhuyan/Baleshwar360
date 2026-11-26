const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "citizen_alice_balasore";
const BOB_UID = "citizen_bob_balasore";

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

function buildValidUserReport(userId, reportId = "BLS-CIVIC-2026-4123") {
  const now = Date.now();
  return {
    id: reportId,
    userId,
    userName: "Alice Mohanty",
    title: "Deep pothole near Phandi Chhak",
    category: "pothole",
    description: "Large water-filled pothole near Ward 14 OT Road junction.",
    wardLocation: "Ward 14 - OT Road / Phandi Chhak",
    status: "SUBMITTED",
    urgency: "HIGH",
    latitude: 21.4942,
    longitude: 86.9318,
    geoAddress: "OT Road, Phandi Chhak, Balasore, Odisha",
    photoBase64: "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAA==",
    reportedTimestamp: now,
    lastUpdatedTimestamp: now,
  };
}

function buildValidCivicReport(userId, reportId = "BLS-CIVIC-2026-4123") {
  const now = Date.now();
  return {
    id: reportId,
    userId,
    title: "Clogged drainage near Cinema Chhak",
    category: "Drain Clog / Waterlogging",
    wardLocation: "Ward 12 - Cinema Chhak",
    description: "Stormwater drain overflowing onto road after rain.",
    status: "PENDING",
    urgency: "HIGH",
    assignedDepartment: "Drainage Desiltation Taskforce",
    reportedTimestamp: now,
    lastUpdatedTimestamp: now,
    hasPhotoAttached: true,
    photoUri: "content://com.niharsales.baleshwar360.fileprovider/civic_eye_saved_photos/issue.jpg",
    photoBase64: "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAA==",
    latitude: 21.4911,
    longitude: 86.9295,
    geoAddress: "Cinema Chhak, Balasore, Odisha",
    resolutionNotes: "Report registered via Civic Eye.",
  };
}

test("Unauthenticated user: can read public civic feed but cannot submit incidents", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertSucceeds(unauthDb.collection("user_reports").limit(20).get());
  await assertFails(
    unauthDb
      .collection("user_reports")
      .doc("BLS-CIVIC-2026-1001")
      .set(buildValidUserReport(ALICE_UID, "BLS-CIVIC-2026-1001"))
  );
  await assertFails(
    unauthDb
      .collection("civic_reports")
      .doc("BLS-CIVIC-2026-1001")
      .set(buildValidCivicReport(ALICE_UID, "BLS-CIVIC-2026-1001"))
  );
});

test("Authenticated citizen: can save Civic Eye report with camera photo & GPS coordinates to user_reports and civic_reports", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(
    aliceDb
      .collection("user_reports")
      .doc("BLS-CIVIC-2026-4123")
      .set(buildValidUserReport(ALICE_UID))
  );

  await assertSucceeds(
    aliceDb
      .collection("civic_reports")
      .doc("BLS-CIVIC-2026-4123")
      .set(buildValidCivicReport(ALICE_UID))
  );

  await assertSucceeds(aliceDb.collection("user_reports").limit(50).get());
});

test("Security bounds: rejects spoofed userId, invalid GPS coordinates, and oversized base64 photo", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Spoofed userId must fail
  await assertFails(
    aliceDb
      .collection("user_reports")
      .doc("BLS-CIVIC-2026-9001")
      .set(buildValidUserReport(BOB_UID, "BLS-CIVIC-2026-9001"))
  );

  // Out-of-range latitude must fail
  await assertFails(
    aliceDb
      .collection("user_reports")
      .doc("BLS-CIVIC-2026-9002")
      .set({
        ...buildValidUserReport(ALICE_UID, "BLS-CIVIC-2026-9002"),
        latitude: 125.0,
      })
  );

  // Oversized photoBase64 (> 120,000 chars) must fail
  await assertFails(
    aliceDb
      .collection("user_reports")
      .doc("BLS-CIVIC-2026-9003")
      .set({
        ...buildValidUserReport(ALICE_UID, "BLS-CIVIC-2026-9003"),
        photoBase64: "A".repeat(120001),
      })
  );
});

test("Cross-user isolation: another user cannot overwrite or delete Alice's Civic Eye incident", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  const report = buildValidUserReport(ALICE_UID, "BLS-CIVIC-2026-5555");
  await assertSucceeds(
    aliceDb.collection("user_reports").doc("BLS-CIVIC-2026-5555").set(report)
  );

  await assertFails(
    bobDb
      .collection("user_reports")
      .doc("BLS-CIVIC-2026-5555")
      .update({ title: "Tampered by Bob" })
  );

  await assertFails(
    bobDb.collection("user_reports").doc("BLS-CIVIC-2026-5555").delete()
  );
});
