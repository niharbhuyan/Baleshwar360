/**
 * Server-Side Firebase Admin SDK Initialization for Baleshwar 360
 * Project ID: gen-lang-client-0068979333
 * Firestore Database ID: ai-studio-android-balasore-011eecca-ad37-48d7-9ed9-0ca2c072c765
 *
 * SECURITY NOTE:
 * - Never bundle serviceAccountKey.json inside the Android APK (app/src/main/assets or res/).
 * - On Android, client authentication uses app/google-services.json + Google Sign-In,
 *   and admin privileges for niharbhuyan@gmail.com are enforced via firestore.rules (isAdmin()).
 * - Use this script on a secure Node.js backend/CLI by setting GOOGLE_APPLICATION_CREDENTIALS
 *   or placing serviceAccountKey.json in the project root (gitignored).
 */

const fs = require("fs");
const path = require("path");
const admin = require("firebase-admin");

const PROJECT_ID = "gen-lang-client-0068979333";
const DATABASE_ID = "ai-studio-android-balasore-011eecca-ad37-48d7-9ed9-0ca2c072c765";
const SERVICE_ACCOUNT_PATH =
  process.env.GOOGLE_APPLICATION_CREDENTIALS ||
  path.resolve(__dirname, "serviceAccountKey.json");

function initializeFirebaseAdmin() {
  if (admin.apps.length > 0) {
    return admin.app();
  }

  if (fs.existsSync(SERVICE_ACCOUNT_PATH)) {
    const serviceAccount = require(SERVICE_ACCOUNT_PATH);
    return admin.initializeApp({
      credential: admin.credential.cert(serviceAccount),
      projectId: PROJECT_ID,
      storageBucket: `${PROJECT_ID}.firebasestorage.app`,
    });
  }

  return admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    projectId: PROJECT_ID,
    storageBucket: `${PROJECT_ID}.firebasestorage.app`,
  });
}

module.exports = {
  admin,
  PROJECT_ID,
  DATABASE_ID,
  initializeFirebaseAdmin,
};
