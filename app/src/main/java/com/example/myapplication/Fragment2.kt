package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapplication.databinding.Fragment2Binding
import com.google.firebase.firestore.Query

data class miTarea(
    val id: String = " ",//aqui colocan el usuario logueado
    val titulo: String = " ",
    val descripcion: String = " ",
    val userId: String = " ",//este carga automaticamente codificando
    val creadoEn: Long = 0L
)


class Fragment2 : Fragment() {
    val args: Fragment2Args by navArgs()
    var usr: String = ""

    private var _binding: Fragment2Binding? = null
    private val binding get() = _binding!!
    private lateinit var tareaAdapter: TareaAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = Fragment2Binding.inflate(inflater, container, false)
        return binding.root
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_2, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val onItemClickAction: (miTarea) -> Unit = { tarea ->
            Log.d("Fragment2", "¡Clic en la tarea (EDITAR): ${tarea.titulo}!")
        }

        val onDeleteClickAction: (miTarea) -> Unit = { tarea ->
            Log.d("Fragment2", "¡Solicitando borrar tarea: ${tarea.titulo}")
            borrarTarea(tarea)
        }
        tareaAdapter = TareaAdapter(
            mutableListOf(),  //lista inicial vacia
            onItemClickAction,
            onDeleteClickAction  //callback para borrado
        )

        //para la nueva tarea al hacer clic
        binding.recyclerViewtareas.apply {
            adapter = tareaAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }

        binding.floatingActionButton.setOnClickListener {
            mostrarDialogoNuevaTarea()
        }

        obtenerTareasDeFirestore()


        var datousuaario = arguments?.getString("usuario") ?: "Invitado"
        val txtusuario = view.findViewById<TextView>(R.id.nombreuser)
        txtusuario.text = datousuaario

        val button = view.findViewById<Button>(R.id.buttonhome)
        button.setOnClickListener({
            findNavController().navigate(Fragment2Directions.actionFragment2ToFragment1())
        })

        val btn = view.findViewById<Button>(R.id.buttonsig)
        btn.setOnClickListener({
            findNavController().navigate(Fragment2Directions.actionFragment2ToFragment3())
        })

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
    private fun borrarTarea(tarea: miTarea) {
        if (tarea.id.isEmpty()) {
            Toast.makeText(requireContext(), "Error: ID vacío, no se puede borrar.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Tareas").document(tarea.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Tarea eliminada", Toast.LENGTH_SHORT).show()
                Log.d("Fragment2", "Tarea eliminada con éxito: ${tarea.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Fragment2", "Error al eliminar tarea", e)
                Toast.makeText(requireContext(), "Error al eliminar tarea", Toast.LENGTH_SHORT).show()
            }
    }
    private fun agregarNuevaTarea(titulo: String, descripcion: String) {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            Log.e("Fragment2", "Usuario no autenticado. No se puede guardar la tarea.")
            return
        }

        val nuevaTarea = miTarea(
            titulo = titulo,
            descripcion = descripcion,
            userId = currentUserId,
            creadoEn = System.currentTimeMillis()
        )
        db.collection("Tareas")
            .add(nuevaTarea)
            .addOnSuccessListener { Log.d("Fragment2", "Tarea agregada con exito: ${it.id}") }
            .addOnFailureListener { e ->
                Log.e("Fragment2", "Error al agregar tarea", e)
                Log.e("Fragment2", "Error al agregar tarea", e)
            }
    }
    private fun obtenerTareasDeFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("Fragment2", "Usuario no autenticado. Abortando carga de tareas.")
            Toast.makeText(
                requireContext(),
                "Error: Debes iniciar sesión para ver las tareas.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        Log.d(
            "Fragment2",
            "DIAGNOSTICO 1: Usuario autenticado. UID: $userId. Iniciando escucha de FIrestore..."
        )
        db.collection("Tareas")
            .orderBy("creadoEn", Query.Direction.DESCENDING)
            .addSnapshotListener(requireActivity()) { snapshot, e ->
                if (e != null) {
                    Log.e("Fragment2", "Error al escuchar Firestore", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val nuevasTareas = mutableListOf<miTarea>()

                    Log.d("Fragment2", "Documentos recibidos: ${snapshot.size()}")

                    if (snapshot.isEmpty) {
                        Log.d("Fragment2", "Snapshot vacío. No hay tareas en la BD o el filtro es muy restrictivo.")
                    }

                    for (document in snapshot.documents) {
                        val documentId = document.id
                        val tarea = document.toObject(miTarea::class.java)

                        if (tarea != null) {
                            val tareaConId = tarea.copy(id = documentId)
                            nuevasTareas.add(tareaConId)
                            Log.d("Fragment2", "Tarea OK: $documentId - ${tarea.titulo}")
                        } else {
                            Log.e("Fragment2", "Fallo toObject() en documento: $documentId")
                            Log.e("Fragment2", "Contenido: ${document.data}")
                        }
                    }

                    Log.d("Fragment2", "Adaptador actualizado con ${nuevasTareas.size} tareas")
                    tareaAdapter.updateData(nuevasTareas)
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    class TareaAdapter(
        private val tareas: MutableList<miTarea>,
        private val onitemClicked: (miTarea) -> Unit,
        private val onDeleteClicked: (miTarea) -> Unit
    ) : RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

        inner class TareaViewHolder(private val binding: ItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(tarea: miTarea) {
                Log.d("Adapter", "Recibiendo tarea: ${tarea.titulo} - ${tarea.descripcion}")
                binding.TVTitulo.text = tarea.titulo
                binding.TVDescripcion.text = tarea.descripcion

                binding.root.setOnClickListener {
                    onitemClicked(tarea)
                }
                binding.button.setOnClickListener {
                    onDeleteClicked(tarea)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
            val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return TareaViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
            holder.bind(tareas[position])
        }

        override fun getItemCount(): Int = tareas.size

        fun updateData(newTareas: List<miTarea>) {
            tareas.clear()
            tareas.addAll(newTareas)
            notifyDataSetChanged()
        }
    }
}