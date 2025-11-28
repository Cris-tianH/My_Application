package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class FragmentAdaptador(
        private val imagenes: List<Int>,
        private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<FragmentAdaptador.VH>() {

        inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
                private val imagen: ImageView = itemView.findViewById(R.id.imgIcono)

                fun bind(resId: Int) {
                        imagen.setImageResource(resId)
                        itemView.setOnClickListener { onClick(resId) }
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
                val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.imagen, parent, false)
                return VH(view)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
                holder.bind(imagenes[position])
        }

        override fun getItemCount(): Int = imagenes.size
}