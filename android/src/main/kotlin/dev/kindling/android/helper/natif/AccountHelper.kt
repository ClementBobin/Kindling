package dev.kindling.android.helper.natif

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import androidx.annotation.RequiresPermission

// ─────────────────────────────────────────────
//  AccountConfig
// ─────────────────────────────────────────────

/**
 * Décrit un type de compte à interroger ou créer.
 *
 * Presets :
 * - [AccountConfig.Google] → comptes Google
 *
 * Personnalisé :
 * ```kotlin
 * val config = AccountConfig(accountType = "com.example.app")
 * accountHelper.getAccounts(config)
 * ```
 */
data class AccountConfig(val accountType: String) {
    companion object {
        val Google = AccountConfig("com.google")
    }
}

// ─────────────────────────────────────────────
//  AccountHelper
// ─────────────────────────────────────────────

/**
 * Helper de gestion de comptes système centralisé.
 *
 * Nécessite `GET_ACCOUNTS` dans le manifest (normal permission sur API < 26,
 * accordée automatiquement ; signature/système sur API ≥ 26 pour les comptes tiers).
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { AccountHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * val accounts = accountHelper.getAccounts(AccountConfig.Google)
 * val primary  = accountHelper.getPrimaryAccount(AccountConfig.Google)
 * val token    = accountHelper.peekAuthToken(account, "oauth2:profile")
 * ```
 */
class AccountHelper(context: Context) {

    internal val appContext      = context.applicationContext
    internal val accountManager  = AccountManager.get(appContext)

    // ── Query ─────────────────────────────────────────────────────────────────

    /**
     * Retourne tous les comptes du [config.accountType] accessibles.
     * Retourne une liste vide si la permission est absente ou refusée.
     */
    @RequiresPermission(Manifest.permission.GET_ACCOUNTS)
    fun getAccounts(config: AccountConfig): List<Account> =
        try {
            accountManager.getAccountsByType(config.accountType).toList()
        } catch (_: SecurityException) {
            emptyList()
        }

    /**
     * Retourne le premier compte trouvé, ou `null`.
     */
    @RequiresPermission(Manifest.permission.GET_ACCOUNTS)
    fun getPrimaryAccount(config: AccountConfig): Account? =
        getAccounts(config).firstOrNull()

    /**
     * Lit un token d'auth en cache (sans déclencher de flow d'authentification).
     * Retourne `null` si aucun token n'est mis en cache.
     */
    @RequiresPermission("android.permission.AUTHENTICATE_ACCOUNTS")
    fun peekAuthToken(account: Account, authTokenType: String): String? =
        accountManager.peekAuthToken(account, authTokenType)

    /** Invalide un token en cache (forcer un refresh au prochain accès). */
    @RequiresPermission(anyOf = ["android.permission.USE_CREDENTIALS", "android.permission.MANAGE_ACCOUNTS"])
    fun invalidateAuthToken(accountType: String, token: String) {
        accountManager.invalidateAuthToken(accountType, token)
    }

    /** `true` si au moins un compte du [config.accountType] est disponible. */
    @RequiresPermission(Manifest.permission.GET_ACCOUNTS)
    fun hasAccount(config: AccountConfig): Boolean =
        getAccounts(config).isNotEmpty()
}