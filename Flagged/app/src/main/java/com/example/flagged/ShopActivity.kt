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

var Username = ""       //Global variable to store the username of the user

/**
 * This activity is used to display the shop to the user.
 *
 * @property flagItems A list of flag items.
 * @property filteredFlagItems A list of flag items that have been filtered.
 * @property adapter The adapter for the list of flag items.
 * @property isReverseSort A boolean that indicates whether the list of flag items is sorted in reverse.
 * @property editTextSearch The search bar.
 * @property flagListView The list of flag items.
 * */
class ShopActivity : AppCompatActivity(){
    private lateinit var flagListView: ListView
    private lateinit var flagItems: List<Flag>
    private lateinit var filteredFlagItems : List<Flag>
    private lateinit var adapter: FlagAdapter
    private lateinit var editTextSearch: EditText
    private var isReverseSort = false

    /**
     *  Sort a list alphabetically
     *  @param items A list of flag items
     *  @return A list of flag items
     * */
    private fun sortAlphabetically(items: List<Flag>): List<Flag> {
        return items.sortedBy { it.name }
    }
    /**
     *  Sort a list reverse alphabetically
     *  @param items A list of flag items
     *  @return A list of flag items
     * */
    private fun sortReverseAlphabetically(items: List<Flag>): List<Flag> {
        return items.sortedByDescending { it.name }
    }
    /**
     *  Sort a list by price from low to high
     *  @param items A list of flag items
     *  @return A list of flag items
     * */
    private fun sortLowHighByPrice(items: List<Flag>): List<Flag> {
        return items.sortedBy { it.price.toDouble() }
    }
    /**
     *  Sort a list by price from high to low
     *  @param items A list of flag items
     *  @return A list of flag items
     * */
    private fun sortHighLowByPrice(items: List<Flag>): List<Flag> {
        return items.sortedByDescending { it.price.toDouble() }
    }
    /**
     *  Filter a list by category
     *  @param items A list of flag items
     *  @param category The category to filter by
     *  @return A list of flag items
     * */
    private fun filterByCategory(items: List<Flag>, category: String): List<Flag> {
        return items.filter { it.category == category }
    }

    /**
     *  Filter pop-up dialog
     *  Allows the user to filter the flags by category or sort them by name or price
     * */
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
        val categoryCountyCityRadioButton = dialogView.findViewById<RadioButton>(R.id.categoryCountyCity)

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
                }else if (categoryCountyCityRadioButton.isChecked){
                    filteredFlagItems = filterByCategory(flagItems,"County/City")
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

    /**
     *    On resume, the list of flags is updated
     * */
    override fun onResume() {
        super.onResume()
        val db = FirestoreDB.getInstance()
        flagItems = db.getFlags()
        filteredFlagItems = flagItems

        adapter = FlagAdapter(this, flagItems, intent)
        flagListView.adapter = adapter

    }

    /**
     *  On create, the list of flags is retrieved from the database and displayed
     *
     *  @param savedInstanceState: the saved instance state of the activity
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

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
            /**
             * This function is called before the text is changed.
             * @param s The text before it is changed.
             * @param start The start index of the text.
             * @param count The number of characters that will be changed.
             * @param after The number of characters in the text after the change occurs.
             * */
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            /**
             * This function is called when the text is changed.
             * @param s The text after it is changed.
             * @param start The start index of the text.
             * @param before The number of characters that were changed.
             * @param count The number of characters in the text after the change occurs.
             * */
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

            /**
             * This function is called after the text is changed.
             * @param s The text after it is changed.
             * */
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}


class FlagAdapter(context: Context, private val flagItems: List<Flag>, private val intent: Intent) :
    ArrayAdapter<Flag>(context, 0, flagItems) {
/**
 *    Get the view for each flag item in the list and populate the shop with them
 *    Also handles the favourite button and the add to cart button
 *
 *    @param position: the position of the flag in the list
 *    @param convertView: the view of the flag
 *    @param parent: the parent view
 *    @return: the view of the flag
 * */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false)
        }
        //Get the views from the layout
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

        //Get the current flag item
        val currentFlagItem = getItem(position)
        //Get the current user
        val db = FirestoreDB.getInstance()
        val username = intent.getStringExtra("username")
        var user = db.getUsers().find { it.username == username }
        //If the user is logged in, check if the flag is in the users cart
        if (user != null) {
            //If the flag is in the users cart, show the cart controls, otherwise show the add to cart button
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
        //Change the look of the favourite button depending on whether a flag is in the users favourites
        favouriteButton?.setOnClickListener {
            Log.e("Username", "$username", )
            val flag = flagItems[position]          //Get the specific flag whose button is being pressed
            Log.d("Flag", "flag: $flag", )
            if (username != null) {
                user = db.getUsers().find { it.username == username }   //Get the user from the database

                    //If the flag is already in the users favourites, remove it, otherwise add it
                    //Change the drawable resource to reflect the change
                    if (user!!.favouriteFlags.contains(flag.name)) {
                        user!!.favouriteFlags.remove(flag.name)
                        favouriteButton.setBackgroundResource(R.drawable.favourite_unfilled)
                    } else {
                        user!!.favouriteFlags.add(flag.name)
                        favouriteButton.setBackgroundResource(R.drawable.favourite_filled)
                        Log.d("Favourites", "added flag:" + flag.name, )
                    }
                    db.patchUser(user!!)      //Send the updated user data to the database
            }
        }

        flagNameTextView?.text = currentFlagItem?.name
        //Get the specific flag image from the drawable folder
        val image = context.resources.getIdentifier(currentFlagItem?.image, "drawable", context.packageName)
        flagImageView?.setImageResource(image ?: 0)
        flagPriceTextView?.text = "$" + currentFlagItem?.price.toString()
        flagDescriptionTextView?.text = currentFlagItem?.description
        //Set the favourites button when the view is originally created
        if(user !=null){
            //If the flag is in the users favourites, set the drawable to filled, otherwise set it to unfilled
            if(user!!.favouriteFlags.contains(currentFlagItem?.name)){
                favouriteButton?.setBackgroundResource(R.drawable.favourite_filled)
            } else {
                favouriteButton?.setBackgroundResource(R.drawable.favourite_unfilled)
            }
        }

        flagAddToCart?.setOnClickListener(){
            if (currentFlagItem != null) {
                //Tries to update stock and add to cart
                if(db.updateStock(currentFlagItem.name,1)){
                    if (db.addToCart(username!!,currentFlagItem.name)) {
                        //Update text on screen
                        flagAddToCart.visibility = View.GONE
                        flagCartLayout?.visibility = View.VISIBLE
                        val amount = db.getUsers()
                            .find { it.username == username }?.cart?.find { it.name == currentFlagItem.name }?.amount
                        flagNumberText?.text = amount.toString()
                    } else {
                        //If flag couldn't be added to the cart display message
                        Toast.makeText(context, "Flag out of stock", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    //If flag is out of stock display message
                    Toast.makeText(context, "Flag out of stock", Toast.LENGTH_SHORT).show()
                }
            }
        }

        flagMinusButton?.setOnClickListener(){
            if (currentFlagItem != null) {
                //Tries to update stock and add to cart
                if(db.updateStock(currentFlagItem.name,-1) && db.removeFromCart(username!!,currentFlagItem.name)){
                    //Update text on screen
                    val amount = db.getUsers().find { it.username == username }?.cart?.find { it.name == currentFlagItem.name }?.amount
                    flagNumberText?.text = amount.toString()
                    //If amount is 0, hide cart controls and show add to cart button
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
                if(db.updateStock(currentFlagItem.name,1)){
                    if (db.addToCart(username!!,currentFlagItem.name)) {
                        //Update text on screen
                        val amount = db.getUsers()
                            .find { it.username == username }?.cart?.find { it.name == currentFlagItem.name }?.amount
                        flagNumberText?.text = amount.toString()
                    } else {
                        //If flag couldn't be added to the cart display message
                        Toast.makeText(context, "Flag out of stock", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    //If flag is out of stock display message
                    Toast.makeText(context, "Flag out of stock", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return itemView!!
    }
}