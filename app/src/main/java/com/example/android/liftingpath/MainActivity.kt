package com.example.android.liftingpath

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.Switch
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    //Used to enable permissions
    var REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    var SPLASH_TIME_OUT:Long = 2000
    var TAG = "tag"

    lateinit var homeFragment: HomeFragment
    lateinit var cameraFragment: CameraFragment
    lateinit var schoolFragment: SchoolFragment
    lateinit var timelineFragment: TimelineFragment
    lateinit var settingFragment: SettingFragment
    lateinit var logoutFragment: LogoutFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(!checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
            Handler().postDelayed({
                // create an intent that will stat the blank activity
                val mainIntent = Intent(this, WelcomeActivity::class.java)
                startActivity(mainIntent)
                finish()
            }, SPLASH_TIME_OUT)
        }

            setSupportActionBar(toolBar)
            val actionBar = supportActionBar
            actionBar?.title = "Navigation Drawer"

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
            R.id.school -> {
                schoolFragment = SchoolFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout,schoolFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
            R.id.timeline -> {
                timelineFragment = TimelineFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout,timelineFragment)
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
            R.id.logout -> {
                logoutFragment = LogoutFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout,logoutFragment)
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

    private fun checkAndRequestPermissions(): Boolean { // Here we check for each individual permission
        val CameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val ReadPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val WritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val ListOfMissingPermissions = ArrayList<String>()
        // We store the missing permissions to check again latear
        if (CameraPermission != PackageManager.PERMISSION_GRANTED)
        {
            ListOfMissingPermissions.add(Manifest.permission.CAMERA)
        }

        if (ReadPermission != PackageManager.PERMISSION_GRANTED)
        {
            ListOfMissingPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (WritePermission != PackageManager.PERMISSION_GRANTED)
        {
            ListOfMissingPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        // Promp the user for permissions again
        if (!ListOfMissingPermissions.isEmpty())
        {
            ActivityCompat.requestPermissions(this,
                ListOfMissingPermissions.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false;
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "Permission callback called-------")
        when (requestCode)
        {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {
                val perms = HashMap<String,Int>()
                //Initialize the map with both permissions
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for all permissions
                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG, "Camera, read and write permissions granted")
                        // Proceed normal flow
                        val mainIntent = Intent(this, MainActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    } else {
                        //else any one or more permissions are not granted
                        Log.d(TAG, "Some permissions are missing, prompt again")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                            showDialogOK("Service Permissions are required for this app",
                                DialogInterface.OnClickListener { dialog, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                        DialogInterface.BUTTON_NEGATIVE ->
                                            // proceed with logic by disabling the related features or quit the app.
                                            finish()
                                    }
                                })
                        } else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }

    }

private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
    AlertDialog.Builder(this)
        .setMessage(message)
        .setPositiveButton("OK", okListener)
        .setNegativeButton("Cancel", okListener)
        .create()
        .show()
}

    private fun explain(msg: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(msg)
            .setPositiveButton("Yes") { paramDialogInterface, paramInt ->
                //  permissionsclass.requestPermission(type,code);
                val settings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package",packageName,null)
                settings.setData(uri)
                startActivity(settings)
            }
            .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> finish() }
        dialog.show()
    }

    companion object {

        val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
        private val SPLASH_TIME_OUT = 2000
    }


}