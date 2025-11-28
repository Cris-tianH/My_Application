package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Por favor ingresa correo y contraseña")
            } else {
                //Primero verificamos el acceso especial (parcia2)
                if (email == "parcial2" && password == "26112025") {
                    // Si coincide, entramos directamente sin llamar a Firebase
                    irAPantallaPrincipal(email)
                }
                else {
                    if (email.contains("@")) {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                irAPantallaPrincipal(email)
                            }
                            .addOnFailureListener {
                                showToast("Error al iniciar sesión: ${it.message}")
                            }
                    } else {
                        // Si no es "parcia2" y tampoco tiene arroba, mostramos error
                        showToast("Credenciales incorrectas")
                    }
                }

            }
        }

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Por favor completa ambos campos")
            } else {
                // Validación extra para asegurar que sea un correo válido para Firebase
                if (email.contains("@")) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            showToast("Cuenta creada exitosamente")
                            emailEditText.text.clear()
                            passwordEditText.text.clear()
                        }
                        .addOnFailureListener {
                            showToast("Error al crear cuenta: ${it.message}")
                        }
                } else {
                    showToast("Ingresa un correo válido para registrarte")
                }
            }
        }
    }

    // Función auxiliar para no repetir el código del Intent
    private fun irAPantallaPrincipal(usuario: String) {
        val intent = Intent(this, principal::class.java)
        intent.putExtra("email", usuario)
        intent.putExtra("extra", "algun dato")
        intent.putExtra("numero", 23)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}




