# Firebase Hosting & AdMob `app-ads.txt` Deployment Guide for Baleshwar 360

Your workspace is pre-configured for **Firebase Hosting** (`gen-lang-client-0068979333.web.app`) so Google AdMob can crawl `/app-ads.txt` at your root domain.

---

## 🔥 Option 1: Deploy to Firebase Hosting (`gen-lang-client-0068979333.web.app`)

All required configuration files are already set up in your project root:
- `.firebaserc` &rarr; Bound to project `gen-lang-client-0068979333`
- `firebase.json` &rarr; Configured to serve `public/` on site `gen-lang-client-0068979333` with `text/plain` headers for `/app-ads.txt`
- `public/app-ads.txt` &rarr; Contains `google.com, pub-4880243637225183, DIRECT, f08c47fec0942fa0`
- `public/index.html` & `public/privacy.html` &rarr; Official app landing page and Privacy Policy

### Run these 3 commands on your local machine (after exporting/cloning the project):
```bash
# 1. Install Firebase CLI globally (if not already installed)
npm install -g firebase-tools

# 2. Sign in to your Google account (niharbhuyan@gmail.com)
firebase login

# 3. Deploy Hosting directly (no need to run 'firebase init' — .firebaserc and firebase.json are already configured!)
firebase deploy --only hosting --project gen-lang-client-0068979333
```

### Live URLs After Deployment:
- **Developer Website (Enter this in Google Play Console &rarr; Store settings &rarr; Website):**  
  `https://gen-lang-client-0068979333.web.app`
- **Live AdMob `app-ads.txt` URL:**  
  `https://gen-lang-client-0068979333.web.app/app-ads.txt`
- **Live Privacy Policy URL:**  
  `https://gen-lang-client-0068979333.web.app/privacy`

---

## ⚡ Option 2: Instant Shared App URL (Already Live)

- **Developer Website URL:**  
  `https://ais-pre-honzkaxc7yscu2h42zeagl-613265325843.asia-east1.run.app`
- **Live `app-ads.txt` URL:**  
  `https://ais-pre-honzkaxc7yscu2h42zeagl-613265325843.asia-east1.run.app/app-ads.txt`

---

## 📋 Exact Record in `public/app-ads.txt`:
```text
google.com, pub-4880243637225183, DIRECT, f08c47fec0942fa0
```
