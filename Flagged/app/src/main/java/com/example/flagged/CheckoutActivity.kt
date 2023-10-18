package com.example.flagged

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*

class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val submitButton = findViewById<Button>(R.id.submitOrderButton)
        submitButton.setOnClickListener {
            val intent = Intent(this, OrderCompleteActivity::class.java)
            startActivity(intent)
        }
    }
}