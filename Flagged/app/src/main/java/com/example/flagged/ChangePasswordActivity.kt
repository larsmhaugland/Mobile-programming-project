package com.example.flagged

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class ChangePasswordActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        val username = findViewById<EditText>(R.id.usernameInput)
        val oldPassword = findViewById<EditText>(R.id.oldPasswordInput)
        val newPassword = findViewById<EditText>(R.id.newPasswordInput)
        val confirmPassword = findViewById<EditText>(R.id.newPasswordInputConfirm)
        username.setText("")
        oldPassword.setText("")
        newPassword.setText("")
        confirmPassword.setText("")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val db = FirestoreDB.getInstance()
        val username = findViewById<EditText>(R.id.usernameInput)
        val oldPassword = findViewById<EditText>(R.id.oldPasswordInput)
        val newPassword = findViewById<EditText>(R.id.newPasswordInput)
        val confirmPassword = findViewById<EditText>(R.id.newPasswordInputConfirm)
        val submitButton = findViewById<AppCompatButton>(R.id.confirmChangePassword)

        submitButton.setOnClickListener {
            if (newPassword.text.toString() != confirmPassword.text.toString()){
                Toast.makeText(applicationContext, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!db.changePassword(username.text.toString(), oldPassword.text.toString(),newPassword.text.toString())){
                Toast.makeText(applicationContext, "Wrong username or password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(applicationContext, "Password changed!", Toast.LENGTH_SHORT).show()
            finish()
        }



    }
}