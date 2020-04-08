package cz.covid19cz.erouska.crypto

import android.content.Context
import android.security.KeyPairGeneratorSpec
import cz.covid19cz.erouska.db.SharedPrefsRepository
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

private const val ENCRYPTION_ALGORITHM = "RSA/ECB/PKCS1Padding"

// Must be <= 256 bytes
private const val DATABASE_KEY_SIZE = 32

const val DATABASE_KEY_ALIAS = "db-key"

fun getDatabaseEncryptionKey(prefsRepository: SharedPrefsRepository, context: Context): ByteArray? {
    val key = prefsRepository.getDbEncryptedKey()
    val secretKey = getAsymmetricStoreKey(context, DATABASE_KEY_ALIAS)

    if (key == null) {
        val randomBytes = generateRandomBytes()
        val encryptedKey = encryptInStore(randomBytes, secretKey.secretKey) ?: randomBytes
        prefsRepository.setDbEncryptedKey(encodeBytes(encryptedKey))
        return randomBytes
    }
    return decryptInStore(decodeBytes(key), secretKey.secretKey)
}

private fun encodeBytes(bytes: ByteArray): String = String(bytes, Charset.forName("UTF-8"))
private fun decodeBytes(string: String): ByteArray = string.toByteArray(Charset.forName("UTF-8"))

/**
 * Create or load an asymmetric secret key from the Android KeyStore.
 */
private fun getAsymmetricStoreKey(context: Context, alias: String): KeyStore.SecretKeyEntry {
    val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    if (!keyStore.containsAlias(alias)) {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        endDate.add(Calendar.YEAR, 25)
        val keyPairGeneratorSpec = KeyPairGeneratorSpec.Builder(context.applicationContext)
            .setAlias(alias)
            .setSubject(X500Principal("CN=$alias"))
            .setSerialNumber(BigInteger.valueOf(123456))
            .setStartDate(startDate.time)
            .setEndDate(endDate.time)
            .build()
        val keyPairGenerator = KeyPairGenerator.getInstance(
            "RSA",
            "AndroidKeyStore"
        )
        keyPairGenerator.initialize(keyPairGeneratorSpec)
        keyPairGenerator.generateKeyPair()
    }
    return keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
}

/**
 * Encrypts data in the KeyStore using the provided key.
 */
private fun encryptInStore(plainText: ByteArray, key: Key): ByteArray? {
    return try {
        val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cipherText = cipher.doFinal(plainText)
        cipherText
    } catch (exception: GeneralSecurityException) {
        exception.printStackTrace()
        null
    }
}

/**
 * Decrypts data in the KeyStore using the provided key.
 */
private fun decryptInStore(cipherText: ByteArray, key: Key): ByteArray? {
    val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
    cipher.init(Cipher.DECRYPT_MODE, key)
    return cipher.doFinal(cipherText)
}

private fun generateRandomBytes(): ByteArray {
    val array = ByteArray(DATABASE_KEY_SIZE)
    SecureRandom().nextBytes(array)
    return array
}
