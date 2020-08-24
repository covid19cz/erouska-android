package cz.covid19cz.erouska.ui.sandbox

import androidx.lifecycle.MutableLiveData

class SandboxConfigValues(val title: String, val size : Int) {

    val stringValues = Array<MutableLiveData<String>>(size) { MutableLiveData() }

    fun setDoubleValues(newValues : List<Double>){
        if (newValues.size == stringValues.size){
            for (i in newValues.indices){
                setDoubleValue(i, newValues[i])
            }
        }
    }

    fun setIntValues(newValues : List<Int>){
        if (newValues.size == stringValues.size){
            for (i in newValues.indices){
                setIntValue(i, newValues[i])
            }
        }
    }

    fun setDoubleValue(index : Int, value : Double){
        stringValues[index].value = value.toString()
    }

    fun setIntValue(index : Int, value : Int){
        stringValues[index].value = value.toString()
    }

    fun getIntValues() : List<Int>{
        return stringValues.map { it.value?.toIntOrNull() ?: 0 }
    }

    fun getIntValue(index : Int) : Int{
        return stringValues[0].value?.toIntOrNull() ?: 0
    }

    fun getDoubleValues() : List<Double>{
        return stringValues.map { it.value?.toDoubleOrNull() ?: 0.0 }
    }

    fun getDoubleValue(index : Int) : Double{
        return stringValues[0].value?.toDoubleOrNull() ?: 0.0
    }

    fun setValues(values : String){
        values.split(";").forEachIndexed { i, s ->
            if (i < stringValues.size) {
                stringValues[i].value = s
            }
        }
    }

    fun joinToString() : String{
        return stringValues.map { it.value }.joinToString(";")
    }
}