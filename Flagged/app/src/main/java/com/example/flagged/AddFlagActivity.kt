package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton

class AddFlagActivity : AppCompatActivity() {
    /**
     *  This function is called when the activity is created.
     *  @param savedInstanceState The saved instance state.
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_flag)

        val db = FirestoreDB.getInstance()
        //Retrieving all the layout elements
        val flagNameInput = findViewById<EditText>(R.id.nameInput)
        val flagPriceInput = findViewById<EditText>(R.id.priceInput)
        val flagStockInput = findViewById<EditText>(R.id.stockInput)
        val flagDescriptionInput = findViewById<EditText>(R.id.descriptionInput)
        val flagCategoryInput = findViewById<Spinner>(R.id.categorySpinner)
        val addButton = findViewById<AppCompatButton>(R.id.addButton)

        //Populating the spinner with the categories
        val categories = arrayOf("Country", "Naval", "County/City", "Limited Edition", "LGBTQ+")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        flagCategoryInput.adapter = adapter

        //Adding the flag to the database
        addButton.setOnClickListener{
            //Converting the input to the correct types
            val name = flagNameInput.text.toString()
            val price = flagPriceInput.text.toString().toIntOrNull() ?: 0
            val stock = flagStockInput.text.toString().toIntOrNull() ?: 0
            val description = flagDescriptionInput.text.toString()
            val category = flagCategoryInput.selectedItem.toString()

            //Creating the flag object and populate it with the data
            val flag = Flag(
                name = name,
                price = price,
                stock = stock,
                description = description,
                category = category,
                image = name.lowercase(),
            )
            db.addFlag(flag)
            finish()
        }

    }
}