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

                val newAdapter = FlagAdapter(this@ShopActivity, filteredFlagItems)
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
        val db = FirestoreDB()

        editTextSearch = findViewById(R.id.searchField)
        flagListView = findViewById(R.id.itemListView)

        val favouritesButton = findViewById<Button>(R.id.favouritesButton)
        favouritesButton.setOnClickListener {
            val intent= Intent(this,FavouritesActivity::class.java)
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

        adapter = FlagAdapter(this, flagItems)
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

                val newAdapter = FlagAdapter(this@ShopActivity, filteredFlagItems)
                flagListView.adapter = newAdapter
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}


class FlagAdapter(context: Context, private val flagItems: List<Flag>) :
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

        val currentFlagItem = getItem(position)

        flagNameTextView?.text = currentFlagItem?.name
        val image = context.resources.getIdentifier(currentFlagItem?.image, "drawable", context.packageName)
        flagImageView?.setImageResource(image ?: 0)
        flagPriceTextView?.text = "$" + currentFlagItem?.price.toString()
        flagDescriptionTextView?.text = currentFlagItem?.description

        return itemView!!
    }
}