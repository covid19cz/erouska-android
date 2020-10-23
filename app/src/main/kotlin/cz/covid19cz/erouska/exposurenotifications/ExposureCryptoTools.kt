package cz.covid19cz.erouska.exposurenotifications

import android.util.Base64
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import cz.covid19cz.erouska.utils.L
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList
import kotlin.random.Random

@Singleton
class ExposureCryptoTools @Inject constructor() {

    fun hashedKeys(keys: List<TemporaryExposureKey>, hmacKey: String): String {
        val cleartextSegments = ArrayList<HashedKeyData>()
        for (k in keys) {
            val base64key = k.keyData.encodeBase64()
            cleartextSegments.add(
                HashedKeyData(
                    base64key, String.format(
                        Locale.ENGLISH,
                        "%s.%d.%d",
                        base64key,
                        k.rollingStartIntervalNumber,
                        k.rollingPeriod
                    )
                )
            )
        }
        val cleartext = cleartextSegments.sortedBy { it.base64key }.joinToString(",") { it.data }
        L.i("Hashing ${keys.size} keys")
        val mac = Mac.getInstance("HmacSHA256");
        mac.init(SecretKeySpec(hmacKey.decodeBase64(), "HmacSHA256"))
        return mac.doFinal(cleartext.toByteArray(StandardCharsets.UTF_8)).encodeBase64()
    }

    fun newHmacKey(): String {
        val bytes = ByteArray(16)
        Random.nextBytes(bytes)
        return bytes.encodeBase64()
    }

    private class HashedKeyData(val base64key: String, val data: String)
}

fun ByteArray.encodeBase64(): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

fun String.decodeBase64(): ByteArray {
    return Base64.decode(this, Base64.NO_WRAP)
}