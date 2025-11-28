package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController

class Fragment1 : Fragment() {
    var usr: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button = view.findViewById<Button>(R.id.buttonsiguiente)
        val textuser = view.findViewById<TextView>(R.id.textusuario)

        val correo = arguments?.getString("email")
        if (correo == null)
        else
            if (correo?.indexOf(string = "@")!! > 0)
            {
                var usua = correo.split("@")
                usr = usua.get(0).toString()
                textuser.text = usr
            }

        button.setOnClickListener({
            findNavController().navigate(Fragment1Directions.actionFragment1ToFragment2(usr))
        })
    }

}

