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

data class flagItem(val name: String, val image: Int, val description: String, val price: String)

class ShopActivity : AppCompatActivity(){
    private lateinit var flagListView: ListView
    private lateinit var flagItems: List<flagItem>
    private lateinit var adapter: FlagAdapter
    private lateinit var editTextSearch: EditText
    private var isReverseSort = false


    private fun sortAlphabetically(items: List<flagItem>): List<flagItem> {
        return items.sortedBy { it.name }
    }
    private fun sortReverseAlphabetically(items: List<flagItem>): List<flagItem> {
        return items.sortedByDescending { it.name }
    }
    private fun sortLowHighByPrice(items: List<flagItem>): List<flagItem> {
        return items.sortedBy { it.price.removePrefix("$").toDouble() }
    }
    private fun sortHighLowByPrice(items: List<flagItem>): List<flagItem> {
        return items.sortedByDescending { it.price.removePrefix("$").toDouble() }
    }


    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.filter_options, null)

        val sortAZRadioButton = dialogView.findViewById<RadioButton>(R.id.sortAZ)
        val sortZARadioButton = dialogView.findViewById<RadioButton>(R.id.sortZA)
        val sortLowHighRadioButton = dialogView.findViewById<RadioButton>(R.id.sortLowHigh)
        val sortHighLowRadioButton = dialogView.findViewById<RadioButton>(R.id.sortHighLow)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Filter Options")
            .setPositiveButton("Apply") { dialog, _ ->
                if (sortAZRadioButton.isChecked) {
                    flagItems = sortAlphabetically(flagItems)
                    isReverseSort = false
                } else if (sortZARadioButton.isChecked) {
                    flagItems = sortReverseAlphabetically(flagItems)
                    isReverseSort = true
                }else if (sortLowHighRadioButton.isChecked) {
            flagItems = sortLowHighByPrice(flagItems)
            isReverseSort = false
            } else if (sortHighLowRadioButton.isChecked) {
            flagItems = sortHighLowByPrice(flagItems)
            isReverseSort = true
            }

                val newAdapter = FlagAdapter(this@ShopActivity, flagItems)
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

        flagItems = listOf(
            flagItem("Norway", R.drawable.logo, "This is a flag", "$1.00"),
            flagItem("Sweden", R.drawable.logo, "This is a flag", "$2.00"),
            flagItem("Denmark", R.drawable.logo, "This is a flag", "$3.00"),
            flagItem("Finland", R.drawable.logo, "This is a flag", "$4.00"),
            flagItem("Faroe Islands", R.drawable.logo, "This is a flag", "$5.00"),
            flagItem("Iceland", R.drawable.logo, "This is a flag", "$6.00"),
            flagItem("Greenland", R.drawable.logo, "This is a flag", "$7.00")
        )

        adapter = FlagAdapter(this, flagItems)
        flagListView.adapter = adapter

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                val filteredFlagItems = mutableListOf<flagItem>()

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


class FlagAdapter(context: Context, private val flagItems: List<flagItem>) :
    ArrayAdapter<flagItem>(context, 0, flagItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false)
        }

        val flagNameTextView = itemView?.findViewById<TextView>(R.id.itemName)
        val flagImageView = itemView?.findViewById<ImageView>(R.id.itemImage)
        val flagPriceTextView = itemView?.findViewById<TextView>(R.id.itemPrice)

        val currentFlagItem = getItem(position)

        flagNameTextView?.text = currentFlagItem?.name
        flagImageView?.setImageResource(currentFlagItem?.image ?: 0)
        flagPriceTextView?.text = currentFlagItem?.price

        return itemView!!
    }
}