package com.example.joke_app

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.joke_app.Fragments.FavoriteFragment
import com.example.joke_app.Fragments.Get_Joke_Fragment
import com.example.joke_app.databinding.ActivityMainBinding
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {


    lateinit var progressBar: ProgressBar

    lateinit var viewPager: ViewPager
    lateinit var tabLayout: TabLayout

    lateinit var get_joke_fragment: Get_Joke_Fragment
    lateinit var fav_joke_fragment: FavoriteFragment
    lateinit var binding:ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)

        progressBar =binding.progressBar
        setup_tablayout()
    }


    fun showProgressBar(visible: Boolean) {
        progressBar.setVisibility(if (visible) View.VISIBLE else View.GONE)
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