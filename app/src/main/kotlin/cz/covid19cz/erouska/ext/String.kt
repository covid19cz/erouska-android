package cz.covid19cz.erouska.ext

fun String.toIntList() : List<Int>{
    return this.split(",").map { it.toInt() }
}