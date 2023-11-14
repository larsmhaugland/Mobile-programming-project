package com.example.flagged

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton

class CheckoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        val db = FirestoreDB()

        val user = db.getUsers().find { it.username == Username }
        val shoppingCartList: ArrayList<ShoppingCartItem> = user?.cart ?: ArrayList()
        val adapter = ShoppingCartAdapter(this, shoppingCartList)
        val listView = findViewById<ListView>(R.id.checkoutListView) // Replace with your ListView ID
        listView.adapter = adapter




        val nameInput = findViewById<EditText>(R.id.checkoutName)
        val addressInput = findViewById<EditText>(R.id.checkoutAddress)
        val zipInput = findViewById<EditText>(R.id.checkoutZIP)
        val townInput = findViewById<EditText>(R.id.checkoutTown)
        val emailInput = findViewById<EditText>(R.id.checkoutEmail)
        val phoneInput = findViewById<EditText>(R.id.checkoutPhone)

        val feedback = findViewById<TextView>(R.id.checkoutFeedback)

        val submitButton = findViewById<Button>(R.id.submitOrderButton)

        val totalCost = findViewById<TextView>(R.id.checkoutTotalCost)
        val tCostText = "Total cost: $" + shoppingCartList.sumOf { it.amount * it.price }
        totalCost.text = tCostText

        submitButton.setOnClickListener {

            if (nameInput.text.isEmpty() || addressInput.text.isEmpty() || zipInput.text.isEmpty() || townInput.text.isEmpty() || emailInput.text.isEmpty() || phoneInput.text.isEmpty()){
                feedback.text = "You need to fill in all fields"
                return@setOnClickListener
            } else {
                feedback.text = ""
            }
            val intent = Intent(this, OrderCompleteActivity::class.java)
            startActivity(intent)
            if (user != null) {
                user.cart.clear()
                db.patchUser(user)
            }
            finish()
        }
    }
}


class ShoppingCartAdapter(private val context: Context, private val itemList: ArrayList<ShoppingCartItem>) : BaseAdapter() {

    override fun getCount(): Int = itemList.size

    override fun getItem(position: Int): Any = itemList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = convertView ?: inflater.inflate(R.layout.checkout_item_layout, parent, false)

        val item = getItem(position) as ShoppingCartItem

        val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        val quantityTextView = view.findViewById<TextView>(R.id.quantityTextView)
        val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
        val removeFromCartButton = view.findViewById<AppCompatButton>(R.id.removeFromCartButton)

        nameTextView.text = item.name
        val qText = "Quantity: " + item.amount.toString()
        quantityTextView.text = qText
        val pText = "Price: $" + item.amount * item.price
        priceTextView.text = pText

        removeFromCartButton.setOnClickListener() {
            val db = FirestoreDB()


            val user = db.getUsers().find { it.username == Username }
            //Remove item from users cart:
            if (user != null) {
                user.cart.remove(item)
                db.patchUser(user)
            }
            //Remove item from view:
            itemList.remove(item)
            notifyDataSetChanged()
            Toast.makeText(context, "Flag removed from cart", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}