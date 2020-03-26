package cz.covid19cz.app.ui.mydata

import android.content.Context
import android.view.View
import androidx.viewpager.widget.PagerAdapter
import cz.covid19cz.app.R
import java.lang.IndexOutOfBoundsException

internal class MyDataPagerAdapter(val context: Context) : PagerAdapter() {

    override fun instantiateItem(view: View, position: Int): Any {
        val resId = when (position) {
            0 -> R.id.pageCritical
            1 -> R.id.pageAll
            else -> throw IndexOutOfBoundsException()
        }
        return view.findViewById(resId)
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> context.getString(R.string.my_data_tab_critical_title)
            1 -> context.getString(R.string.my_data_tab_all_title)
            else -> throw IndexOutOfBoundsException()
        }
    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1 as View
    }
}