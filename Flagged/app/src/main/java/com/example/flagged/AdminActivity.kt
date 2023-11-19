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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
class AdminActivity : AppCompatActivity(){
    private lateinit var flagListView: ListView
    private lateinit var flagItems: List<Flag>
    private lateinit var adapter: FlagAdapterAdmin
    private lateinit var editTextSearch: EditText
    override fun onResume() {
        super.onResume()
        val db = FirestoreDB.getInstance()
        flagItems = db.getFlags()
        adapter = FlagAdapterAdmin(this, flagItems)
        flagListView.adapter = adapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        val db = FirestoreDB.getInstance()

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
            itemView =
                LayoutInflater.from(context).inflate(R.layout.activity_admin_list, parent, false)
        }
        val db = FirestoreDB.getInstance()
        val flagNameTextView = itemView?.findViewById<TextView>(R.id.itemName)
        val flagImageView = itemView?.findViewById<ImageView>(R.id.itemImage)
        val flagPriceTextView = itemView?.findViewById<TextView>(R.id.itemPrice)
        val flagStockTextView = itemView?.findViewById<TextView>(R.id.itemStock)
        val flagDescriptionTextView = itemView?.findViewById<TextView>(R.id.itemDescription)

        val currentFlagItem = getItem(position)

        itemView?.findViewById<Button>(R.id.editFlagButton)?.setOnClickListener() {
            val intent = Intent(context, EditFlagActivity::class.java)
            intent.putExtra("flagName", currentFlagItem?.name)
            intent.putExtra("flagPrice", currentFlagItem?.price)
            intent.putExtra("flagStock", currentFlagItem?.stock)
            intent.putExtra("flagDescription", currentFlagItem?.description)
            intent.putExtra("flagCategory", currentFlagItem?.category)
            context.startActivity(intent)
        }
        flagNameTextView?.text = currentFlagItem?.name
        val image =
            context.resources.getIdentifier(currentFlagItem?.image, "drawable", context.packageName)
        flagImageView?.setImageResource(image ?: 0)
        flagPriceTextView?.text = "$" + currentFlagItem?.price.toString()
        flagStockTextView?.text = "Stock:" + currentFlagItem?.stock.toString()
        flagDescriptionTextView?.text = currentFlagItem?.description

        val editFlagButton = itemView?.findViewById<AppCompatButton>(R.id.editFlagButton)
        editFlagButton?.setOnClickListener {
            val intent = Intent(context, EditFlagActivity::class.java)
            intent.putExtra("flagName", currentFlagItem?.name)
            context.startActivity(intent)
        }
        val deleteFlagButton = itemView?.findViewById<AppCompatButton>(R.id.deleteButton)
        deleteFlagButton?.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete Flag")
            builder.setMessage("Are you sure you want to delete this flag?")
            builder.setPositiveButton("Yes") { _, _ ->
                if(currentFlagItem != null){
                    db.deleteFlag(currentFlagItem)
                    flagItems.toMutableList().remove(currentFlagItem)
                    notifyDataSetChanged()
                }
            }
            builder.setNegativeButton("No") { _, _ ->
            }
            builder.show()
        }

        return itemView!!
    }
}