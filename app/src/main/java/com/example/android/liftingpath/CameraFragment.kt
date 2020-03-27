package com.example.android.liftingpath

import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_camera.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.CvType
import org.opencv.core.Mat


/**
 * A simple [Fragment] subclass.
 */
class CameraFragment : Fragment(), CameraBridgeViewBase.CvCameraViewListener2  {

    //private var decorView: View?=null

    private var cameraBridgeViewBase: CameraBridgeViewBase? = null

    private val baseLoaderCallback = object : BaseLoaderCallback(this.activity){
        override fun onManagerConnected(status: Int) {
            when(status){
                    LoaderCallbackInterface.SUCCESS ->
                    {
                        cameraBridgeViewBase!!.enableView()
                    }
                else -> super.onManagerConnected(status)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        //change orientation to landscape
        //activity?.requestedOrientation = SCREEN_ORIENTATION_LANDSCAPE
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView()
    {

        //Initialize camera
        cameraBridgeViewBase = openCvCameraView
        cameraBridgeViewBase!!.visibility = SurfaceView.VISIBLE
        cameraBridgeViewBase!!.setCvCameraViewListener(this)
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {

        return inputFrame!!.rgba()
    }

    override fun onResume() {
        super.onResume()

        if(OpenCVLoader.initDebug())
        {
            Log.d(this.activity.toString(),"OpenCV successfully loaded")
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        } else {
            Log.d(this.activity.toString(),"OpenCV load failed")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this.activity,baseLoaderCallback)
        }

    }

    override fun onPause() {
        super.onPause()
        if(cameraBridgeViewBase!=null){ //If screen goes to sleep, turn off camera
            cameraBridgeViewBase!!.disableView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(cameraBridgeViewBase != null){ //If app is closed, turn off camera
            cameraBridgeViewBase!!.disableView()
        }
        //return to normal orientation
        activity?.requestedOrientation = SCREEN_ORIENTATION_UNSPECIFIED
    }
}
