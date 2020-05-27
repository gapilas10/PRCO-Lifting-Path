package com.example.android.liftingpath

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var homeFragment: HomeFragment
    lateinit var cameraFragment: CameraFragment
    lateinit var videoFragment: VideoFragment
    lateinit var settingFragment: SettingFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

            setSupportActionBar(toolBar)
            val actionBar = supportActionBar
            actionBar?.title = "Lifting Path"

            val drawerToggle:ActionBarDrawerToggle = object : ActionBarDrawerToggle (
                this,
                drawerLayout,
                toolBar,
                (R.string.open),
                (R.string.close)
            ) {

            }

            drawerToggle.isDrawerIndicatorEnabled = true
            drawerLayout.addDrawerListener(drawerToggle)
            drawerToggle.syncState()

            nav_view.setNavigationItemSelectedListener(this)
            // set default fragment to be home fragment
            homeFragment = HomeFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout,homeFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()

    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId)
        {
            R.id.home -> {
                homeFragment = HomeFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout,homeFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
            R.id.camera -> {
                cameraFragment = CameraFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout,cameraFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
            R.id.video -> {
                videoFragment = VideoFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout,videoFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
            R.id.setting -> {
                settingFragment = SettingFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout,settingFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) //If menu open
        {
            drawerLayout.closeDrawer(GravityCompat.START) //Close menu
        }
        else //Else do normal "back" action
        {
            super.onBackPressed()
        }
    }



}