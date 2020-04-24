package com.example.android.liftingpath


import android.app.Activity.RESULT_CANCELED
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import kotlinx.android.synthetic.main.fragment_video.*
import org.opencv.core.Mat
import org.opencv.videoio.VideoCapture
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class VideoFragment : Fragment() {
    private var VIDEO_DIRECTORY = "/DemoVideos"
    private var GALLERY = 1
    private var CAMERA = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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
            Log.d("what", "gallery")

                val contentURI = data?.data

                val selectedVideoPath = getPath(contentURI).toString()
                Log.d("path", selectedVideoPath)
                saveVideoToInternalStorage(selectedVideoPath)
                VideoView.setVideoURI(contentURI)
                VideoView.requestFocus()
                VideoView.start()

        }
        else if (requestCode == CAMERA)
        {
            Log.d("what", "camera")
            val contentURI = data?.data
            val recordedVideoPath = getPath(contentURI).toString()
            Log.d("path", recordedVideoPath)
            saveVideoToInternalStorage(recordedVideoPath)
            VideoView.setVideoURI(contentURI)
            VideoView.requestFocus()
            VideoView.start()
        }
    }

    private fun saveVideoToInternalStorage(selectedVideoPath: String) {
        val newFile: File

        try {
            val currentFile = File(selectedVideoPath)
            val wallpaperDirectory =
                File(context?.getExternalFilesDir(null)?.absolutePath + VIDEO_DIRECTORY)
            newFile =
                File(wallpaperDirectory, Calendar.getInstance().timeInMillis.toString() + ".mp4")

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

    private fun getPath(contentURI: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media._ID)
        val columnIndexID: Int
        val cursor =
            contentURI?.let { activity?.contentResolver?.query(it, projection, null, null, null) }

        return if (cursor != null) {
            columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            cursor.moveToFirst()
            cursor.getString(columnIndexID)
        } else {
            null
        }
    }
}




