package com.example.myapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemimgBinding


class adaptadorTarea (
    private val tareas: MutableList<Fragment3.imgTarea>,
    private val onitemClicked: (Fragment3.imgTarea) -> Unit,
    private val onDeleteClicked: (Fragment3.imgTarea) -> Unit
) : RecyclerView.Adapter<adaptadorTarea.TareaImgVH>() {

    inner class TareaImgVH(private val binding: ItemimgBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(tarea: Fragment3.imgTarea) {
                binding.TVTitulo.text = tarea.titulo
                binding.TVDescripcion.text = tarea.descripcion

                val res = if (tarea.imgId != 0L) tarea.imgId.toInt() else R.drawable.img_1
                binding.imgTarea.setImageResource(res)

                binding.root.setOnClickListener {onitemClicked(tarea)}
                binding.btnborrar.setOnClickListener {onDeleteClicked(tarea)}
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaImgVH {
        val binding = ItemimgBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TareaImgVH(binding)
    }

    override fun onBindViewHolder(holder: TareaImgVH, position: Int) {
        holder.bind(tareas[position])
    }

    override fun getItemCount(): Int = tareas.size

    fun updateData(newTareas: List<Fragment3.imgTarea>) {
        tareas.clear()
        tareas.addAll(newTareas)
        notifyDataSetChanged()
    }
}