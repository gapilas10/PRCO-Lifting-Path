package com.example.android.liftingpath


import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import kotlinx.android.synthetic.main.fragment_video.*
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class VideoFragment : Fragment() {
    private var VIDEO_DIRECTORY = "/DemoVideos"
    private var GALLERY = 1
    private var CAMERA = 2

    private lateinit var listOfPoints: ArrayList<Point>
    private lateinit var tensorflowNet:Net

    private lateinit var loadingDialog: LoadingDialog

    private fun LoadOpenCV(){
        OpenCVLoader.initDebug()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        LoadOpenCV()
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        SelectVideoButton.setOnClickListener {
            val builder = AlertDialog.Builder(this.activity)
            builder.setTitle("Select Action")
            val pictureDialogItems = arrayOf("Select video from gallery", "Record video from camera")
            builder.setItems(pictureDialogItems) { _, which ->
                try {
                    when (which) {
                        0 -> chooseVideoFromGallery()
                        1 -> takeVideoFromCamera()
                    }
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this.activity, "Option not available", Toast.LENGTH_SHORT).show()
                }
            }
            val dialog = builder.create()
            dialog.show()
        }
        VideoView.setOnPreparedListener {mp ->
                // Start Playback
                VideoView.start()
                // Loop Video
                mp!!.isLooping = true
            }

        listOfPoints = ArrayList()
        val tensorflowModel = getPath("frozen_inference_graph.pb", this.context)
        val tensorflowGraph = getPath("ssdGraph.pbtxt", this.context)

        tensorflowNet = Dnn.readNetFromTensorflow(tensorflowModel, tensorflowGraph)

        loadingDialog = LoadingDialog(activity)

        super.onViewCreated(view, savedInstanceState)
    }

    private fun chooseVideoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takeVideoFromCamera() {
        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(videoIntent, CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("result","" + resultCode)
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_CANCELED)
        {
            Log.d("what","cancel")
        }
        if (requestCode == GALLERY)
        {
                val contentURI = data?.data

                val selectedVideoPath = RealPathUtil.getRealPath(context!!, contentURI!!)
            if (selectedVideoPath != null) {
                convertToImages(selectedVideoPath)
            }

        }
        else if (requestCode == CAMERA)
        {
            val contentURI = data?.data
            val recordedVideoPath = RealPathUtil.getRealPath(context!!, contentURI!!)
            if (recordedVideoPath != null) {
                saveVideoToInternalStorage(recordedVideoPath)
                convertToImages(recordedVideoPath)
            }
        }
    }

    private fun saveVideoToInternalStorage(selectedVideoPath: String) {
        val newFile: File

        try {
            val currentFile = File(selectedVideoPath)
            val wallpaperDirectory = File(context?.getExternalFilesDir(null)?.absolutePath + VIDEO_DIRECTORY)
            newFile = File(wallpaperDirectory, Calendar.getInstance().timeInMillis.toString() + ".mp4")

            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs()
            }

            if (currentFile.exists()) {
                val INput = FileInputStream(currentFile)
                val OUTput = FileOutputStream(newFile)

                // Copy the bits from instream to outstream
                val buffer = ByteArray(1024)
                while ((INput.read(buffer)) > 0) {
                    OUTput.write(buffer, 0, INput.read(buffer))
                }
                INput.close()
                OUTput.close()
                Log.v("vii", "Video file saved successfully.")
            } else {
                Log.v("vii", "Video saving failed. Source file missing.")
            }
        }
        catch (e:Exception)
        {
            e.printStackTrace()
        }
    }

    private fun convertToImages(selectedVideoPath: String)
    {
        Log.d("ENTERED convertToImages", "ENTERED convertToImages")
        val filePath = File(selectedVideoPath).absolutePath
        val wallpaperDirectory = File(context?.getExternalFilesDir(null)?.absolutePath + VIDEO_DIRECTORY)
        val newFile = File(wallpaperDirectory, Calendar.getInstance().timeInMillis.toString())
        if (!newFile.exists()) {
            newFile.mkdirs()
        }
        //var newFile = "$filePath-COPY"
        val command = arrayOf("-i", filePath, "-r", "24/1", "$newFile/image_%03d.jpg")
        try {
            FFmpeg.getInstance(context).execute(command,object : ExecuteBinaryResponseHandler() {
                override fun onStart() {
                    Log.d("TAG", "Started command")
                    loadingDialog.startLoadingDialog()
                }

                override fun onFailure(message: String?) {
                    Log.d("TAG", "Failed. Message : $message")
                    loadingDialog.dismissDialog()
                }

                override fun onProgress(message: String?) {
                    Log.d("TAG", "Progress : $message")
                }

                override fun onSuccess(message: String?) {
                    Log.d("TAG", "successfully saved into : $newFile")
                    processImages(newFile.absolutePath)
                }

                override fun onFinish() {
                    Log.d("TAG", "Finished command")
                }
            })

        } catch (e: Exception) {
            Log.d("Exception : ", e.toString())
        }
    }

    private fun convertToVideo(processedImagesPath: String)
    {
        Log.d("ENTERED convertToVideo", "ENTERED convertToVideo")
        val filePath = File(processedImagesPath).absolutePath
        val command = arrayOf("-r","24","-f","image2","-i", "$filePath/image_%03d.jpg", "-vcodec", "libx264", "$filePath/processedVideo.mp4")
        try {
            FFmpeg.getInstance(context).execute(command,object : ExecuteBinaryResponseHandler() {
                override fun onStart() {
                    Log.d("TAG", "Started command")
                }

                override fun onFailure(message: String?) {
                    Log.d("TAG", "Failed. Message : $message")
                    loadingDialog.dismissDialog()
                }

                override fun onProgress(message: String?) {
                    Log.d("TAG", "Progress : $message")
                }

                override fun onSuccess(message: String?) {
                    Log.d("TAG", "successfully saved into : $filePath")
                    VideoView.setVideoPath("$filePath/processedVideo.mp4")
                    VideoView.requestFocus()
                    VideoView.start()
                    loadingDialog.dismissDialog()
                }

                override fun onFinish() {
                    Log.d("TAG", "Finished command")
                }
            })

        } catch (e: Exception) {
            Log.d("Exception : ", e.toString())
        }

    }

    private fun processImages(convertedImagesPath: String)
    {
        val directory = File("$convertedImagesPath")

        val IN_WIDTH = 300.0
        val IN_HEIGHT = 300.0
        val IN_SCALE_FACTOR = 1.0
        val MEAN_VAL = 0.0
        val THRESHOLD = 0.6;

        val listOfImages:ArrayList<File> = ArrayList()
        directory.walkTopDown().forEach {
            listOfImages.add(it)
        }
        listOfImages.removeAt(0) // get rid of folder
        listOfImages.forEach {
            val frame:Mat = Imgcodecs.imread(it.absolutePath)
            Imgproc.cvtColor(frame,frame,Imgproc.COLOR_RGBA2RGB)
            val imageBlob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR, Size(IN_WIDTH,IN_HEIGHT), Scalar(MEAN_VAL),true ,false)
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

                    // Find centre of rectangle from detected object.
                    val centre1 = (left + right) /2
                    val centre2 = (top + bottom) / 2
                    val circleCentre = Point(centre1,centre2)

                    listOfPoints.add(circleCentre)
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
            Imgcodecs.imwrite(it.absolutePath,frame)
        }
        convertToVideo(directory.absolutePath)

    }

    private fun getPath(file:String, context: Context?): String {
        val assetManager = context?.assets

        var inputStream: BufferedInputStream? = null
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
object RealPathUtil {
    fun getRealPath(context: Context, fileUri: Uri): String? {
        val realPath: String?
        realPath =  getRealPathFromURI_API19(context, fileUri)

        return realPath
    }

    @SuppressLint("NewApi")
    fun getRealPathFromURI_API19(
        context: Context,
        uri: Uri
    ): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return context.getExternalFilesDir(null).toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}





