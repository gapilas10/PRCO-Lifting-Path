package com.example.android.liftingpath

import android.app.Activity
import android.app.AlertDialog
import androidx.fragment.app.FragmentActivity

class LoadingDialog {
    lateinit var activity:FragmentActivity
    lateinit var dialog:AlertDialog

    constructor(myActivity: FragmentActivity?)
    {
        if (myActivity != null) {
            activity = myActivity
        }
    }

    fun startLoadingDialog()
    {
        var builder = AlertDialog.Builder(activity)

        var inflater = activity.layoutInflater
        builder.setView(inflater.inflate(R.layout.dialog_loading,null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    fun dismissDialog()
    {
        dialog.dismiss()
    }

}