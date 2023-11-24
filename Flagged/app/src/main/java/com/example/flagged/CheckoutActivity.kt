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
        val db = FirestoreDB.getInstance()

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

        //Update total cost
        updateTotalCost()

        submitButton.setOnClickListener {
            //Check if all fields are filled in
            if (nameInput.text.isEmpty() || addressInput.text.isEmpty() || zipInput.text.isEmpty() || townInput.text.isEmpty() || emailInput.text.isEmpty() || phoneInput.text.isEmpty()){
                //If not, show a message and return
                feedback.text = "You need to fill in all fields"
                return@setOnClickListener
            } else {
                //If all fields are filled in, clear the feedback text
                feedback.text = ""
            }
            //Clear the users cart
            if (user != null) {
                user.cart.clear()
                updateTotalCost()
                db.patchUser(user)
            }
            //Send the user to the order complete activity
            val intent = Intent(this, OrderCompleteActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun updateTotalCost() {
        val totalCostView = findViewById<TextView>(R.id.checkoutTotalCost)
        val db = FirestoreDB.getInstance()
        val tCost = db.getUsers().find{it.username == Username}?.cart?.sumOf { it.amount * it.price } ?: 0
        val tCostText = "Total cost: $" + tCost
        totalCostView.text = tCostText

    }
}

/**
 * This adapter is used to populate the list view with the items in the shopping cart.
 * @param context The context of the adapter.
 * @param itemList The list of items to be displayed.
 */
class ShoppingCartAdapter(private val context: Context, private val itemList: ArrayList<ShoppingCartItem>) : BaseAdapter() {
    /**
     * Get the number of items in the list.
     * @return The number of items in the list.
     */
    override fun getCount(): Int = itemList.size

    /**
     * Get the item at the specified position.
     * @param position The position of the item in the list.
     * @return The item at the specified position.
     */
    override fun getItem(position: Int): Any = itemList[position]

    /**
     * Get the id of the item at the specified position.
     * @param position The position of the item in the list.
     * @return The id of the item at the specified position.
     */
    override fun getItemId(position: Int): Long = position.toLong()

    /**
     * Get the view for each item in the list.
     * @param position The position of the item in the list.
     * @param convertView The view of the item.
     * @param parent The parent view.
     * @return The view of the item.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Inflate the view if it is null
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = convertView ?: inflater.inflate(R.layout.checkout_item_layout, parent, false)

        // Get the item at the specified position
        val item = getItem(position) as ShoppingCartItem
        // Get the views from the layout
        val nameTextView = view.findViewById<TextView>(R.id.nameTextView)
        val quantityTextView = view.findViewById<TextView>(R.id.quantityTextView)
        val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
        val removeFromCartButton = view.findViewById<AppCompatButton>(R.id.removeFromCartButton)

        // Set the text of the views to the item's values
        nameTextView.text = item.name
        val qText = "Quantity: " + item.amount.toString()
        quantityTextView.text = qText
        val pText = "Price: $" + item.amount * item.price
        priceTextView.text = pText

        // Set the click listener for the remove from cart button
        removeFromCartButton.setOnClickListener() {
            //Add back stock to item:
            val db = FirestoreDB.getInstance()
            val user = db.getUsers().find { it.username == Username }
            db.updateStock(item.name, item.amount)
            //Remove item from users cart:
            if (user != null) {
                user.cart.remove(item)
                db.patchUser(user)
                (context as CheckoutActivity).updateTotalCost()
            }
            //Remove item from view:
            itemList.remove(item)
            notifyDataSetChanged()
        }
        return view
    }
}