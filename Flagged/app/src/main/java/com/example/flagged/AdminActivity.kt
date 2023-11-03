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
class AdminActivity : AppCompatActivity(){
    private lateinit var flagListView: ListView
    private lateinit var flagItems: List<Flag>
    private lateinit var filteredFlagItems : List<Flag>
    private lateinit var adapter: FlagAdapterAdmin
    private lateinit var editTextSearch: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        val db = FirestoreDB()

        editTextSearch = findViewById(R.id.searchField)
        flagListView = findViewById(R.id.itemListView)

        flagItems = db.getFlags()

        adapter = FlagAdapterAdmin(this, flagItems)
        flagListView.adapter = adapter

        val addButton = findViewById<AppCompatButton>(R.id.addButton)
        addButton.setOnClickListener {
            val intent = Intent(this, AddFlagActivity::class.java)
            startActivity(intent)
        }


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

                val newAdapter = FlagAdapterAdmin(this@AdminActivity, filteredFlagItems)
                flagListView.adapter = newAdapter
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}
class FlagAdapterAdmin(context: Context, private val flagItems: List<Flag>) :
    ArrayAdapter<Flag>(context, 0, flagItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.activity_admin_list, parent, false)
        }

        val flagNameTextView = itemView?.findViewById<TextView>(R.id.itemName)
        val flagImageView = itemView?.findViewById<ImageView>(R.id.itemImage)
        val flagPriceTextView = itemView?.findViewById<TextView>(R.id.itemPrice)
        val flagStockTextView = itemView?.findViewById<TextView>(R.id.itemStock)
        val flagDescriptionTextView = itemView?.findViewById<TextView>(R.id.itemDescription)

        val currentFlagItem = getItem(position)


        flagNameTextView?.text = currentFlagItem?.name
        val image = context.resources.getIdentifier(currentFlagItem?.image, "drawable", context.packageName)
        flagImageView?.setImageResource(image ?: 0)
        flagPriceTextView?.text = "$" + currentFlagItem?.price.toString()
        flagStockTextView?.text = "Stock:" + currentFlagItem?.stock.toString()
        flagDescriptionTextView?.text = currentFlagItem?.description


        return itemView!!
    }
}