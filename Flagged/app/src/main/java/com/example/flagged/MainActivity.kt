package com.example.flagged

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        val usernameInput = findViewById<EditText>(R.id.usernameEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        usernameInput.setText("")
        passwordInput.setText("")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val submitButton = findViewById<Button>(R.id.submitButton)
        val registerButton = findViewById<TextView>(R.id.signUpTextView)
        val usernameInput = findViewById<EditText>(R.id.usernameEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        val db = FirestoreDB()

        registerButton.setOnClickListener {
            val intent= Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }

        submitButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            if(!db.authUser(username,password)){
                return@setOnClickListener
            }
            if(username == "admin"){
                val intent= Intent(this,AdminActivity::class.java)
                startActivity(intent)
            }else{
            val intent= Intent(this,ShopActivity::class.java)
            intent.putExtra("username",username)
            startActivity(intent)
            }
        }

    }
}