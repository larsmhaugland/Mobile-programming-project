package com.example.flagged

import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

/**
 * This activity is used to register a new user.
 */
class RegisterActivity : AppCompatActivity()  {
    /**
     *  This function is called when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        val usernameInput = findViewById<EditText>(R.id.usernameEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        val emailInput = findViewById<EditText>(R.id.emailEditText)
        usernameInput.setText("")
        passwordInput.setText("")
        emailInput.setText("")
    }

    /**
     * This function is called when the activity is created.
     * @param savedInstanceState The saved instance state bundle.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Get the users from the database
        val db = FirestoreDB.getInstance()
        val users = db.getUsers()
        //Get the views
        val registerText = findViewById<TextView>(R.id.registerTextView)
        val usernameInput = findViewById<EditText>(R.id.usernameEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        val emailInput = findViewById<EditText>(R.id.emailEditText)
        val submitButton = findViewById<AppCompatButton>(R.id.submitButton)

        //Set the submit button to register a new user
        submitButton.setOnClickListener {
            //Check if the user has filled in all the fields
            if (usernameInput.text.isEmpty() || passwordInput.text.isEmpty() || emailInput.text.isEmpty()) {
                registerText.text = "Please fill in all fields"
                registerText.setTextColor(Color.parseColor("#FF0000"))
                return@setOnClickListener
            }
            //Check if the username already exists
            val username = usernameInput.text.toString()
            for (user in users) {
                if(user.username == username){
                    registerText.text = "Username already exists"
                    registerText.setTextColor(Color.parseColor("#FF0000"))
                    return@setOnClickListener
                }
            }
            //Reset the text
            registerText.text = "Register new user"
            registerText.setTextColor(Color.parseColor("#000000"))
            //Add the user to the database
            val password = passwordInput.text.toString()
            val email = emailInput.text.toString()
            val user = User (
                username = username,
                password = password,
                email = email,
                favouriteFlags = arrayListOf(),
                cart = arrayListOf()
            )
            //Check if the user was added successfully
            if(!db.addUser(user)){
                Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            finish()
        }
    }
}