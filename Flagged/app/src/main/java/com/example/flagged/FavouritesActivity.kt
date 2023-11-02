package com.example.flagged

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView

class FavouritesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        val db = FirestoreDB()
        val username = intent.getStringExtra("username")

        val user = db.getUsers().find { it.username == username }
        val flags = db.getFlags()
        val favouriteFlags = flags.filter { user?.favouriteFlags?.contains(it.name) == true }

        val adapter = FlagAdapter(this, favouriteFlags, intent)
        val flagListView = findViewById<ListView>(R.id.itemListView)
        flagListView.adapter = adapter
    }
}
