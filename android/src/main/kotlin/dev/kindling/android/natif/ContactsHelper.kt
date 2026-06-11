package dev.kindling.android.natif

import android.Manifest
import android.content.ContentProviderOperation
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.fragment.app.FragmentActivity
import androidx.core.net.toUri

// ─────────────────────────────────────────────
//  ContactInfo
// ─────────────────────────────────────────────

/**
 * Informations d'un contact.
 *
 * @param id          Identifiant ContactsContract.
 * @param displayName Nom affiché.
 * @param phones      Liste des numéros de téléphone.
 * @param emails      Liste des adresses email.
 * @param photoUri    URI de la photo, ou `null`.
 */
data class ContactInfo(
    val id: String,
    val displayName: String,
    val phones: List<String>  = emptyList(),
    val emails: List<String>  = emptyList(),
    val photoUri: Uri?        = null
)

// ─────────────────────────────────────────────
//  ContactQuery
// ─────────────────────────────────────────────

/**
 * Décrit une requête de recherche de contacts.
 *
 * Presets :
 * - [ContactQuery.All]           → tous les contacts
 * - [ContactQuery.withPhone]     → contacts avec numéro uniquement
 */
data class ContactQuery(
    val nameFilter: String?   = null,
    val requirePhone: Boolean = false,
    val limit: Int            = Int.MAX_VALUE
) {
    companion object {
        val All        = ContactQuery()
        fun withPhone() = ContactQuery(requirePhone = true)
        fun search(name: String) = ContactQuery(nameFilter = name)
    }
}

// ─────────────────────────────────────────────
//  ContactsHelper
// ─────────────────────────────────────────────

/**
 * Helper de contacts centralisé.
 *
 * Permissions requises :
 * - `READ_CONTACTS`  → lecture
 * - `WRITE_CONTACTS` → création / suppression
 *
 * Enregistrement Koin :
 * ```kotlin
 * single { ContactsHelper(androidContext()) }
 * ```
 *
 * Utilisation :
 * ```kotlin
 * // Lire tous les contacts
 * val contacts = contactsHelper.query(ContactQuery.All)
 *
 * // Chercher par nom
 * val results = contactsHelper.query(ContactQuery.search("Alice"))
 *
 * // Sélecteur natif (pas de permission requise)
 * val launcher = contactsHelper.registerPickerLauncher(activity) { uri ->
 *     uri?.let { contactsHelper.resolveContact(context, it) }
 * }
 * contactsHelper.openPicker(launcher)
 * ```
 */
class ContactsHelper(context: Context) {

    internal val appContext = context.applicationContext

    // ── Query ─────────────────────────────────────────────────────────────────

    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun query(query: ContactQuery = ContactQuery.All): List<ContactInfo> {
        val selection = mutableListOf<String>()
        val args      = mutableListOf<String>()

        query.nameFilter?.let {
            selection.add("${ContactsContract.Contacts.DISPLAY_NAME} LIKE ?")
            args.add("%$it%")
        }
        if (query.requirePhone) {
            selection.add("${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1")
        }

        val cursor: Cursor? = appContext.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.PHOTO_URI
            ),
            if (selection.isEmpty()) null else selection.joinToString(" AND "),
            if (args.isEmpty()) null else args.toTypedArray(),
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
        )

        val contacts = mutableListOf<ContactInfo>()
        cursor?.use { c ->
            while (c.moveToNext() && contacts.size < query.limit) {
                val id    = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name  = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)) ?: ""
                val photo = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))

                contacts.add(ContactInfo(
                    id          = id,
                    displayName = name,
                    phones      = queryPhones(id),
                    emails      = queryEmails(id),
                    photoUri    = photo?.toUri()
                ))
            }
        }
        return contacts
    }

    /** Résout un [Uri] retourné par le sélecteur natif en [ContactInfo]. */
    @RequiresPermission(Manifest.permission.READ_CONTACTS)
    fun resolveContact(context: Context, uri: Uri): ContactInfo? {
        val cursor = context.contentResolver.query(uri,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
            null, null, null
        ) ?: return null

        return cursor.use { c ->
            if (!c.moveToFirst()) return null
            val id   = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)) ?: ""
            ContactInfo(id = id, displayName = name, phones = queryPhones(id), emails = queryEmails(id))
        }
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Crée un contact minimal (nom + téléphone).
     * Requiert `WRITE_CONTACTS`.
     */
    @RequiresPermission(Manifest.permission.WRITE_CONTACTS)
    fun createContact(displayName: String, phone: String? = null, email: String? = null) {
        val ops = ArrayList<ContentProviderOperation>()

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build())

        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
            .build())

        phone?.let {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, it)
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build())
        }

        email?.let {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, it)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                .build())
        }

        appContext.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
    }

    // ── SAF picker ────────────────────────────────────────────────────────────

    /** Enregistre un launcher pour le sélecteur de contact natif. */
    fun registerPickerLauncher(
        activity: FragmentActivity,
        onResult: (Uri?) -> Unit
    ): ActivityResultLauncher<Void?> =
        activity.registerForActivityResult(
            ActivityResultContracts.PickContact(), onResult
        )

    fun openPicker(launcher: ActivityResultLauncher<Void?>) = launcher.launch(null)

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun queryPhones(contactId: String): List<String> {
        val phones = mutableListOf<String>()
        appContext.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId), null
        )?.use { c ->
            while (c.moveToNext())
                phones.add(c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)))
        }
        return phones
    }

    private fun queryEmails(contactId: String): List<String> {
        val emails = mutableListOf<String>()
        appContext.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId), null
        )?.use { c ->
            while (c.moveToNext())
                emails.add(c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)))
        }
        return emails
    }
}