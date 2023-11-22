package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import android.content.Context
import android.content.DialogInterface
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
    /**
     *  This function is called when the activity is resumed.
     * */
    override fun onResume() {
        super.onResume()
        val db = FirestoreDB.getInstance()
        flagItems = db.getFlags()
        adapter = FlagAdapterAdmin(this, flagItems)
        flagListView.adapter = adapter
    }

    /**
     *  This function is called when the activity is created.
     * */
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
            /** This function is called when the user types in the search field.
            *  @param s The text in the search field.
             *  @param start The start index of the text.
             *  @param before The length of the text before the change.
             *  @param count The length of the text after the change.
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

                val newAdapter = FlagAdapterAdmin(this@AdminActivity, filteredFlagItems)
                flagListView.adapter = newAdapter
            }
            /**
             *  This function is called after the user types in the search field.
             *  @param s Editable text in the search field.
             * */
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }
}
class FlagAdapterAdmin(context: Context, private val flagItems: List<Flag>) :
    ArrayAdapter<Flag>(context, 0, flagItems) {

    /**
     *  Get the view for each item in the list.
     *  @param position The position of the item in the list.
     *  @param convertView The view of the item.
     *  @param parent The parent view.
     *  @return The view of the item.
     * */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView =
                LayoutInflater.from(context).inflate(R.layout.activity_admin_list, parent, false)
        }

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

        val db = FirestoreDB.getInstance()

        val deleteButton = itemView?.findViewById<AppCompatButton>(R.id.deleteButton)
        deleteButton?.setOnClickListener {
            showConfirmationDialog(object : FlagAdapterAdmin.ConfirmationCallback {
                override fun onConfirmed() {
                    // User clicked Yes, perform the deletion
                    db.deleteFlag(currentFlagItem!!)
                    notifyDataSetChanged()
                }

                override fun onCancelled() {
                    // User clicked No or dismissed the dialog
                    // No need to do anything
                }
            })
        }

        return itemView!!
    }
    /**
     *  Show a confirmation dialog.
     *  @param callback The callback to be called when the user confirms or cancels the deletion.
     * */
    private fun showConfirmationDialog(callback: ConfirmationCallback) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete the flag?")
            .setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
                // User clicked Yes, perform the deletion
                callback.onConfirmed()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog: DialogInterface, _: Int ->
                // User clicked No, do nothing or handle accordingly
                callback.onCancelled()
                dialog.dismiss()
            }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()

    }

    interface ConfirmationCallback {
        fun onConfirmed()
        fun onCancelled()
    }

}