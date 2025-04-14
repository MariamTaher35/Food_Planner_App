package com.example.foodplannerapplication.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.foodplannerapplication.R
import com.example.foodplannerapplication.auth.AuthManager
import com.example.foodplannerapplication.databinding.ActivityMainBinding
import com.example.foodplannerapplication.utils.SessionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sessionManager: SessionManager
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var authManager: AuthManager
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupNavigation()
        setupGoogleSignIn()
        updateDrawerHeader()
        checkGuestMode()
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
    }

    private fun setupNavigation() {
        drawerLayout = binding.drawerLayout
        val navViewDrawer = binding.navViewDrawer
        val navViewBottom = binding.navViewBottom

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.mealOfDayFragment,
                R.id.searchFragment,
                R.id.categoriesFragment,
                R.id.countriesFragment,
                R.id.planFragment,
                R.id.favoritesFragment // Add favoritesFragment as a top-level destination if you want the hamburger icon on it
            ),
            drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navViewBottom.setupWithNavController(navController)
        navViewDrawer.setupWithNavController(navController)

        navViewDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    logout()
                    true
                }
                else -> {
                    val handled = menuItem.onNavDestinationSelected(navController)
                    if (handled) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    }
                    handled
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupGoogleSignIn() {
        auth = Firebase.auth
        sessionManager = SessionManager(this)
        authManager = AuthManager(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        Log.d("MainActivity", "Is Guest Mode in onCreateOptionsMenu: ${authManager.isGuestMode()}")
        if (authManager.isGuestMode()) {
            val settingsItem = menu?.findItem(R.id.action_settings)
            settingsItem?.setTitle(R.string.login)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("MainActivity", "onOptionsItemSelected: ${item.itemId}")
        return when (item.itemId) {
            R.id.action_settings -> {
                Log.d("MainActivity", "Settings/Login item clicked. Is Guest Mode: ${authManager.isGuestMode()}")
                if (authManager.isGuestMode()) {
                    Log.d("MainActivity", "Starting LoginActivity")
                    startActivity(Intent(this, LoginActivity::class.java))
                    return true
                } else {
                    // Handle settings action
                    Log.d("MainActivity", "Handling Settings action for logged-in user")
                    false
                }
            }
            else -> item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
        }
    }

    private fun updateDrawerHeader() {
        val user: FirebaseUser? = auth.currentUser
        val headerView = binding.navViewDrawer.getHeaderView(0)
        val nameTextView: TextView = headerView.findViewById(R.id.nameTextView)
        val emailTextView: TextView = headerView.findViewById(R.id.emailTextView)
        val profileImageView: ImageView = headerView.findViewById(R.id.imageView)

        if (user != null) {
            nameTextView.text = user.displayName
            emailTextView.text = user.email

            if (user.photoUrl != null) {
                Glide.with(this)
                    .load(user.photoUrl)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .into(profileImageView)
            } else {
                profileImageView.setImageResource(R.mipmap.ic_launcher_round)
            }
        } else {
            nameTextView.text = getString(R.string.guest_user)
            emailTextView.text = getString(R.string.not_logged_in)
            profileImageView.setImageResource(R.mipmap.ic_launcher_round)
        }
    }

    private fun logout() {
        sessionManager.clearSession()
        auth.signOut()
        googleSignInClient.signOut()
        authManager.setLoggedIn(false)
        authManager.setGuestMode(false)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun checkGuestMode() {
        if (authManager.isGuestMode()) {
            // You can add UI updates here to reflect guest mode in the header if needed
            val headerView = binding.navViewDrawer.getHeaderView(0)
            val nameTextView: TextView = headerView.findViewById(R.id.nameTextView)
            nameTextView.text = getString(R.string.guest_user)
        }
    }
}