package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class AddFlagActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_flag)

        val db = FirestoreDB()

        val flagNameInput = findViewById<EditText>(R.id.nameInput)
        val flagPriceInput = findViewById<EditText>(R.id.priceInput)
        val flagStockInput = findViewById<EditText>(R.id.stockInput)
        val flagDescriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val flagCategoryInput = findViewById<Spinner>(R.id.categorySpinner)
        val addButton = findViewById<AppCompatButton>(R.id.addButton)

        addButton.setOnClickListener{
            val name = flagNameInput.text.toString()
            val price = flagPriceInput.text.toString()
            val stock = flagStockInput.text.toString()
            val description = flagDescriptionInput.text.toString()
            val category = flagCategoryInput.selectedItem.toString()

            val flag = Flag(
                name = name,
                price = price,
                stock = stock,
                description = description,
                category = category,
                image = " "
            )
            db.addFlag(flag)

        }

    }
}