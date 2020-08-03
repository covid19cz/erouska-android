package cz.covid19cz.erouska.ui.sandbox

import androidx.lifecycle.MutableLiveData

class SandboxConfigValues(val title: String, val size : Int) {

    val stringValues = Array<MutableLiveData<String>>(size) { MutableLiveData() }

    fun setValues(newValues : List<Int>){
        if (newValues.size == stringValues.size){
            for (i in newValues.indices){
                setValue(i, newValues[i])
            }
        }
    }

    fun setValue(index : Int, value : Int){
        stringValues[index].value = value.toString()
    }

    fun getIntValues() : List<Int>{
        return stringValues.map { it.value?.toIntOrNull() ?: 0 }
    }

    fun getIntValue(index : Int) : Int{
        return stringValues[0].value?.toIntOrNull() ?: 0
    }
}