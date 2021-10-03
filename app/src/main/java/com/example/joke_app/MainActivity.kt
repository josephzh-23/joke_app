package com.example.joke_app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.joke_app.Fragments.FavoriteFragment
import com.example.joke_app.Fragments.Get_Joke_Fragment
import com.example.joke_app.databinding.ActivityMainBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.tabs.TabLayout
import com.google.gson.GsonBuilder
import com.ledsmart.grow3.Syncing.Modes_RecyclerAdapter
import com.smartherd.globofly.services.Django_Service
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {


    lateinit var progressBar: ProgressBar

    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

    lateinit var get_joke_fragment: Get_Joke_Fragment
    lateinit var fav_joke_fragment: FavoriteFragment
    lateinit var binding:ActivityMainBinding
    /*
     A singleton-patterned background thread-pool used to handle
     background tasks
     */

    lateinit var viewModel: View_Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)


        progressBar =binding.progressBar


        setup_tablayout()
    }


    fun showProgressBar(visible: Boolean) {
        progressBar.setVisibility(if (visible) View.VISIBLE else View.INVISIBLE)
    }


    private fun setup_tablayout() {
        viewPager = findViewById<ViewPager>(R.id.view_pager)
        tabLayout = findViewById<TabLayout>(R.id.tab_layout)

       fav_joke_fragment = FavoriteFragment()
        get_joke_fragment = Get_Joke_Fragment()



        tabLayout.setupWithViewPager(viewPager)

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, 0)
        viewPagerAdapter.addFragment( get_joke_fragment, "Get Joke")
        viewPagerAdapter.addFragment(fav_joke_fragment, "Favorite Jokes")
        viewPager.setAdapter(viewPagerAdapter)

        tabLayout.getTabAt(0)!!.setIcon(R.drawable.main)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.favorite)

        val badgeDrawable: BadgeDrawable = tabLayout.getTabAt(0)!!.getOrCreateBadge()
        badgeDrawable.isVisible = true
    }


    private class ViewPagerAdapter(fm: FragmentManager, behavior: Int) :
        FragmentPagerAdapter(fm, behavior) {
        private val fragments: MutableList<Fragment> = java.util.ArrayList()
        private val fragmentTitle: MutableList<String> = java.util.ArrayList()
        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            fragmentTitle.add(title)
        }

        override fun getItem(position: Int): Fragment {
            Log.i(TAG, "getItem: position $position")
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return fragmentTitle[position]
        }
    }



}