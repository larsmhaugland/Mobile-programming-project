package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton

/**
 * This activity is used to display a message to the user when they have completed an order.
 */
class OrderCompleteActivity : AppCompatActivity() {
    /**
     *  This function is called when the activity is created.
     *  @param savedInstanceState The saved instance state bundle.
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_complete)

        //Set so that the button leads back to the main shop activity
        val backToShopButton = findViewById<AppCompatButton>(R.id.returnToHomeButton)
        backToShopButton.setOnClickListener {
            finish()
        }
    }
}