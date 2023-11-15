package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import java.io.Serializable

var Username = ""
class ShopActivity : AppCompatActivity(){
    private lateinit var flagListView: ListView
    private lateinit var flagItems: List<Flag>
    private lateinit var filteredFlagItems : List<Flag>
    private lateinit var adapter: FlagAdapter
    private lateinit var editTextSearch: EditText
    private var isReverseSort = false


    private fun sortAlphabetically(items: List<Flag>): List<Flag> {
        return items.sortedBy { it.name }
    }
    private fun sortReverseAlphabetically(items: List<Flag>): List<Flag> {
        return items.sortedByDescending { it.name }
    }
    private fun sortLowHighByPrice(items: List<Flag>): List<Flag> {
        return items.sortedBy { it.price.toDouble() }
    }
    private fun sortHighLowByPrice(items: List<Flag>): List<Flag> {
        return items.sortedByDescending { it.price.toDouble() }
    }
    private fun filterByCategory(items: List<Flag>, category: String): List<Flag> {
        return items.filter { it.category == category }
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.filter_options, null)

        val sortAZRadioButton = dialogView.findViewById<RadioButton>(R.id.sortAZ)
        val sortZARadioButton = dialogView.findViewById<RadioButton>(R.id.sortZA)
        val sortLowHighRadioButton = dialogView.findViewById<RadioButton>(R.id.sortLowHigh)
        val sortHighLowRadioButton = dialogView.findViewById<RadioButton>(R.id.sortHighLow)

        val categoryCountryRadioButton = dialogView.findViewById<RadioButton>(R.id.categoryCountry)
        val categoryNavalRadioButton = dialogView.findViewById<RadioButton>(R.id.categoryNaval)
        val categoryLimitedEditionRadioButton = dialogView.findViewById<RadioButton>(R.id.categoryLimitedEdition)
        val categoryLGBTQRadioButton = dialogView.findViewById<RadioButton>(R.id.categoryLGBTQ)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Filter Options")
            .setPositiveButton("Apply") { dialog, _ ->
                if (sortAZRadioButton.isChecked) {
                    filteredFlagItems = sortAlphabetically(flagItems)
                    isReverseSort = false
                } else if (sortZARadioButton.isChecked) {
                    filteredFlagItems = sortReverseAlphabetically(flagItems)
                    isReverseSort = true
                }else if (sortLowHighRadioButton.isChecked) {
            filteredFlagItems = sortLowHighByPrice(flagItems)
            isReverseSort = false
            } else if (sortHighLowRadioButton.isChecked) {
            filteredFlagItems = sortHighLowByPrice(flagItems)
            isReverseSort = true
            }else if (categoryCountryRadioButton.isChecked) {
                    filteredFlagItems = filterByCategory(flagItems,"Country")
                } else if (categoryNavalRadioButton.isChecked) {
                    filteredFlagItems = filterByCategory(flagItems,"Naval")
                } else if (categoryLimitedEditionRadioButton.isChecked) {
                    filteredFlagItems = filterByCategory(flagItems,"Limited Edition")
                } else if (categoryLGBTQRadioButton.isChecked) {
                    filteredFlagItems = filterByCategory(flagItems, "LGBTQ+")
                }

                val newAdapter = FlagAdapter(this@ShopActivity, filteredFlagItems, intent)
                flagListView.adapter = newAdapter

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)
        val startTime = System.currentTimeMillis()
        val username = intent.getStringExtra("username")
        val db = FirestoreDB.getInstance()
        editTextSearch = findViewById(R.id.searchField)
        flagListView = findViewById(R.id.itemListView)

        val favouritesButton = findViewById<Button>(R.id.favouritesButton)
        favouritesButton.setOnClickListener {
            val intent= Intent(this,FavouritesActivity::class.java)
            intent.putExtra("username",username)
            startActivity(intent)

        }

        val checkOut = findViewById<Button>(R.id.checkoutButton)
        checkOut.setOnClickListener {
            val intent= Intent(this,CheckoutActivity::class.java)
            Username = username.toString()
            startActivity(intent)

        }

        val filterButton = findViewById<Button>(R.id.filterButton)
        filterButton.setOnClickListener {
            // Show the filter dialog
            showFilterDialog()
        }

        flagItems = db.getFlags()
        filteredFlagItems = flagItems

        adapter = FlagAdapter(this, flagItems, intent)
        flagListView.adapter = adapter

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                val filteredFlagItems = mutableListOf<Flag>()

                val itemsStartingWithSearchText = flagItems.filter { flagItem ->
                    flagItem.name.startsWith(searchText, ignoreCase = true)
                }
                filteredFlagItems.addAll(itemsStartingWithSearchText)

                val itemsContainingSearchText = flagItems.filter { flagItem ->
                    flagItem.name.contains(searchText, ignoreCase = true) && !filteredFlagItems.contains(flagItem)
                }
                filteredFlagItems.addAll(itemsContainingSearchText)

                val newAdapter = FlagAdapter(this@ShopActivity, filteredFlagItems, intent)
                flagListView.adapter = newAdapter
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
        println("Shop time taken: ${System.currentTimeMillis() - startTime}ms")
    }
}


class FlagAdapter(context: Context, private val flagItems: List<Flag>, private val intent: Intent) :
    ArrayAdapter<Flag>(context, 0, flagItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false)
        }

        val flagNameTextView = itemView?.findViewById<TextView>(R.id.itemName)
        val flagImageView = itemView?.findViewById<ImageView>(R.id.itemImage)
        val flagPriceTextView = itemView?.findViewById<TextView>(R.id.itemPrice)
        val flagDescriptionTextView = itemView?.findViewById<TextView>(R.id.itemDescription)
        val favouriteButton = itemView?.findViewById<AppCompatButton>(R.id.favouriteButton)
        val flagAddToCart = itemView?.findViewById<AppCompatButton>(R.id.addToCartButton)
        val flagCartLayout = itemView?.findViewById<LinearLayout>(R.id.cartControlsLayout)
        val flagMinusButton = itemView?.findViewById<AppCompatButton>(R.id.minusButton)
        val flagNumberText = itemView?.findViewById<TextView>(R.id.cartItemAmount)
        val flagPlusButton = itemView?.findViewById<AppCompatButton>(R.id.plusButton)

        val currentFlagItem = getItem(position)
        val db = FirestoreDB.getInstance()
        val username = intent.getStringExtra("username")
        val user = db.getUsers().find { it.username == username }

        if (user != null) {
            if (user.cart.find { it.name == currentFlagItem?.name } != null) {
                flagAddToCart?.visibility = View.GONE
                flagCartLayout?.visibility = View.VISIBLE
                val amount = db.getUsers().find { it.username == username }?.cart?.find { it.name == currentFlagItem?.name }?.amount
                flagNumberText?.text = amount.toString()
            } else {
                flagAddToCart?.visibility = View.VISIBLE
                flagCartLayout?.visibility = View.GONE
            }
        }

        favouriteButton?.setOnClickListener {
            Log.e("Username", "$username", )
            val flag = flagItems[position]
            Log.e("Flag", "flag: $flag", )
            if (username != null) {
                Log.d("User Patching", "Before: ${user?.favouriteFlags}")
                if (user != null && flag != null) {
                    if (user.favouriteFlags.contains(flag?.name)) {
                        user.favouriteFlags.remove(flag?.name)
                        favouriteButton?.setBackgroundResource(R.drawable.favourite_unfilled)
                    } else {
                        user.favouriteFlags.add(flag.name)
                        favouriteButton?.setBackgroundResource(R.drawable.favourite_filled)
                        Log.e("Favourites", "added flag:" + flag.name, )
                    }
                    db.patchUser(user)
                }
                Log.d("User Patching", "After: ${user?.favouriteFlags}")
            }
        }

        flagNameTextView?.text = currentFlagItem?.name
        val image = context.resources.getIdentifier(currentFlagItem?.image, "drawable", context.packageName)
        flagImageView?.setImageResource(image ?: 0)
        flagPriceTextView?.text = "$" + currentFlagItem?.price.toString()
        flagDescriptionTextView?.text = currentFlagItem?.description
        if(user !=null){
            if(user.favouriteFlags.contains(currentFlagItem?.name)){
                favouriteButton?.setBackgroundResource(R.drawable.favourite_filled)
            } else {
                favouriteButton?.setBackgroundResource(R.drawable.favourite_unfilled)
            }
        }

        flagAddToCart?.setOnClickListener(){
            if (currentFlagItem != null) {
                //Tries to update stock and add to cart
                if(db.updateStock(currentFlagItem.name,1) && db.addToCart(username!!,currentFlagItem.name)){
                    flagAddToCart.visibility = View.GONE
                    flagCartLayout?.visibility = View.VISIBLE
                    val amount = db.getUsers().find { it.username == username }?.cart?.find { it.name == currentFlagItem.name }?.amount
                    flagNumberText?.text = amount.toString()
                } else {
                    Toast.makeText(context, "Flag out of stock", Toast.LENGTH_SHORT).show()
                }
            }
        }

        flagMinusButton?.setOnClickListener(){
            if (currentFlagItem != null) {
                //Tries to update stock and add to cart
                if(db.updateStock(currentFlagItem.name,-1) && db.removeFromCart(username!!,currentFlagItem.name)){
                    val amount = db.getUsers().find { it.username == username }?.cart?.find { it.name == currentFlagItem.name }?.amount
                    flagNumberText?.text = amount.toString()
                    if(amount == 0 || amount == null){
                        flagCartLayout?.visibility = View.GONE
                        flagAddToCart?.visibility = View.VISIBLE
                    }
                }
            }
        }

        flagPlusButton?.setOnClickListener(){
            if (currentFlagItem != null) {
                //Tries to update stock and add to cart
                if(db.updateStock(currentFlagItem.name,1) && db.addToCart(username!!,currentFlagItem.name)){
                    val amount = db.getUsers().find { it.username == username }?.cart?.find { it.name == currentFlagItem.name }?.amount
                    flagNumberText?.text = amount.toString()
                } else {
                    Toast.makeText(context, "Flag out of stock", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return itemView!!
    }
}