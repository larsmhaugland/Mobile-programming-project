package com.example.flagged

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

/**
 * This activity is used to change the password of a user.
 * */
class ChangePasswordActivity : AppCompatActivity() {
    /**
     * This function is called when the activity is resumed.
     * */
    override fun onResume() {
        super.onResume()
        //Clear the fields
        val username = findViewById<EditText>(R.id.usernameInput)
        val oldPassword = findViewById<EditText>(R.id.oldPasswordInput)
        val newPassword = findViewById<EditText>(R.id.newPasswordInput)
        val confirmPassword = findViewById<EditText>(R.id.newPasswordInputConfirm)
        username.setText("")
        oldPassword.setText("")
        newPassword.setText("")
        confirmPassword.setText("")
    }

    /**
     * This function is called when the activity is created.
     *
     * @param savedInstanceState The saved instance state bundle.
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        //Get the database instance and the views
        val db = FirestoreDB.getInstance()
        val username = findViewById<EditText>(R.id.usernameInput)
        val oldPassword = findViewById<EditText>(R.id.oldPasswordInput)
        val newPassword = findViewById<EditText>(R.id.newPasswordInput)
        val confirmPassword = findViewById<EditText>(R.id.newPasswordInputConfirm)
        val submitButton = findViewById<AppCompatButton>(R.id.confirmChangePassword)

        //Set the click listener for the button to change the password
        submitButton.setOnClickListener {
            //Check if the user has filled in all the fields
            if (username.text.isEmpty() || oldPassword.text.isEmpty() || newPassword.text.isEmpty() || confirmPassword.text.isEmpty()) {
                Toast.makeText(applicationContext, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Check if the new password and the confirmation password match
            if (newPassword.text.toString() != confirmPassword.text.toString()){
                Toast.makeText(applicationContext, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Try to change the password
            if (!db.changePassword(username.text.toString(), oldPassword.text.toString(),newPassword.text.toString())){
                Toast.makeText(applicationContext, "Wrong username or password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Show a toast message and finish the activity
            Toast.makeText(applicationContext, "Password changed!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}