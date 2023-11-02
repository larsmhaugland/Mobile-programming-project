package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
data class ShoppingCartItem(val name: String, var amount: Int)

var shoppingCart = mutableListOf<ShoppingCartItem>()


class ShopActivity : AppCompatActivity(){
    private lateinit var flagListView: ListView
    private lateinit var flagItems: List<Flag>
    private lateinit var filteredFlagItems : List<Flag>
    private lateinit var adapter: FlagAdapter
    private lateinit var editTextSearch: EditText
    private var isReverseSort = false
    private var shoppingCart = listOf<ShoppingCartItem>()



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
        val adminState = intent.getBooleanExtra("admin", false)
        val username = intent.getStringExtra("username")
        val db = FirestoreDB()

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

        val currentFlagItem = getItem(position)

        favouriteButton?.setOnClickListener {
            val db = FirestoreDB()
            val username = intent.getStringExtra("username")
            Log.e("Username", "$username", )
            val flag = flagItems[position]
            Log.e("Flag", "flag: $flag", )
            if (username != null) {
                val user = db.getUsers().find { it.username == username }
                Log.d("User Patching", "Before: ${user?.favouriteFlags}")
                if (user != null && flag != null) {
                    if (user.favouriteFlags.contains(flag?.name)) {
                        user.favouriteFlags.remove(flag?.name)
                    } else {
                        user.favouriteFlags.add(flag.name)
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

        flagAddToCart?.setOnClickListener(){
            val db = FirestoreDB()
            if (currentFlagItem != null) {
                if(db.updateStock(currentFlagItem.name,1) != ""){
                    var flag = shoppingCart.find(){it.name === currentFlagItem.name}
                    if(flag === null){
                        flag = ShoppingCartItem(
                            currentFlagItem.name,
                            1
                        )
                        shoppingCart.add(flag)
                    } else {
                        flag.amount += 1
                        shoppingCart[shoppingCart.indexOf(flag)] = flag
                    }
                }
            }
        }

        return itemView!!
    }
}