package cz.covid19cz.erouska.exposurenotifications

import android.util.Base64
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import cz.covid19cz.erouska.utils.L
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.random.Random

class ExposureCryptoTools {

    private fun hashedKeys(keys : List<TemporaryExposureKey>) : String? {
        val cleartextSegments = ArrayList<String>()
        for (k in keys) {
            cleartextSegments.add(String.format(
                Locale.ENGLISH,
                "%s.%d.%d",
                Base64.encodeToString(k.keyData, Base64.DEFAULT),
                k.rollingStartIntervalNumber,
                k.rollingPeriod))
        }
        val cleartext = cleartextSegments.joinToString(",")
        L.d( "${keys.size} keys for hashing prior to verification: [" + cleartext + "]")
        try {
            val mac = Mac.getInstance("HmacSHA256");
            mac.init(SecretKeySpec(Base64.decode(newHmacKey(), Base64.DEFAULT), "HmacSHA256"))
            return Base64.encodeToString(mac.doFinal(cleartext.toByteArray(StandardCharsets.UTF_8)), Base64.DEFAULT)
        } catch (t : Throwable) {
            L.e(t)
        }
        return null
    }

    fun newHmacKey(): String? {
        val bytes = ByteArray(16)
        Random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }
}