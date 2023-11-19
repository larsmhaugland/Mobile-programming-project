package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton

class OrderCompleteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_complete)

        val backToShopButton = findViewById<AppCompatButton>(R.id.returnToHomeButton)

        backToShopButton.setOnClickListener {
            finish()
        }
    }
}