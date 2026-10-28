package com.hayesenterprisesllc.onthaset.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hayesenterprisesllc.onthaset.data.AuthService
import com.hayesenterprisesllc.onthaset.data.LocalEvent
import com.hayesenterprisesllc.onthaset.data.LocalStore
import com.hayesenterprisesllc.onthaset.data.LocationParts
import com.hayesenterprisesllc.onthaset.data.SupabaseAd
import com.hayesenterprisesllc.onthaset.data.SupabaseEvent
import com.hayesenterprisesllc.onthaset.ui.admin.AdminDashboardScreen
import com.hayesenterprisesllc.onthaset.ui.admin.AdminLockScreen
import com.hayesenterprisesllc.onthaset.ui.ads.AdDetailScreen
import com.hayesenterprisesllc.onthaset.ui.ads.AdEditPinScreen
import com.hayesenterprisesllc.onthaset.ui.ads.AdvertiserSignupScreen
import com.hayesenterprisesllc.onthaset.ui.ads.EditAdScreen
import com.hayesenterprisesllc.onthaset.ui.events.EventHomeScreen
import com.hayesenterprisesllc.onthaset.ui.events.EventShareScreen
import com.hayesenterprisesllc.onthaset.ui.events.NearbyEventsScreen
import com.hayesenterprisesllc.onthaset.ui.events.ReportEventScreen
import com.hayesenterprisesllc.onthaset.ui.events.SupabaseEventDetailScreen
import com.hayesenterprisesllc.onthaset.ui.home.HomeScreen
import com.hayesenterprisesllc.onthaset.ui.legal.AboutScreen
import com.hayesenterprisesllc.onthaset.ui.legal.PrivacyPolicyScreen
import com.hayesenterprisesllc.onthaset.ui.legal.TermsOfServiceScreen
import com.hayesenterprisesllc.onthaset.ui.national.NationalEventDetailScreen
import com.hayesenterprisesllc.onthaset.ui.national.NationalRunCalendarScreen
import com.hayesenterprisesllc.onthaset.ui.payment.ExtraPostPurchaseScreen
import com.hayesenterprisesllc.onthaset.ui.payment.PaymentSelectionScreen
import com.hayesenterprisesllc.onthaset.ui.posting.AddEditEventScreen
import com.hayesenterprisesllc.onthaset.ui.posting.EditLocalEventScreen
import com.hayesenterprisesllc.onthaset.ui.posting.LocalEventDetailScreen
import com.hayesenterprisesllc.onthaset.ui.posting.UploadBikeProgressScreen
import com.hayesenterprisesllc.onthaset.ui.posting.UploadEventPhotoScreen
import com.hayesenterprisesllc.onthaset.ui.profile.EditProfileScreen
import com.hayesenterprisesllc.onthaset.ui.profile.MyAccountScreen
import com.hayesenterprisesllc.onthaset.ui.profile.PostType
import com.hayesenterprisesllc.onthaset.ui.profile.PostedByProfileScreen
import com.hayesenterprisesllc.onthaset.ui.profile.SettingsScreen
import com.hayesenterprisesllc.onthaset.ui.profile.WelcomeSetupScreen
import com.hayesenterprisesllc.onthaset.ui.weather.WeatherScreen
import com.hayesenterprisesllc.onthaset.ui.weather.WeatherSource

private const val NATIONAL_DETAIL = "nationalDetail"

/** Navigation graph replacing every NavigationLink / sheet / fullScreenCover in the iOS app. */
@Composable
fun AppNavHost(nav: NavHostController = rememberNavController()) {
    fun back() { nav.popBackStack() }
    fun openEvent(e: SupabaseEvent, distance: Double? = null) {
        NavData.selectedEvent = e; NavData.selectedDistance = distance; nav.navigate(Routes.EVENT_DETAIL)
    }
    fun openAd(ad: SupabaseAd) { NavData.selectedAd = ad; nav.navigate(Routes.AD_DETAIL) }
    fun openWeatherFor(lat: Double, lng: Double, locationName: String, title: String) {
        val venue = LocationParts.split(locationName).firstOrNull() ?: title
        if (lat != 0.0 && lng != 0.0) nav.navigate(Routes.weatherCoords(lat, lng, venue))
        else nav.navigate(Routes.weatherCity(LocationParts.city(locationName)))
    }
    fun shareSelected() { nav.navigate(Routes.EVENT_SHARE) }

    NavHost(
        navController = nav,
        startDestination = Routes.HOME,
        enterTransition = { slideInHorizontally { it } + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onViewEvents = { nav.navigate(Routes.events("list")) },
                onNearby = { nav.navigate(Routes.NEARBY) },
                onNational = { nav.navigate(Routes.NATIONAL) },
                onWeather = { nav.navigate(Routes.WEATHER) },
                onAccount = { nav.navigate(Routes.ACCOUNT) },
                onWelcomeSetup = { nav.navigate(Routes.WELCOME_SETUP) },
                onAdvertise = { nav.navigate(Routes.ADVERTISE) },
                onAdClick = ::openAd,
                onAdminLock = { nav.navigate(Routes.ADMIN_LOCK) },
                onAbout = { nav.navigate(Routes.ABOUT) },
                onPrivacy = { nav.navigate(Routes.PRIVACY) },
                onTerms = { nav.navigate(Routes.TERMS) }
            )
        }

        // ── Events ───────────────────────────────────────────────────────────
        composable(Routes.EVENTS, arguments = listOf(navArgument("mode") { type = NavType.StringType; defaultValue = "list" })) { entry ->
            EventHomeScreen(
                initialMode = entry.arguments?.getString("mode") ?: "list",
                onBack = ::back, onEventClick = { openEvent(it) }, onAdClick = ::openAd
            )
        }
        composable(Routes.EVENT_DETAIL) {
            val e = NavData.selectedEvent
            if (e == null) { LaunchedEffect(Unit) { back() }; return@composable }
            SupabaseEventDetailScreen(
                event = e,
                distanceMiles = NavData.selectedDistance,
                onBack = ::back,
                onShare = ::shareSelected,
                onReport = { nav.navigate(Routes.report(e.id ?: e.title, e.title)) },
                onWeather = { openWeatherFor(e.latitude, e.longitude, e.locationName, e.title) },
                onPostedBy = { nav.navigate(Routes.postedBy(e.postedByUserID, e.postedByName)) }
            )
        }
        composable(Routes.EVENT_SHARE) {
            val e = NavData.selectedEvent
            if (e == null) { LaunchedEffect(Unit) { back() }; return@composable }
            EventShareScreen(e.title, e.date, e.locationName, e.details, e.category, e.imageURL, onClose = ::back)
        }
        composable(Routes.NEARBY) {
            NearbyEventsScreen(onBack = ::back, onEventClick = { openEvent(it.toSupabaseEvent(), it.distanceMiles) }, onAdClick = ::openAd)
        }
        composable(Routes.NATIONAL) {
            NationalRunCalendarScreen(onBack = ::back, onEventClick = { NavData.selectedEvent = it; nav.navigate(NATIONAL_DETAIL) })
        }
        composable(NATIONAL_DETAIL) {
            val e = NavData.selectedEvent
            if (e == null) { LaunchedEffect(Unit) { back() }; return@composable }
            NationalEventDetailScreen(
                e, onClose = ::back,
                onWeather = { openWeatherFor(e.latitude, e.longitude, e.locationName, e.title) },
                onShare = ::shareSelected
            )
        }
        composable(Routes.REPORT, arguments = listOf(navArgument("eventId") { type = NavType.StringType }, navArgument("title") { type = NavType.StringType })) { entry ->
            ReportEventScreen(entry.arguments?.getString("eventId").navArg(), entry.arguments?.getString("title").navArg(), onClose = ::back)
        }
        composable(Routes.POSTED_BY, arguments = listOf(navArgument("userId") { type = NavType.StringType }, navArgument("name") { type = NavType.StringType })) { entry ->
            PostedByProfileScreen(entry.arguments?.getString("userId").navArg(), entry.arguments?.getString("name").navArg(), onBack = ::back)
        }

        // ── Weather ──────────────────────────────────────────────────────────
        composable(Routes.WEATHER) { WeatherScreen(WeatherSource.Gps, onBack = ::back, onAdClick = ::openAd) }
        composable(
            Routes.WEATHER_COORDS,
            arguments = listOf(
                navArgument("lat") { type = NavType.StringType }, navArgument("lng") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { entry ->
            val lat = entry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = entry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0
            WeatherScreen(WeatherSource.Coordinates(lat, lng, entry.arguments?.getString("name").navArg()), onBack = ::back, onAdClick = ::openAd)
        }
        composable(Routes.WEATHER_CITY, arguments = listOf(navArgument("city") { type = NavType.StringType })) { entry ->
            WeatherScreen(WeatherSource.City(entry.arguments?.getString("city").navArg()), onBack = ::back, onAdClick = ::openAd)
        }

        // ── Account & posting ────────────────────────────────────────────────
        composable(Routes.ACCOUNT) {
            MyAccountScreen(
                onBack = ::back,
                onEditProfile = { nav.navigate(Routes.EDIT_PROFILE) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onWelcomeSetup = { nav.navigate(Routes.WELCOME_SETUP) },
                onPost = { t ->
                    when (t) {
                        PostType.EVENT -> nav.navigate(Routes.POST_EVENT)
                        PostType.PHOTO -> nav.navigate(Routes.UPLOAD_PHOTO)
                        PostType.BIKE -> nav.navigate(Routes.UPLOAD_BIKE)
                    }
                },
                onPayment = { nav.navigate(Routes.PAYMENT) },
                onExtraPost = { nav.navigate(Routes.EXTRA_POST) },
                onLocalEventClick = { e: LocalEvent -> nav.navigate(Routes.localEvent(e.id)) }
            )
        }
        composable(Routes.WELCOME_SETUP) {
            val profile = LocalStore.profileFor(AuthService.currentUserId)
            if (profile == null) { LaunchedEffect(Unit) { back() }; return@composable }
            WelcomeSetupScreen(profile) {
                nav.navigate(Routes.ACCOUNT) { popUpTo(Routes.HOME) }
            }
        }
        composable(Routes.EDIT_PROFILE) { EditProfileScreen(AuthService.currentUserId.orEmpty(), onClose = ::back) }
        composable(Routes.SETTINGS) {
            SettingsScreen(onDone = ::back, onSignedOut = { nav.popBackStack(Routes.HOME, inclusive = false) })
        }
        composable(Routes.POST_EVENT) { AddEditEventScreen(onClose = ::back) }
        composable(Routes.UPLOAD_PHOTO) { UploadEventPhotoScreen(onClose = ::back) }
        composable(Routes.UPLOAD_BIKE) { UploadBikeProgressScreen(onClose = ::back) }
        composable(Routes.PAYMENT) {
            PaymentSelectionScreen(onClose = ::back, onPurchased = {
                nav.navigate(Routes.POST_EVENT) { popUpTo(Routes.PAYMENT) { inclusive = true } }
            })
        }
        composable(Routes.EXTRA_POST) {
            ExtraPostPurchaseScreen(onClose = ::back, onPurchased = {
                nav.navigate(Routes.POST_EVENT) { popUpTo(Routes.EXTRA_POST) { inclusive = true } }
            })
        }

        // ── Local (on-device) events ─────────────────────────────────────────
        composable(Routes.LOCAL_EVENT_DETAIL, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            LocalEventDetailScreen(
                eventId = id,
                onBack = ::back,
                onEdit = { nav.navigate(Routes.localEventEdit(id)) },
                onShare = { nav.navigate(Routes.localEventShare(id)) },
                onReport = {
                    val e = LocalStore.events.value.firstOrNull { it.id == id }
                    nav.navigate(Routes.report(id, e?.title ?: ""))
                },
                onWeather = { e -> openWeatherFor(e.latitude, e.longitude, e.locationName, e.title) },
                onPostedBy = { e -> nav.navigate(Routes.postedBy(e.postedByUserID, e.postedByName)) }
            )
        }
        composable(Routes.LOCAL_EVENT_EDIT, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            EditLocalEventScreen(entry.arguments?.getString("id").orEmpty(), onClose = ::back)
        }
        composable(Routes.LOCAL_EVENT_SHARE, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            val e = LocalStore.events.value.firstOrNull { it.id == entry.arguments?.getString("id") }
            if (e == null) { LaunchedEffect(Unit) { back() }; return@composable }
            EventShareScreen(e.title, e.date, e.locationName, e.details, e.category.rawValue, e.imagePath, onClose = ::back)
        }

        // ── Ads ──────────────────────────────────────────────────────────────
        composable(Routes.ADVERTISE) { AdvertiserSignupScreen(onClose = ::back) }
        composable(Routes.AD_DETAIL) {
            val ad = NavData.selectedAd
            if (ad == null) { LaunchedEffect(Unit) { back() }; return@composable }
            AdDetailScreen(ad, onClose = ::back, onEdit = { nav.navigate(Routes.AD_PIN) })
        }
        composable(Routes.AD_PIN) {
            val ad = NavData.selectedAd
            if (ad == null) { LaunchedEffect(Unit) { back() }; return@composable }
            AdEditPinScreen(ad, onBack = ::back, onUnlocked = { nav.navigate(Routes.AD_EDIT) { popUpTo(Routes.AD_PIN) { inclusive = true } } })
        }
        composable(Routes.AD_EDIT) {
            val ad = NavData.selectedAd
            if (ad == null) { LaunchedEffect(Unit) { back() }; return@composable }
            EditAdScreen(ad, onBack = ::back, onDone = { nav.popBackStack(Routes.HOME, inclusive = false) })
        }

        // ── Admin & info ─────────────────────────────────────────────────────
        composable(Routes.ADMIN_LOCK) {
            AdminLockScreen(onCancel = ::back, onUnlocked = { nav.navigate(Routes.ADMIN) { popUpTo(Routes.ADMIN_LOCK) { inclusive = true } } })
        }
        composable(Routes.ADMIN) { AdminDashboardScreen(onBack = ::back, onViewEvent = { openEvent(it) }) }
        composable(Routes.ABOUT) { AboutScreen(onBack = ::back) }
        composable(Routes.PRIVACY) { PrivacyPolicyScreen(onClose = ::back) }
        composable(Routes.TERMS) { TermsOfServiceScreen(onClose = ::back) }
    }
}
