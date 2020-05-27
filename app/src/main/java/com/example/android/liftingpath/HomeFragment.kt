package com.example.android.liftingpath


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnLocal.setOnClickListener{
            val newFragment = VideoFragment()
            val transaction = fragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, newFragment)
                ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                ?.commit()
        }

        btnRealTime.setOnClickListener{
            val newFragment = CameraFragment()
            val transaction = fragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, newFragment)
                ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                ?.commit()

        }

        btnSettings.setOnClickListener{
            val newFragment = SettingFragment()
            val transaction = fragmentManager?.beginTransaction()
                ?.replace(R.id.frame_layout, newFragment)
                ?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                ?.commit()
        }

        super.onViewCreated(view, savedInstanceState)
    }
}
