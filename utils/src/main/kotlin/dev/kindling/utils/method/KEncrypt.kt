package dev.kindling.utils.method

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

// ─────────────────────────────────────────────────────────────────────────────
//  Provider registration (call once at app startup)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Registers the Bouncy Castle providers required for classical and
 * post-quantum cryptography.
 *
 * Call this once in your `Application.onCreate()`:
 * ```kotlin
 * KEncryptProviders.register()
 * ```
 */
object KEncryptProviders {
    fun register() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
        if (Security.getProvider("BCPQC") == null) {
            Security.addProvider(BouncyCastlePQCProvider())
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Algorithm selectors
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Hash algorithm used to derive the AES key from a passphrase.
 * Only relevant for [KEncryptKeyType.Classical].
 */
enum class KHashAlgorithm(internal val jcaName: String, internal val keyBytes: Int) {
    /** SHA-256 → 256-bit AES key */
    SHA256("SHA-256", 32),

    /** SHA-1 → truncated to 128-bit AES key (legacy use only) */
    SHA1("SHA-1", 16)
}

/**
 * Selects between a classical passphrase-derived key and a post-quantum
 * asymmetric key pair (CRYSTALS-Kyber via Bouncy Castle).
 */
sealed class KEncryptKeyType {
    /**
     * Classical symmetric encryption.
     *
     * The passphrase (+ optional salt) is hashed with [hash] to derive an
     * AES-GCM key.  The [hash] determines the AES key size:
     * - [KHashAlgorithm.SHA256] → AES-256
     * - [KHashAlgorithm.SHA1]   → AES-128
     *
     * @param passphrase  Secret used to derive the AES key.
     * @param hash        Hashing algorithm for key derivation. Default: SHA-256.
     */
    data class Classical(
        val passphrase: String,
        val hash: KHashAlgorithm = KHashAlgorithm.SHA256
    ) : KEncryptKeyType()

    /**
     * Post-quantum asymmetric encryption (CRYSTALS-Kyber / Kyber-1024).
     *
     * Encryption uses the **public key**; decryption uses the **private key**.
     * Keys are stored as Base64 strings for easy serialisation.
     *
     * Generate a pair with [KEncryptKeyPair.generate], then persist both keys
     * securely (e.g. Android Keystore for the private key).
     *
     * @param publicKeyBase64  Recipient public key (Base64-encoded).
     * @param privateKeyBase64 Recipient private key (Base64-encoded). Required for decryption.
     */
    data class PostQuantum(
        val publicKeyBase64: String,
        val privateKeyBase64: String? = null
    ) : KEncryptKeyType()
}

// ─────────────────────────────────────────────────────────────────────────────
//  Post-quantum key pair helper
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A Kyber-1024 key pair, both keys Base64-encoded for serialisation.
 *
 * ```kotlin
 * val pair = KEncryptKeyPair.generate()
 * // Store pair.privateKeyBase64 securely (Android Keystore recommended)
 * // Distribute pair.publicKeyBase64 freely
 * ```
 */
data class KEncryptKeyPair(
    val publicKeyBase64: String,
    val privateKeyBase64: String
) {
    companion object {
        /**
         * Generates a fresh CRYSTALS-Kyber-1024 key pair.
         *
         * Requires [KEncryptProviders.register] to have been called first.
         */
        fun generate(): KEncryptKeyPair {
            val kpg = KeyPairGenerator.getInstance("Kyber", "BCPQC")
            kpg.initialize(KyberParameterSpec.kyber1024)
            val pair = kpg.generateKeyPair()
            return KEncryptKeyPair(
                publicKeyBase64  = pair.public.encoded.toBase64(),
                privateKeyBase64 = pair.private.encoded.toBase64()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Core encrypt / decrypt
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Encrypts a [String] and returns a Base64-encoded ciphertext.
 *
 * ### Classical (AES-GCM) — no salt
 * ```kotlin
 * val cipher = "Hello Kindling!".encrypt(
 *     key = KEncryptKeyType.Classical("my-secret")
 * )
 * ```
 *
 * ### Classical with salt
 * ```kotlin
 * val cipher = "Hello Kindling!".encrypt(
 *     key  = KEncryptKeyType.Classical("my-secret", KHashAlgorithm.SHA256),
 *     salt = "random-salt"
 * )
 * ```
 *
 * ### Post-quantum (Kyber-1024)
 * ```kotlin
 * val pair   = KEncryptKeyPair.generate()
 * val cipher = "Hello Kindling!".encrypt(
 *     key = KEncryptKeyType.PostQuantum(publicKeyBase64 = pair.publicKeyBase64)
 * )
 * ```
 *
 * @param key  The key type and material to use.
 * @param salt Optional salt string mixed into the key derivation (classical only).
 * @return Base64-encoded ciphertext (includes IV/nonce prepended).
 */
fun String.encrypt(key: KEncryptKeyType, salt: String? = null): String =
    toByteArray(Charsets.UTF_8).encrypt(key, salt).toBase64()

/**
 * Decrypts a Base64-encoded ciphertext produced by [encrypt].
 *
 * ### Classical
 * ```kotlin
 * val plain = cipherText.decrypt(
 *     key  = KEncryptKeyType.Classical("my-secret"),
 *     salt = "random-salt"   // must match the salt used during encryption
 * )
 * ```
 *
 * ### Post-quantum
 * ```kotlin
 * val plain = cipherText.decrypt(
 *     key = KEncryptKeyType.PostQuantum(
 *         publicKeyBase64  = pair.publicKeyBase64,
 *         privateKeyBase64 = pair.privateKeyBase64
 *     )
 * )
 * ```
 *
 * @param key  The key type and material to use.
 * @param salt Optional salt; must match the value used during [encrypt].
 * @return Decrypted plaintext string.
 * @throws KEncryptException if decryption fails (wrong key, wrong salt, corrupted data…).
 */
fun String.decrypt(key: KEncryptKeyType, salt: String? = null): String =
    fromBase64().decrypt(key, salt).toString(Charsets.UTF_8)

// ─────────────────────────────────────────────────────────────────────────────
//  ByteArray-level encrypt / decrypt (advanced / internal)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Low-level [ByteArray] encryption. Returns raw ciphertext bytes.
 *
 * Prefer the [String.encrypt] extension for everyday use.
 */
fun ByteArray.encrypt(key: KEncryptKeyType, salt: String? = null): ByteArray =
    when (key) {
        is KEncryptKeyType.Classical  -> encryptClassical(this, key, salt)
        is KEncryptKeyType.PostQuantum -> encryptPostQuantum(this, key)
    }

/**
 * Low-level [ByteArray] decryption. Returns raw plaintext bytes.
 *
 * Prefer the [String.decrypt] extension for everyday use.
 */
fun ByteArray.decrypt(key: KEncryptKeyType, salt: String? = null): ByteArray =
    when (key) {
        is KEncryptKeyType.Classical   -> decryptClassical(this, key, salt)
        is KEncryptKeyType.PostQuantum -> decryptPostQuantum(this, key)
    }

// ─────────────────────────────────────────────────────────────────────────────
//  Classical (AES-GCM) implementation
// ─────────────────────────────────────────────────────────────────────────────

// Wire format:  [ 12-byte IV | ciphertext + 16-byte GCM auth tag ]

private const val GCM_IV_LENGTH  = 12
private const val GCM_TAG_LENGTH = 128 // bits

private fun encryptClassical(
    plaintext : ByteArray,
    key       : KEncryptKeyType.Classical,
    salt      : String?
): ByteArray {
    val secretKey = deriveAesKey(key.passphrase, key.hash, salt)
    val iv        = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
    val cipher    = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
    val ciphertext = cipher.doFinal(plaintext)
    return iv + ciphertext
}

private fun decryptClassical(
    data : ByteArray,
    key  : KEncryptKeyType.Classical,
    salt : String?
): ByteArray = runCatching {
    val secretKey  = deriveAesKey(key.passphrase, key.hash, salt)
    val iv         = data.sliceArray(0 until GCM_IV_LENGTH)
    val ciphertext = data.sliceArray(GCM_IV_LENGTH until data.size)
    val cipher     = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
    cipher.doFinal(ciphertext)
}.getOrElse { throw KEncryptException("Classical decryption failed", it) }

/** Derives an AES [SecretKey] from [passphrase] + optional [salt] via [hash]. */
private fun deriveAesKey(
    passphrase : String,
    hash       : KHashAlgorithm,
    salt       : String?
): SecretKey {
    val input    = if (salt != null) passphrase + salt else passphrase
    val digest   = MessageDigest.getInstance(hash.jcaName)
    val keyBytes = digest.digest(input.toByteArray(Charsets.UTF_8)).copyOf(hash.keyBytes)
    return SecretKeySpec(keyBytes, "AES")
}

// ─────────────────────────────────────────────────────────────────────────────
//  Post-quantum (Kyber → AES-GCM hybrid) implementation
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Kyber is a KEM (Key Encapsulation Mechanism), not a direct cipher.
 * We use a standard hybrid approach:
 *   1. Generate a fresh AES-256 session key.
 *   2. Encapsulate it with Kyber (produces an "encapsulated key" blob).
 *   3. Encrypt the payload with AES-GCM using the session key.
 *
 * Wire format:
 *   [ 4-byte encapsulated-key length (big-endian int)
 *   | encapsulated-key bytes
 *   | 12-byte AES-GCM IV
 *   | AES-GCM ciphertext + 16-byte tag ]
 */
private fun encryptPostQuantum(
    plaintext : ByteArray,
    key       : KEncryptKeyType.PostQuantum
): ByteArray = runCatching {
    val publicKey = loadKyberPublicKey(key.publicKeyBase64)

    // 1. Generate ephemeral AES-256 key
    val aesKeyGen = KeyGenerator.getInstance("AES")
    aesKeyGen.init(256)
    val sessionKey: SecretKey = aesKeyGen.generateKey()

    // 2. Encapsulate the session key with Kyber
    val kyberCipher = Cipher.getInstance("CRYSTALS-KYBER", "BCPQC")
    kyberCipher.init(Cipher.WRAP_MODE, publicKey)
    val encapsulatedKey: ByteArray = kyberCipher.wrap(sessionKey)

    // 3. Encrypt payload with AES-GCM
    val iv = ByteArray(GCM_IV_LENGTH).also { SecureRandom().nextBytes(it) }
    val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
    aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
    val ciphertext = aesCipher.doFinal(plaintext)

    // 4. Pack: [len(encapsulatedKey) | encapsulatedKey | iv | ciphertext]
    val lenBytes = encapsulatedKey.size.toBytesBigEndian()
    lenBytes + encapsulatedKey + iv + ciphertext
}.getOrElse { throw KEncryptException("Post-quantum encryption failed", it) }

private fun decryptPostQuantum(
    data : ByteArray,
    key  : KEncryptKeyType.PostQuantum
): ByteArray = runCatching {
    val privateKeyB64 = key.privateKeyBase64
        ?: throw KEncryptException("Private key required for post-quantum decryption")

    val privateKey = loadKyberPrivateKey(privateKeyB64)

    // 1. Unpack
    val encKeyLen      = data.sliceArray(0..3).fromBytesBigEndian()
    val encapsulatedKey = data.sliceArray(4 until 4 + encKeyLen)
    val iv             = data.sliceArray(4 + encKeyLen until 4 + encKeyLen + GCM_IV_LENGTH)
    val ciphertext     = data.sliceArray(4 + encKeyLen + GCM_IV_LENGTH until data.size)

    // 2. Unwrap session key with Kyber
    val kyberCipher = Cipher.getInstance("CRYSTALS-KYBER", "BCPQC")
    kyberCipher.init(Cipher.UNWRAP_MODE, privateKey)
    val sessionKey = kyberCipher.unwrap(encapsulatedKey, "AES", Cipher.SECRET_KEY)

    // 3. Decrypt with AES-GCM
    val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
    aesCipher.init(Cipher.DECRYPT_MODE, sessionKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
    aesCipher.doFinal(ciphertext)
}.getOrElse { throw KEncryptException("Post-quantum decryption failed", it) }

private fun loadKyberPublicKey(base64: String): PublicKey {
    val keyBytes = base64.fromBase64()
    val spec     = X509EncodedKeySpec(keyBytes)
    return KeyFactory.getInstance("Kyber", "BCPQC").generatePublic(spec)
}

private fun loadKyberPrivateKey(base64: String): PrivateKey {
    val keyBytes = base64.fromBase64()
    val spec     = PKCS8EncodedKeySpec(keyBytes)
    return KeyFactory.getInstance("Kyber", "BCPQC").generatePrivate(spec)
}

// ─────────────────────────────────────────────────────────────────────────────
//  Exception
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Thrown when encryption or decryption fails.
 *
 * Common causes: wrong passphrase, mismatched salt, corrupted ciphertext,
 * wrong key pair, or missing provider registration.
 */
class KEncryptException(message: String, cause: Throwable? = null) :
    Exception(message, cause)