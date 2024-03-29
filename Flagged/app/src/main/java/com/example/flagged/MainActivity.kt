package com.example.flagged

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

/**
 * This activity is used to login to the application.
 */
class MainActivity : AppCompatActivity() {
    /**
     *  This function is called when the activity is resumed.
     * */
    override fun onResume() {
        super.onResume()
        val usernameInput = findViewById<EditText>(R.id.usernameEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        //Clear the username and password fields
        usernameInput.setText("")
        passwordInput.setText("")
    }

    /**
     *  This function is called when the activity is created.
     *  @param savedInstanceState The saved instance state.
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Get the elements from the layout
        val submitButton = findViewById<Button>(R.id.submitButton)
        val registerButton = findViewById<TextView>(R.id.signUpTextView)
        val changePasswordButton = findViewById<TextView>(R.id.changePasswordTextView)
        val usernameInput = findViewById<EditText>(R.id.usernameEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        val db = FirestoreDB.getInstance()

        //Set the click listener for the button to register a user
        registerButton.setOnClickListener {
            val intent= Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
        //Set the click listener for the button to change the password
        changePasswordButton.setOnClickListener {
            val intent= Intent(this,ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        //Set the click listener for the button to login
        submitButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()
            //Check if the user exists in the database
            if(!db.authUser(username,password)){
                //If the user does not exist, show a toast message and return
                Toast.makeText(applicationContext, "Wrong username or password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // If the user is admin, go to admin activity, else go to shop activity.
            if(username == "admin"){
                val intent= Intent(this,AdminActivity::class.java)
                startActivity(intent)
            }else{
            val intent= Intent(this,ShopActivity::class.java)
                //Send the username to the next activity
            intent.putExtra("username",username)
            startActivity(intent)
            }
        }
    }
}