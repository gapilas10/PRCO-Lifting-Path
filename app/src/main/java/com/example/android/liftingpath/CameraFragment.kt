package com.example.android.liftingpath

import android.content.Context
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
import org.opencv.core.*
import org.opencv.core.CvType.CV_32F
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 */
class CameraFragment : Fragment(), CameraBridgeViewBase.CvCameraViewListener2  {

    private var cameraBridgeViewBase: CameraBridgeViewBase? = null
    private var currentlyProcessing = false;
    private lateinit var listOfPoints: ArrayList<Point>
    private lateinit var tensorflowNet:Net

    private var  baseLoaderCallback = object : BaseLoaderCallback(this.activity){
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listOfPoints = ArrayList()
        initView()
    }

    private fun initView()
    {
        //Initialize camera
        cameraBridgeViewBase = openCvCameraView
        cameraBridgeViewBase!!.visibility = SurfaceView.VISIBLE
        cameraBridgeViewBase!!.setCvCameraViewListener(this)
        //Initialize button
        processButton.setOnClickListener{
            // toggle processing on-off
            currentlyProcessing = !currentlyProcessing
        }
        clearButton.setOnClickListener{
            listOfPoints.clear()
        }
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        val tensorflowModel = getPath("frozen_inference_graph.pb", this.context)
        val tensorflowGraph = getPath("ssdGraph.pbtxt", this.context)

        tensorflowNet = Dnn.readNetFromTensorflow(tensorflowModel, tensorflowGraph)
    }

    override fun onCameraViewStopped() {

    }

    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        val IN_WIDTH = 300.0
        val IN_HEIGHT = 300.0
        val IN_SCALE_FACTOR = 1.0
        val MEAN_VAL = 0.0
        val THRESHOLD = 0.6;
        val frame = inputFrame!!.rgba()

        if(currentlyProcessing)
        {
            // Get rid of alpha for processing
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);
            val imageBlob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR, Size(IN_WIDTH,IN_HEIGHT), Scalar(MEAN_VAL),true ,false)
            Log.d("image Blob :",imageBlob.toString())

            tensorflowNet.setInput(imageBlob)
            // This is used to reshape the "Matrix" class provided by Java, as Java does not allow for 4 dimensional arrays, so we store the 3rd and 4th array into a 2D array
            val detections = tensorflowNet.forward().reshape(1,1)

            val cols = frame.cols()
            val rows = frame.rows()

            for (i in 0 until detections.rows()) // For every detection
            {
                val confidence = detections.get(i,2)[0] // Get the confidence
                if (confidence > THRESHOLD)
                {
                    val left = detections.get(i,3)[0]*cols
                    val top = detections.get(i,4)[0]*rows
                    val right = detections.get(i,5)[0]*cols
                    val bottom = detections.get(i,6)[0]*rows

                    // Draw rectangle around detected object.
                    //Imgproc.rectangle(frame, Point(left,top), Point(right,bottom), Scalar(0.0,255.0,0.0))
                   // val label = "yellow_band" + ":" + confidence
                    //val baseline = intArrayOf(1)
                    //val labelSize = Imgproc.getTextSize(label, 2, 1.0,2,baseline)

                    // Find centre of rectangle from detected object.
                    val centre1 = (left + right) /2
                    val centre2 = (top + bottom) / 2
                    val circleCentre = Point(centre1,centre2)

                    listOfPoints.add(circleCentre)


                    //Imgproc.circle(frame,circleCentre, 5, Scalar(255.0,0.0,0.0), 5)

                    // Draw background for label
                    //Imgproc.rectangle(frame, Point(left,top-labelSize.height), Point(left+labelSize.width,top+baseline[0]),Scalar(0.0,0.0,0.0), 2)
                    //Imgproc.rectangle(frame, Point(right,bottom), Point(right+labelSize.height,top),Scalar(0.0,0.0,0.0), 2)
                    // Write class name and confidence
                    //Imgproc.putText(frame,label, Point(left,top), 2,1.0,Scalar(255.0,255.0,255.0))
                }
            }

        }

        if(listOfPoints.isNotEmpty())
        {
            val matOfPoint = MatOfPoint()
            matOfPoint.fromList(listOfPoints)
            val listOfPoints = ArrayList<MatOfPoint>()
            listOfPoints.add(matOfPoint)

            Imgproc.polylines(frame,listOfPoints,false,Scalar(SettingFragment.colorRed,SettingFragment.colorGreen,SettingFragment.colorBlue),SettingFragment.lineWidth)
        }

        return frame
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

    private fun getPath(file:String, context: Context?): String {
        val assetManager = context?.assets

        var inputStream:BufferedInputStream? = null
        try{
            // Read data from assets
            if (assetManager != null) {
                inputStream = BufferedInputStream(assetManager.open(file))
            }
            val data = inputStream?.available()?.let { ByteArray(it) }
            inputStream?.read(data)
            inputStream?.close()

            // Create copy of file in storage
            var outFile = File(context?.filesDir,file)
            var outputStream = FileOutputStream(outFile)
            outputStream.write(data)
            outputStream.close()
            // Return a path to file which may be read in common way
            return outFile.absolutePath
        }
        catch (e: IOException)
        {
            Log.i("", "Failed to get path. Exception : $e")
        }
        return ""
    }

}
