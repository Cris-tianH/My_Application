package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Fragmentimg : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fragmentimg, container, false)
    }

    private lateinit var recicler: RecyclerView


    private val imagenes = listOf(
        R.drawable.img_1,
        R.drawable.img_2,
        R.drawable.img_3,
        R.drawable.img_4,
        R.drawable.img_5,
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recicler = view.findViewById(R.id.RGaleria)
        recicler.layoutManager = GridLayoutManager(requireContext(),5)
        recicler.adapter = FragmentAdaptador(imagenes) { imagenSeleccionada ->
            setFragmentResult("imagenSeleccionada", Bundle().apply { putInt("resId", imagenSeleccionada) })
            findNavController().popBackStack()
            Toast.makeText(requireContext(), "Alguna imagen seleccionaste", Toast.LENGTH_SHORT)
                .show()
        }


    }
}