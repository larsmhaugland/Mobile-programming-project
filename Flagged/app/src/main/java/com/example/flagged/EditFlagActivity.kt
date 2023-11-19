package com.example.flagged

import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class EditFlagActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_flag)

        val db = FirestoreDB.getInstance()

        var name = intent.getStringExtra("flagName") ?: ""
        var description = intent.getStringExtra("flagDescription") ?: ""
        var category = intent.getStringExtra("flagCategory") ?: ""
        var price = intent.getIntExtra("flagPrice", 0)
        var stock = intent.getIntExtra("flagStock", 0)
        val editStock = findViewById<EditText>(R.id.editStock)
        editStock.setText(stock.toString())
        val editPrice = findViewById<EditText>(R.id.editPrice)
        editPrice.setText(price.toString())
        val editName = findViewById<EditText>(R.id.editName)
        editName.setText(name)
        val editDescription = findViewById<EditText>(R.id.editDescription)
        editDescription.setText(description)
        val editCategory = findViewById<EditText>(R.id.editCategory)
        editCategory.setText(category)

        var flag = db.getFlags().find { it.name == name }
        if (flag == null) {
            flag = Flag(name, stock, price, description, "image", category)
        }
        val saveButton = findViewById<AppCompatButton>(R.id.saveFlagButton)
        saveButton.setOnClickListener {
            name = editName.text.toString()
            description = editDescription.text.toString()
            category = editCategory.text.toString()
            price = editPrice.text.toString().toInt()
            stock = editStock.text.toString().toInt()
            val flag = Flag(name, stock, price, description, "image", category)
            db.patchFlag(flag)
            finish()
        }

        val deleteButton = findViewById<AppCompatButton>(R.id.deleteFlagButton)
        deleteButton.setOnClickListener {

            showConfirmationDialog(object : ConfirmationCallback {
                override fun onConfirmed() {
                    // User clicked Yes, perform the deletion
                    db.deleteFlag(flag)
                    finish()
                }

                override fun onCancelled() {
                    // User clicked No or dismissed the dialog
                    // No need to do anything
                }
            })
        }
    }

    private fun showConfirmationDialog(callback: ConfirmationCallback) {
        val builder = AlertDialog.Builder(this)

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