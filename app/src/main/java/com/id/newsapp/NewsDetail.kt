package com.id.newsapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class NewsDetail : AppCompatActivity() {
    private lateinit var newsTitle: TextView
    private lateinit var newsSubTitle: TextView
    private lateinit var newsImage: ImageView

    private lateinit var edit: Button
    private lateinit var hapus: Button
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_news_detail)

        newsTitle = findViewById(R.id.newsTitle)
        newsSubTitle = findViewById(R.id.newsSubtitle)
        newsImage = findViewById(R.id.newsImage)
        edit = findViewById(R.id.editButton)
        hapus = findViewById(R.id.deleteButton)
        db = FirebaseFirestore.getInstance()

        // Get data from Intent
        val intent = intent
        val id = intent.getStringExtra("id")
        val title = intent.getStringExtra("title")
        val subTitle = intent.getStringExtra("desc")
        val imageUrl = intent.getStringExtra("imageUrl")
        // Set data to UI component
        newsTitle.text = title
        newsSubTitle.text = subTitle
        if(!imageUrl.isNullOrEmpty()){
            Picasso.get()
                .load(imageUrl)
                .fit()
                .centerCrop()
                .error(R.drawable.samplenews)
                .into(newsImage)
        }


        edit.setOnClickListener {
            val editIntent = Intent(this, NewsAdd::class.java).apply {
                putExtra("id", id)
                putExtra("title", title)
                putExtra("desc", subTitle)
                putExtra("imageUrl", imageUrl)
            }
            startActivity(editIntent)
        }

        hapus.setOnClickListener {
            id?.let { documentId ->
                db.collection("news").document(documentId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "News deleted successfully", Toast.LENGTH_SHORT).show()
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(mainIntent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error deleting news: " + e.message, Toast.LENGTH_SHORT).show()
                        Log.w("NewsDetail", "Error deleting document", e)
                    }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}