package com.example.flagged

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView

class FavouritesActivity : AppCompatActivity() {
    /**
     *  This function is called when the activity is created.
     *  @param savedInstanceState The saved instance state bundle.
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        val db = FirestoreDB.getInstance()
        // Get the username from the intent
        val username = intent.getStringExtra("username")

        //If the favourites button is clicked, go back to the main shop activity
        val favouritesButton = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.favouritesButton)
        favouritesButton.setOnClickListener{
            finish()
        }

        val user = db.getUsers().find { it.username == username }
        val flags = db.getFlags()
        //Filter the flags to only include the favourite flags
        val favouriteFlags = flags.filter { user?.favouriteFlags?.contains(it.name) == true }
        //Populate the adapter with the favourite flags and view
        val adapter = FlagAdapter(this, favouriteFlags, intent)
        val flagListView = findViewById<ListView>(R.id.itemListView)
        flagListView.adapter = adapter
    }
}
