package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton

class EditFlagActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_flag)
        val flagName = intent.getStringExtra("flagname");

        var checkStock = findViewById<CheckBox>(R.id.checkStock)
        var checkPrice = findViewById<CheckBox>(R.id.checkPrice)
        var editStockInput = findViewById<EditText>(R.id.editStock)
        var editPriceInput = findViewById<EditText>(R.id.editPrice)

        val db = FirestoreDB.getInstance()

        val submitButton = findViewById<AppCompatButton>(R.id.submitButton)
        submitButton.setOnClickListener{
            if(checkStock.isChecked && flagName != null){
                db.updateStock(flagName,editStockInput.text.toString().toInt())
            }
            if(checkPrice.isChecked && flagName != null){
                db.updatePrice(flagName,editPriceInput.text.toString().toInt())
            }
            finish()
        }


    }
}