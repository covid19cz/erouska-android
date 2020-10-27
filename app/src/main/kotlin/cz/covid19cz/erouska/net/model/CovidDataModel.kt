package cz.covid19cz.erouska.net.model

import com.google.gson.annotations.SerializedName

data class CovidStatsResponse(
    @SerializedName("date") val date: String?,
    @SerializedName("testsTotal") val testsTotal: Int?,
    @SerializedName("testsIncrease") val testsIncrease: Int?,
    @SerializedName("confirmedCasesTotal") val confirmedCasesTotal: Int?,
    @SerializedName("confirmedCasesIncrease") val confirmedCasesIncrease: Int?,
    @SerializedName("activeCasesTotal") val activeCasesTotal: Int?,
    @SerializedName("activeCasesIncrease") val activeCasesIncrease: Int?,
    @SerializedName("curedTotal") val curedTotal: Int?,
    @SerializedName("curedIncrease") val curedIncrease: Int?,
    @SerializedName("deceasedTotal") val deceasedTotal: Int?,
    @SerializedName("deceasedIncrease") val deceasedIncrease: Int?,
    @SerializedName("currentlyHospitalizedTotal") val currentlyHospitalizedTotal: Int?,
    @SerializedName("currentlyHospitalizedIncrease") val currentlyHospitalizedIncrease: Int?
)

data class DownloadMetricsResponse(
    @SerializedName("modified") val modified: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("activations_yesterday") val activationsYesterday: Int?,
    @SerializedName("activations_total") val activationsTotal: Int?,
    @SerializedName("key_publishers_yesterday") val keyPublishersYesterday: Int?,
    @SerializedName("key_publishers_total") val keyPublishersTotal: Int?,
    @SerializedName("notifications_yesterday") val notificationsYesterday: Int?,
    @SerializedName("notifications_total") val notificationsTotal: Int?
)