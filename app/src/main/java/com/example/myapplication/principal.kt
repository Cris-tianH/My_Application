package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView

class principal : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        val email = intent.getStringExtra("email")
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.contenedorFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val parametro = bundleOf("email" to email.toString())

        drawerLayout = findViewById(R.id.main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val navView : NavigationView = findViewById(R.id.menu)


        //para el actionbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Menu"
        //para la navegacion
        val appbar = AppBarConfiguration(setOf(R.id.fragment1,R.id.fragment2,R.id.fragment3), drawerLayout)
        NavigationUI.setupWithNavController(toolbar, navController, appbar)
        NavigationUI.setupWithNavController(navigationView = navView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragment1 -> supportActionBar?.title = "Inicio"
                R.id.fragment2 -> supportActionBar?.title = "Tareas"
                R.id.fragment3 -> supportActionBar?.title = "Tareas con imagen"
                else -> supportActionBar?.title = "Menú"
            }
        }


        navController.navigate(R.id.fragment1, parametro)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
