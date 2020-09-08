package cz.covid19cz.erouska.net.model

import com.google.gson.annotations.SerializedName

data class CovidStatsRequest(
    val data: CovidStatsDto
)

data class CovidStatsDto(
    val date: String?
)

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