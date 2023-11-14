package com.example.flagged

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import org.w3c.dom.Text

data class ShoppingItem(
    val name: String,
    val quantity: Int,
    val price: Double
)
class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val shoppingItemNames = intent.getStringArrayExtra("shoppingCartNames")
        val shoppingItemAmounts = intent.getIntArrayExtra("shoppingCartAmounts")

        val db = FirestoreDB()

        var shoppingCart = db.getFlags().filter { item ->
            item.name in (shoppingItemNames?.asIterable() ?: emptyList())
        }


        val nameInput = findViewById<EditText>(R.id.checkoutName)
        val addressInput = findViewById<EditText>(R.id.checkoutAddress)
        val zipInput = findViewById<EditText>(R.id.checkoutZIP)
        val townInput = findViewById<EditText>(R.id.checkoutTown)
        val emailInput = findViewById<EditText>(R.id.checkoutEmail)
        val phoneInput = findViewById<EditText>(R.id.checkoutPhone)

        val feedback = findViewById<TextView>(R.id.checkoutFeedback)

        val submitButton = findViewById<Button>(R.id.submitOrderButton)

        submitButton.setOnClickListener {


            if (nameInput.text.isEmpty() || addressInput.text.isEmpty() || zipInput.text.isEmpty() || townInput.text.isEmpty() || emailInput.text.isEmpty() || phoneInput.text.isEmpty()){
                feedback.text = "You need to fill in all fields"
                return@setOnClickListener
            }
            val intent = Intent(this, OrderCompleteActivity::class.java)
            startActivity(intent)
        }
    }
}


class CheckOutAdapter