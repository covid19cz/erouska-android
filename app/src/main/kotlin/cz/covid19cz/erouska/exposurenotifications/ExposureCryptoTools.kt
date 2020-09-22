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
        val cleartextSegments = ArrayList<String>()
        for (k in keys) {
            cleartextSegments.add(
                String.format(
                    Locale.ENGLISH,
                    "%s.%d.%d",
                    k.keyData.encodeBase64(),
                    k.rollingStartIntervalNumber,
                    k.rollingPeriod
                )
            )
        }
        val cleartext = cleartextSegments.joinToString(",")
        L.d("${keys.size} keys for hashing prior to verification: [" + cleartext + "]")
        val mac = Mac.getInstance("HmacSHA256");
        mac.init(SecretKeySpec(hmacKey.decodeBase64(), "HmacSHA256"))
        return mac.doFinal(cleartext.toByteArray(StandardCharsets.UTF_8)).encodeBase64()
    }

    fun newHmacKey(): String {
        val bytes = ByteArray(16)
        Random.nextBytes(bytes)
        return bytes.encodeBase64()
    }

    fun ByteArray.encodeBase64() : String{
        return Base64.encodeToString(this, Base64.NO_WRAP)
    }

    fun String.decodeBase64() : ByteArray{
        return Base64.decode(this, Base64.NO_WRAP)
    }
}