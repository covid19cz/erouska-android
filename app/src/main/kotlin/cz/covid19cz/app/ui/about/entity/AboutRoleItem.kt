package cz.covid19cz.app.ui.about.entity

import androidx.databinding.ObservableArrayList

class AboutRoleItem(val title : Int) {

    val items = ObservableArrayList<AboutProfileItem>()

    fun add(item : AboutProfileItem){
        items.add(item)
    }
}