# On Tha Set — Android (Kotlin + Jetpack Compose)

This is the Android port of the iOS SwiftUI app **OnThaSet (New)**. It talks to the same Supabase project, so events, ads, profiles, photos and bike builds are shared between iOS and Android.

## Open in Android Studio
1. Unzip, then **File → Open…** and select the `OnThaSetAndroid` folder (the one containing `settings.gradle.kts`).
2. Let Gradle sync (Android Studio downloads Gradle 8.11.1, AGP 8.7.3, Kotlin 2.0.21 and the libraries).
   If Studio offers to upgrade AGP/Gradle, it's safe to accept.
3. Pick an emulator or phone (Android 8.0 / API 26+) and press **Run**.

## Tech mapping (iOS → Android)
| iOS | Android |
|---|---|
| SwiftUI views | Jetpack Compose screens (`ui/…`) |
| NavigationStack / sheets | Navigation Compose (`ui/navigation/AppNavHost.kt`) |
| SwiftData (`Event`, `UserProfile`, `EventPhoto`, `BikeProgress`) | JSON-backed `LocalStore` + image files |
| URLSession REST calls to Supabase | OkHttp (`data/Http.kt`, `data/SupabaseManager.kt`) — same endpoints, tables and buckets |
| Supabase Swift auth (email/password) | Supabase Auth REST (`data/AuthService.kt`) |
| StoreKit 2 | Google Play Billing (`billing/BillingManager.kt`) |
| CoreLocation / CLGeocoder | Fused Location + `Geocoder` (`location/LocationManager.kt`) |
| MapKit (event mini-map) | Leaflet/OpenStreetMap in a WebView (no API key needed) |
| National Run Calendar WKWebView map | Same Leaflet page (`assets/us_map.html`) in a WebView |
| PhotosPicker / camera | Android Photo Picker / camera intent |
| UIActivityViewController, mailto, sms, Maps/Waze | Android share sheet & intents |

## Things you need to do
1. **Google Play products** — in Play Console create:
   - In-app product `com.onthaset.singlepost` ($0.99, consumable)
   - Subscription `com.hayesenterprisellc.onthaset.membership.monthly` ($2.99/month, one base plan)
   Purchases only work on a build installed from a Play testing track with a licence-tester account.
2. **Owner account** — `AppConfig.ownerUserIDs` (in `data/Config.kt`) contains your Apple user id, so signing in with Apple on Android gives you owner access. If you use an email account instead, add its Supabase user id (uppercase UUID).
3. **Password reset emails** — Supabase's reset link redirects to your Site URL; make sure that's set to something sensible in Supabase → Authentication → URL Configuration.
4. **Signing** — create a release keystore via *Build → Generate Signed App Bundle* when you're ready to publish.

## Shared sign-in (same account on iOS and Android)
- **Email/password**: works on both apps already — same Supabase Auth user, same id.
- **Sign in with Apple**: Android uses Supabase OAuth and keys the account by Apple's user id (`sub`), which is the same id iOS stores in `users.apple_user_id`, so iOS Apple users see their own profile, events, photos and builds.
  One-time setup:
  1. Apple Developer → Identifiers → **Services IDs** → create one (e.g. `com.hayesenterprisesllc.onthaset.web`), enable *Sign in with Apple*, primary App ID = your iOS app, Return URL = `https://zlvqhowflvgyaythfzkx.supabase.co/auth/v1/callback`.
  2. Apple Developer → Keys → create a key with *Sign in with Apple* (download the .p8; note Key ID and Team ID).
  3. Supabase → Authentication → Providers → **Apple**: enable, Client IDs = `com.hayesenterprisesllc.onthaset.web,com.hayesenterprisesllc.onthaset`, and generate the Secret Key from the .p8 (Supabase has a generator; it expires every 6 months).
  4. Supabase → Authentication → URL Configuration → **Redirect URLs**: add `onthaset://auth-callback`.

## Behaviour differences from iOS
- **Sign in with Apple works on Android** through Supabase in the browser (see *Shared sign-in* below). Email/password and "Explore Without Signing In" work too.
- The signed-in user stays signed in across app restarts (iOS required signing in again each launch).
- Legal text and "We accept" panel mention Google Play / Google Pay instead of Apple. Have the policies reviewed before publishing.
- Small bug fixes: ad sign-up now sends its `notes` (appointment-only / hidden-address flags) to Supabase, editing an event now saves the new flyer URL, and posts are attributed to the signed-in user rather than the first local profile.
- Subscribers who've used their 4 monthly posts are sent to the "Get More Posts" screen (iOS had this screen but never reached it).
- Unused iOS files were not ported (old `ProfileView`, `HomePage`, `ContentView`, `LoginView`, `MainView`, standalone photo/bike feeds, `EventSectionView`, `HighlightedEventRow`, location-prompt coordinator). The local-event share screen uses the standard share layout rather than the generated 1080×1920 story image.

## Project layout
```
app/src/main/java/com/hayesenterprisesllc/onthaset/
  data/        Config, Prefs, Models, LocalStore, Http, SupabaseManager, AuthService
  billing/     Google Play Billing
  location/    location + geocoding
  weather/     Open-Meteo weather view model
  util/        dates, images, intents
  ui/          screens grouped by feature (home, events, national, weather, profile, posting, ads, payment, admin, legal, auth)
```
