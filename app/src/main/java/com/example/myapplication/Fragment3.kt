package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.Fragment3Binding
import com.example.myapplication.databinding.ItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Fragment3 : Fragment() {

    data class imgTarea(
        val id: String = " ",//aqui colocan el usuario logueado
        val titulo: String = " ",
        val descripcion: String = " ",
        val userId: String = " ",//este carga automaticamente codificando
        val creadoEn: Long = 0L,
        val imgId: Long = 0L
    )

    private var _binding: Fragment3Binding? = null
    private val binding get() = _binding!!
    private lateinit var tareaAdapter: adaptadorTarea
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var imagenSeleccionada: Long = 0L


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Fragment3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_fragment3_to_fragmentimg)
        }

        binding.buttonhom.setOnClickListener {
            findNavController().navigate(R.id.action_fragment3_to_fragment1)
        }

        setFragmentResultListener("imagenSeleccionada") { _, bundle ->
            val resId = bundle.getInt("resId", 0)
            imagenSeleccionada = resId.toLong()
            Log.d("Fragment3", "Imagen seleccionada: $imagenSeleccionada")
            mostrarDialogoNuevaTarea()
        }

        val onItemClickAction: (imgTarea) -> Unit = { tarea ->
            Log.d("Fragment3", "¡Clic en la tarea (EDITAR): ${tarea.titulo}!")
        }
        val onDeleteClickAction: (imgTarea) -> Unit = { tarea ->
            Log.d("Fragment3", "¡Solicitando borrar tarea: ${tarea.titulo}")
            borrarTarea(tarea)
        }

        tareaAdapter = adaptadorTarea(
            mutableListOf(),  //lista inicial vacia
            onItemClickAction,
            onDeleteClickAction  //callback para borrado
        )

        binding.recyclerViewimgtareas.apply{
            adapter = tareaAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }

        obtenerTareasDeFirestore()
    }

    private fun mostrarDialogoNuevaTarea() {
        val context = requireContext()

        val tituloInput = EditText(context).apply { hint = "Titulo de la tarea" }
        val descripcionInput = EditText(context).apply { hint = "Descripcion (opcional)" }

        val layout = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            addView(tituloInput)
            addView(descripcionInput)
        }

        AlertDialog.Builder(context)
            .setTitle("Añadir Nueva tarea")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val titulo = tituloInput.text.toString().trim()
                val descripcion = descripcionInput.text.toString().trim()

                if (titulo.isNotEmpty()) {
                    agregarNuevaTarea(titulo, descripcion)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun borrarTarea(tarea: imgTarea) {
        if (tarea.id.isEmpty()) {
            Toast.makeText(requireContext(), "Error: ID vacío, no se puede borrar.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Tareas2").document(tarea.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Tarea eliminada", Toast.LENGTH_SHORT).show()
                Log.d("Fragment3", "Tarea eliminada con éxito: ${tarea.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Fragment3", "Error al eliminar tarea", e)
                Toast.makeText(requireContext(), "Error al eliminar tarea", Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarNuevaTarea(titulo: String, descripcion: String) {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            Log.e("Fragment3", "Usuario no autenticado. No se puede guardar la tarea.")
            return
        }

        val nuevaTarea = imgTarea(
            titulo = titulo,
            descripcion = descripcion,
            userId = currentUserId,
            creadoEn = System.currentTimeMillis(),
            imgId = (if (imagenSeleccionada != 0L) imagenSeleccionada else R.drawable.img_1.toLong())
        )
        db.collection("Tareas2")
            .add(nuevaTarea)
            .addOnSuccessListener { Log.d("Fragment3", "Tarea agregada con exito: ${it.id}") }
            .addOnFailureListener { e ->
                Log.e("Fragment3", "Error al agregar tarea", e)
                Log.e("Fragment3", "Error al agregar tarea", e)
            }
    }

    private fun obtenerTareasDeFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("Fragment3", "Usuario no autenticado. Abortando carga de tareas.")
            Toast.makeText(
                requireContext(),
                "Error: Debes iniciar sesión para ver las tareas.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Log.d(
            "Fragment3",
            "DIAGNOSTICO 1: Usuario autenticado. UID: $userId. Iniciando escucha de FIrestore..."
        )
        db.collection("Tareas2")
            .orderBy("creadoEn", Query.Direction.DESCENDING)
            .addSnapshotListener(requireActivity()) { snapshot, e ->
                if (e != null) {
                    Log.e("Fragment3", "Error al escuchar Firestore", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val nuevasTareas = mutableListOf<imgTarea>()

                    Log.d("Fragment3", "Documentos recibidos: ${snapshot.size()}")

                    if (snapshot.isEmpty) {
                        Log.d("Fragment3", "Snapshot vacío. No hay tareas en la BD o el filtro es muy restrictivo.")
                    }

                    for (document in snapshot.documents) {
                        val documentId = document.id
                        val tarea = document.toObject(imgTarea::class.java)

                        if (tarea != null) {
                            val tareaConId = tarea.copy(id = documentId)
                            nuevasTareas.add(tareaConId)
                            Log.d("Fragment3", "Tarea OK: $documentId - ${tarea.titulo}")
                        } else {
                            Log.e("Fragment3", "Fallo toObject() en documento: $documentId")
                            Log.e("Fragment3", "Contenido: ${document.data}")
                        }
                    }

                    Log.d("Fragment3", "Adaptador actualizado con ${nuevasTareas.size} tareas")
                    tareaAdapter.updateData(nuevasTareas)
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

