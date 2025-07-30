package com.example.borsacimv1.classes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.borsacimv1.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Başlangıçta Ana Sayfa Fragment'ı göster
        setCurrentFragment(HomeFragment())

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> setCurrentFragment(HomeFragment())
                R.id.analysy -> setCurrentFragment(AnalysisFragment())
                R.id.podcast -> setCurrentFragment(PodcastFragment())
                R.id.favorities -> setCurrentFragment(FavoritesFragment())
                R.id.menu -> setCurrentFragment(SettingsFragment())
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmetLayout, fragment)
            commit()
        }
    }
}
