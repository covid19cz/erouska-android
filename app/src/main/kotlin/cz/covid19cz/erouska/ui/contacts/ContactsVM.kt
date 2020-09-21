package cz.covid19cz.erouska.ui.contacts

import androidx.databinding.ObservableArrayList
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.ui.base.BaseVM
import cz.covid19cz.erouska.ui.contacts.event.ContactsEvent
import java.lang.reflect.Type

class ContactsVM @ViewModelInject constructor() : BaseVM() {

    val state = MutableLiveData<ContactsEvent>()
    val items = ObservableArrayList<Contact>()

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {

        val contactsListType: Type = object : TypeToken<ArrayList<Contact>?>() {}.type

        val contacts: ArrayList<Contact> = Gson().fromJson(AppConfig.contactsContentJson, contactsListType)

        items.clear()
        items.addAll(contacts)

    }

    fun onLinkClick(link: String) {
        state.value = ContactsEvent.ContactLinkClicked(link)
    }

}