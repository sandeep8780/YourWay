package com.example.yourway.post

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.yourway.R
import com.google.android.material.chip.Chip

class DisplayFullPost : Fragment() {

    private lateinit var iv_profileImg : ImageView
    private lateinit var chip_username : Chip
    private lateinit var tv_title : TextView
    private lateinit var tv_description : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_display_full_post, container, false)

        //initialization

        iv_profileImg = view.findViewById(R.id.iv_display_profile_profileImage)
        chip_username = view.findViewById(R.id.chip_displayfullpost_username)
        tv_title = view.findViewById(R.id.tv_title)
        tv_description = view.findViewById(R.id.tv_description)


        return view
    }


}