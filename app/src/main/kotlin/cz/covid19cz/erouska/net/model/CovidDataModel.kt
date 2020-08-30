package cz.covid19cz.erouska.net.model

data class CovidStatsRequest(
    val data: CovidStatsDto
)

data class CovidStatsDto(
    val date: String?
)

data class Data<T>(
    var data: T? = null
)

data class CovidStatsResponse(
    val date: String?,
    val testsTotal: Int?,
    val testsIncrease: Int?,
    val confirmedCasesTotal: Int?,
    val confirmedCasesIncrease: Int?,
    val activeCasesTotal: Int?,
    val activeCasesIncrease: Int?,
    val curedTotal: Int?,
    val curedIncrease: Int?,
    val deceasedTotal: Int?,
    val deceasedIncrease: Int?,
    val currentlyHospitalizedTotal: Int?,
    val currentlyHospitalizedIncrease: Int?
)