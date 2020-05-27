package com.example.android.liftingpath


import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import kotlinx.android.synthetic.main.colorpicker.*
import kotlinx.android.synthetic.main.fragment_setting.*
import org.opencv.core.Scalar
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class SettingFragment : Fragment() {

    companion object {
        var lineWidth = 5
        var colorRed = 255.0
        var colorGreen = 0.0
        var colorBlue = 0.0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnColorPicker.setOnClickListener {
            colorSelector.visibility = View.VISIBLE
        }
        btnColorSelected.setOnClickListener{
            colorSelector.visibility = View.VISIBLE
        }

        widthValue.addTextChangedListener(object :TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var newNumber:String = s.toString()
                lineWidth = Integer.parseInt(newNumber)
            }

        })

        colorR.max = 255
        colorR.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val colorStr = getColorString()
                strColor.setText(colorStr.replace("#", "").toUpperCase(Locale.ROOT))
                btnColorPreview.setBackgroundColor(Color.parseColor(colorStr))
            }
        })
        colorG.max = 255
        colorG.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val colorStr = getColorString()
                strColor.setText(colorStr.replace("#","").toUpperCase(Locale.ROOT))
                btnColorPreview.setBackgroundColor(Color.parseColor(colorStr))
            }
        })
        colorB.max = 255
        colorB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val colorStr = getColorString()
                strColor.setText(colorStr.replace("#","").toUpperCase(Locale.ROOT))
                btnColorPreview.setBackgroundColor(Color.parseColor(colorStr))
            }
        })

        colorCancelBtn.setOnClickListener{
            colorSelector.visibility = View.GONE
        }

        colorOkBtn.setOnClickListener{
            val color:String = getColorString()
            btnColorSelected.setBackgroundColor(Color.parseColor(color))
            colorSelector.visibility = View.GONE
            colorRed = (((255*colorR.progress)/colorR.max).toDouble())
            colorGreen = (((255*colorG.progress)/colorG.max).toDouble())
            colorBlue = (((255*colorB.progress)/colorB.max).toDouble())
        }

        super.onViewCreated(view, savedInstanceState)
    }
    fun getColorString(): String {
        var redValue = Integer.toHexString(((255*colorR.progress)/colorR.max))
        if(redValue.length==1) {
            redValue = "0$redValue"

        }
        var greenValue = Integer.toHexString(((255*colorG.progress)/colorG.max))
        if(greenValue.length==1) {
            greenValue = "0$greenValue"
        }
        var blueValue = Integer.toHexString(((255*colorB.progress)/colorB.max))
        if(blueValue.length==1) {
            blueValue = "0$blueValue"
        }
        return "#$redValue$greenValue$blueValue"
    }
}