package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton

class AddFlagActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_flag)

        val db = FirestoreDB.getInstance()

        val flagNameInput = findViewById<EditText>(R.id.nameInput)
        val flagPriceInput = findViewById<EditText>(R.id.priceInput)
        val flagStockInput = findViewById<EditText>(R.id.stockInput)
        val flagDescriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val flagCategoryInput = findViewById<Spinner>(R.id.categorySpinner)

        val categories = arrayOf("Country", "Naval", "County/City", "Limited Edition", "LGBTQ+")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        flagCategoryInput.adapter = adapter

        val addButton = findViewById<AppCompatButton>(R.id.addButton)

        addButton.setOnClickListener{
            val name = flagNameInput.text.toString()
            val price = flagPriceInput.text.toString().toIntOrNull() ?: 0
            val stock = flagStockInput.text.toString().toIntOrNull() ?: 0
            val description = flagDescriptionInput.text.toString()
            val category = flagCategoryInput.selectedItem.toString()

            val flag = Flag(
                name = name,
                price = price,
                stock = stock,
                description = description,
                category = category,
                image = name.lowercase()
            )
            db.addFlag(flag)
            finish()
        }

    }
}