package dev.kindling.android.helper.launcher

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri

// ─────────────────────────────────────────────
//  NavigationDestination
// ─────────────────────────────────────────────

/**
 * Décrit une destination de navigation / carte.
 *
 * Priorité de résolution :
 * 1. Coordonnées ([latitude]/[longitude]) → pin précis
 * 2. Adresse ([address])                  → géocodée par l'app de navigation
 *
 * Au moins l'une des deux sources doit être fournie.
 */
data class NavigationDestination(
    val label: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
) {
    init {
        require((latitude == null) == (longitude == null)) {
            "latitude et longitude doivent être fournis ensemble"
        }
        require(latitude != null || !address.isNullOrBlank()) {
            "NavigationDestination nécessite des coordonnées ou une adresse non vide"
        }
    }

    companion object {
        /** Destination basée sur des coordonnées précises. */
        fun ofCoordinates(label: String, latitude: Double, longitude: Double) =
            NavigationDestination(label, latitude, longitude)

        /** Destination basée sur une adresse texte, géocodée par l'app de navigation. */
        fun ofAddress(label: String, address: String) =
            NavigationDestination(label, address = address)
    }
}

// ─────────────────────────────────────────────
//  TravelMode
// ─────────────────────────────────────────────

/** Mode de déplacement pour [NavigationHelper.launchDirections]. */
enum class TravelMode(internal val apiValue: String) {
    Driving("driving"),
    Walking("walking"),
    Bicycling("bicycling"),
    Transit("transit"),
}

// ─────────────────────────────────────────────
//  NavigationApp
// ─────────────────────────────────────────────

/** Une application capable de résoudre une [NavigationDestination], retournée par [NavigationHelper.getAvailableNavigationApps]. */
data class NavigationApp(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
) {
    companion object {
        const val GOOGLE_MAPS = "com.google.android.apps.maps"
        const val WAZE = "com.waze"
    }
}

// ─────────────────────────────────────────────
//  NavigationHelper
// ─────────────────────────────────────────────

/**
 * Lance l'application de navigation / cartes préférée de l'utilisateur pour une
 * [NavigationDestination], via une URI `geo:` standard (`ACTION_VIEW`), avec repli
 * navigateur (`https://maps.google.com`) si aucune app n'est installée.
 *
 * Compatible Google Maps, Waze, OsmAnd, HERE, et toute app enregistrée pour `geo:`.
 * N'affiche jamais de crash.
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { NavigationHelper(androidContext()) }
 * ```
 *
 * Utilisation dans un Composable :
 * ```kotlin
 * val context = LocalContext.current
 * val destination = NavigationDestination.ofCoordinates("Cyna HQ", 47.2184, -1.5536)
 *
 * Icon(
 *     imageVector = Icons.Default.Place,
 *     modifier    = Modifier.clickable { navigationHelper.launch(destination) }
 * )
 * ```
 */
class NavigationHelper(context: Context) {

    private val appContext = context.applicationContext

    // ── Basic launch ─────────────────────────────────────────────────────────

    /**
     * Lance l'app de navigation par défaut pour [destination].
     * Si aucune app ne résout l'URI `geo:` et [fallbackToBrowser] est `true` (défaut),
     * ouvre Google Maps dans le navigateur au lieu d'échouer silencieusement.
     */
    fun launch(
        destination: NavigationDestination,
        context: Context = appContext,
        fallbackToBrowser: Boolean = true,
    ) {
        val intent = buildGeoIntent(destination).withTaskFlagIfNeeded(context)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            if (fallbackToBrowser) {
                openInBrowser(destination, context)
            } else {
                context.toastNoApp()
            }
        }
    }

    /** `true` si au moins une application peut résoudre la [destination] donnée. */
    fun canLaunch(destination: NavigationDestination, context: Context = appContext): Boolean =
        buildGeoIntent(destination).resolveActivity(context.packageManager) != null

    // ── Directions / routing ────────────────────────────────────────────────

    /**
     * Lance des directions turn-by-turn vers [destination] avec le [travelMode] donné.
     * [origin] optionnel fixe le point de départ (sinon la position actuelle est utilisée).
     * [navigate] démarre la navigation immédiatement plutôt que d'afficher un simple aperçu.
     *
     * Utilise l'URL universelle Google Maps `/maps/dir/?api=1`, ouverte par n'importe
     * quelle app de cartes compatible ou, à défaut, par le navigateur.
     */
    fun launchDirections(
        destination: NavigationDestination,
        travelMode: TravelMode = TravelMode.Driving,
        origin: NavigationDestination? = null,
        navigate: Boolean = true,
        context: Context = appContext,
    ) {
        val url = buildDirectionsUrl(destination, travelMode, origin, navigate)
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).withTaskFlagIfNeeded(context)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            context.toastNoApp("Aucune application ou navigateur disponible.")
        }
    }

    // ── Targeting a specific app ────────────────────────────────────────────

    /**
     * Force le lancement d'une app précise (voir [NavigationApp.GOOGLE_MAPS], [NavigationApp.WAZE])
     * plutôt que de laisser le système choisir l'app par défaut.
     * Affiche un Toast si [packageName] n'est pas installé.
     */
    fun launchWithApp(
        destination: NavigationDestination,
        packageName: String,
        context: Context = appContext,
    ) {
        val intent = buildGeoIntent(destination)
            .setPackage(packageName)
            .withTaskFlagIfNeeded(context)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            context.toastNoApp("Application non installée.")
        }
    }

    /**
     * Liste les applications installées capables de résoudre [destination], pour
     * construire un sélecteur personnalisé (au lieu du chooser système).
     */
    fun getAvailableNavigationApps(
        destination: NavigationDestination,
        context: Context = appContext,
    ): List<NavigationApp> {
        val pm = context.packageManager
        val intent = buildGeoIntent(destination)
        @Suppress("DEPRECATION")
        val candidates = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return candidates.map { resolveInfo ->
            NavigationApp(
                packageName = resolveInfo.activityInfo.packageName,
                label = resolveInfo.loadLabel(pm).toString(),
                icon = runCatching { resolveInfo.loadIcon(pm) }.getOrNull(),
            )
        }
    }

    /** Affiche le sélecteur système (chooser) pour [destination], même si une app par défaut est définie. */
    fun launchChooser(
        destination: NavigationDestination,
        title: String = "Ouvrir avec",
        context: Context = appContext,
    ) {
        val chooser = Intent.createChooser(buildGeoIntent(destination), title)
            .withTaskFlagIfNeeded(context)
        try {
            context.startActivity(chooser)
        } catch (_: ActivityNotFoundException) {
            openInBrowser(destination, context)
        }
    }

    // ── Browser fallback & sharing ──────────────────────────────────────────

    /** Ouvre [destination] dans le navigateur via Google Maps (fonctionne sans app installée). */
    fun openInBrowser(destination: NavigationDestination, context: Context = appContext) {
        val intent = Intent(Intent.ACTION_VIEW, buildSearchUrl(destination).toUri())
            .withTaskFlagIfNeeded(context)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            context.toastNoApp("Aucun navigateur disponible.")
        }
    }

    /** Partage [destination] (nom + lien Google Maps) via le sélecteur de partage système. */
    fun shareLocation(destination: NavigationDestination, context: Context = appContext) {
        val url = buildSearchUrl(destination)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${destination.label}\n$url")
        }
        val chooser = Intent.createChooser(sendIntent, "Partager la position")
            .withTaskFlagIfNeeded(context)
        context.startActivity(chooser)
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun buildGeoIntent(destination: NavigationDestination): Intent =
        Intent(Intent.ACTION_VIEW, destination.toGeoUri())

    private fun Intent.withTaskFlagIfNeeded(context: Context): Intent = apply {
        if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun Context.toastNoApp(message: String = "Aucune application de navigation trouvée.") {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun NavigationDestination.toGeoUri(): Uri {
        val encodedLabel = Uri.encode(label)
        return if (latitude != null && longitude != null) {
            "geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)".toUri()
        } else {
            val query = Uri.encode(address ?: label)
            "geo:0,0?q=$query($encodedLabel)".toUri()
        }
    }

    /** Représentation "lat,lng" ou adresse encodée, utilisée dans les URLs Google Maps. */
    private fun destinationQueryParam(destination: NavigationDestination): String =
        if (destination.latitude != null && destination.longitude != null) {
            "${destination.latitude},${destination.longitude}"
        } else {
            Uri.encode(destination.address ?: destination.label)
        }

    private fun buildSearchUrl(destination: NavigationDestination): String =
        "https://www.google.com/maps/search/?api=1&query=${destinationQueryParam(destination)}"

    private fun buildDirectionsUrl(
        destination: NavigationDestination,
        travelMode: TravelMode,
        origin: NavigationDestination?,
        navigate: Boolean,
    ): String {
        val builder = StringBuilder("https://www.google.com/maps/dir/?api=1")
        builder.append("&destination=").append(destinationQueryParam(destination))
        origin?.let { builder.append("&origin=").append(destinationQueryParam(it)) }
        builder.append("&travelmode=").append(travelMode.apiValue)
        if (navigate) builder.append("&dir_action=navigate")
        return builder.toString()
    }
}