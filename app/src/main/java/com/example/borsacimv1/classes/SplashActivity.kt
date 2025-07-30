package com.example.borsacimv1.classes

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.borsacimv1.R

class SplashActivity : AppCompatActivity() {

    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate çağrıldı.")

        try {
            setContentView(R.layout.activity_splash)
            Log.d(TAG, "Splash ekranı yüklendi.")

            Handler(Looper.getMainLooper()).postDelayed({
                Log.d(TAG, "2 saniye bekleme tamamlandı. MainActivity başlatılıyor...")
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 1500)

        } catch (e: Exception) {
            Log.e(TAG, "Hata oluştu: ${e.localizedMessage}", e)
        }
    }
}
