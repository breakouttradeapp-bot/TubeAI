# TubeBoost AI – Video SEO Tags Generator

A production-ready Android app that helps video creators generate SEO tags, viral titles, hashtags, and descriptions using AI.

---

## 📁 Project Structure

```
TubeBoostAI/
├── app/
│   ├── build.gradle                    # App-level build config
│   ├── google-services.json            # ⚠️ Replace with YOUR Firebase config
│   ├── proguard-rules.pro              # ProGuard rules
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/tubeboost/ai/
│       │   ├── TubeBoostApplication.kt # App class (AdMob + Firebase init)
│       │   ├── ui/
│       │   │   ├── SplashActivity.kt   # Animated splash screen
│       │   │   ├── MainActivity.kt     # Home screen
│       │   │   └── ResultActivity.kt   # Results display
│       │   ├── viewmodel/
│       │   │   └── MainViewModel.kt    # MVVM ViewModel
│       │   ├── network/
│       │   │   ├── ApiService.kt       # Retrofit interface
│       │   │   └── RetrofitClient.kt   # Singleton Retrofit client
│       │   ├── model/
│       │   │   ├── RequestModel.kt     # API request models
│       │   │   └── ResponseModel.kt    # API response + UI models
│       │   └── utils/
│       │       ├── NetworkUtils.kt     # Internet connectivity check
│       │       └── AdManager.kt        # Centralized AdMob manager
│       └── res/
│           ├── layout/                 # Activity layouts
│           ├── values/                 # Colors, strings, themes, dimens
│           └── drawable/               # Vector icons, backgrounds
├── build.gradle                        # Root build config
└── settings.gradle
```

---

## 🚀 Setup Instructions

### Step 1: Get Your Cerebras API Key
1. Go to [https://cloud.cerebras.ai](https://cloud.cerebras.ai)
2. Create an account and generate an API key
3. Open `app/build.gradle`
4. Replace `"YOUR_CEREBRAS_API_KEY_HERE"` with your actual key:
   ```groovy
   buildConfigField "String", "CEREBRAS_API_KEY", '"csk-your-actual-key-here"'
   ```

### Step 2: Set Up Firebase
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project named "TubeBoostAI"
3. Add an Android app with package name: `com.tubeboost.ai`
4. Download `google-services.json`
5. Replace the placeholder `app/google-services.json` with your downloaded file
6. Enable **Crashlytics** in the Firebase console

### Step 3: Set Up AdMob
1. Go to [AdMob Console](https://admob.google.com)
2. Create a new app
3. Get your **App ID** (format: `ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX`)
4. Open `app/build.gradle` and replace the test App ID:
   ```groovy
   manifestPlaceholders = [admobAppId: "ca-app-pub-YOUR-REAL-APP-ID"]
   ```
5. Create 3 ad units:
   - **Banner Ad** → replace `BANNER_AD_UNIT_ID` in `AdManager.kt`
   - **Interstitial Ad** → replace `INTERSTITIAL_AD_UNIT_ID` in `AdManager.kt`
   - **Rewarded Ad** → replace `REWARDED_AD_UNIT_ID` in `AdManager.kt`

### Step 4: Privacy Policy
1. Create a privacy policy at your website
2. Update the URL in `strings.xml`:
   ```xml
   <string name="privacy_url">https://yourwebsite.com/privacy</string>
   ```

### Step 5: Build & Run
```bash
# Debug build
./gradlew assembleDebug

# Release AAB for Play Store
./gradlew bundleRelease
```

---

## 🔑 Key Configuration Files

| File | What to Update |
|------|---------------|
| `app/build.gradle` | `CEREBRAS_API_KEY`, `admobAppId` |
| `app/google-services.json` | Replace with your Firebase config |
| `utils/AdManager.kt` | Replace test ad unit IDs with real ones |
| `values/strings.xml` | `privacy_url` |

---

## 🏗️ Architecture

- **Pattern**: MVVM (Model-View-ViewModel)
- **UI**: Material Design 3, ViewBinding
- **Networking**: Retrofit 2 + OkHttp + Gson
- **Async**: Kotlin Coroutines + LiveData
- **Ads**: Google AdMob (Banner + Interstitial + Rewarded)
- **Monitoring**: Firebase Crashlytics

---

## 🎯 Features

| Feature | Implementation |
|---------|---------------|
| AI Tag Generation | Cerebras API (OpenAI-compatible) |
| SEO Tags | 30 ranked keywords per generation |
| Viral Titles | 5 click-worthy titles |
| Description | 150-200 word optimized description |
| Hashtags | 15 trending hashtags |
| Bonus Tags | 30 extra tags via rewarded ad |
| Copy to Clipboard | Per-section copy buttons |
| Share Result | Full formatted share sheet |
| Banner Ads | Bottom of Home + Result screens |
| Interstitial Ads | Every 3rd generation |
| Rewarded Ads | Unlock 30 bonus tags |
| Crash Reporting | Firebase Crashlytics |
| Offline Detection | Graceful network error handling |

---

## 🛡️ Play Store Compliance

- ✅ No "YouTube" trademark in UI or package names
- ✅ Uses "Video Creator Tools" branding
- ✅ Minimal permissions (INTERNET + ACCESS_NETWORK_STATE only)
- ✅ No forced ad clicks
- ✅ No ads on splash screen
- ✅ Privacy policy URL included
- ✅ ProGuard enabled for release builds

---

## 📊 Ad Unit IDs (Replace Before Publishing)

The app currently uses Google's **test ad unit IDs**. Before publishing to Play Store, replace all IDs in `AdManager.kt` with your real AdMob ad unit IDs.

Test IDs used:
- Banner: `ca-app-pub-3940256099942544/6300978111`
- Interstitial: `ca-app-pub-3940256099942544/1033173712`
- Rewarded: `ca-app-pub-3940256099942544/5224354917`

---

## 🔧 Minimum Requirements

- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Kotlin**: 1.9.22
- **AGP**: 8.2.2
- **Java**: 17
