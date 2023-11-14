package com.example.flagged

import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class RegisterActivity : AppCompatActivity()  {
    override fun onResume() {
        super.onResume()
        val usernameInput = findViewById<EditText>(R.id.usernameEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        val emailInput = findViewById<EditText>(R.id.emailEditText)
        usernameInput.setText("")
        passwordInput.setText("")
        emailInput.setText("")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val db = FirestoreDB()
        val users = db.getUsers()
        val registerText = findViewById<TextView>(R.id.registerTextView)
        val usernameInput = findViewById<EditText>(R.id.usernameEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        val emailInput = findViewById<EditText>(R.id.emailEditText)
        val submitButton = findViewById<AppCompatButton>(R.id.submitButton)

        submitButton.setOnClickListener {
            if (usernameInput.text.isEmpty() || passwordInput.text.isEmpty() || emailInput.text.isEmpty()) {
                registerText.text = "Please fill in all fields"
                registerText.setTextColor(Color.parseColor("#FF0000"))
                return@setOnClickListener
            }
            val username = usernameInput.text.toString()
            for (user in users) {
                if(user.username == username){
                    registerText.text = "Username already exists"
                    registerText.setTextColor(Color.parseColor("#FF0000"))
                    return@setOnClickListener
                }
            }
            registerText.text = "Register new user"
            registerText.setTextColor(Color.parseColor("#000000"))
            val password = passwordInput.text.toString()
            val email = emailInput.text.toString()
            val user = User (
                username = username,
                password = password,
                email = email,
                favouriteFlags = arrayListOf(),
                cart = arrayListOf()
            )
            if(db.addUser(user).isFailure){
                return@setOnClickListener
            }
            finish()
        }
    }
}