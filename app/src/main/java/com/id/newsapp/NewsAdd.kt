package com.id.newsapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class NewsAdd : AppCompatActivity() {

    private var id: String? = null
    private var judul: String? = null
    private var deskripsi: String? = null
    private var image: String? = null

    private lateinit var title: EditText
    private lateinit var desc: EditText
    private lateinit var imageView: ImageView
    private lateinit var saveNews: Button
    private lateinit var chooseImage: Button

    private lateinit var imageUri: Uri

    private lateinit var dbNews: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_news)

        dbNews = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        title = findViewById(R.id.title)
        desc = findViewById(R.id.desc)
        imageView = findViewById(R.id.imageView)
        saveNews = findViewById(R.id.btnAdd)
        chooseImage = findViewById(R.id.btnChooseImage)

        val updateOption = intent
        if (updateOption != null) {
            id = updateOption.getStringExtra("id")
            judul = updateOption.getStringExtra("title")
            deskripsi = updateOption.getStringExtra("desc")
            image = updateOption.getStringExtra("imageUrl")

            title.setText(judul)
            desc.setText(deskripsi)
            if(!image.isNullOrEmpty()) {
                Picasso.get()
                    .load(image)
                    .fit()
                    .centerCrop()
                    .error(R.drawable.samplenews)
                    .into(imageView)
            }
        }

        progressDialog = ProgressDialog(this).apply {
            setTitle("Loading....")
        }

        chooseImage.setOnClickListener {
            openFileChooser()
        }

        saveNews.setOnClickListener {
            val newsTitle = title.text.toString().trim()
            val newsDesc = desc.text.toString().trim()

            if (newsTitle.isEmpty() || newsDesc.isEmpty()) {
                Toast.makeText(
                    this@NewsAdd,
                    "Title and Description cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            progressDialog.show()
            uploadImageToStorage(newsTitle, newsDesc)
        }
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadImageToStorage(newsTitle: String, newsDesc: String) {
        imageUri.let { uri ->
            val storageRef =
                storage.reference.child("news_image_ifan/" + System.currentTimeMillis() + ".jpg")
            uri.let {
                storageRef.putFile(it).addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val imageUrl = downloadUri.toString()
                        saveData(newsTitle, newsDesc, imageUrl)
                    }
                }.addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed To Upload Image ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun saveData(newsTitle: String, newsDesc: String, imageUrl: String) {
        val news = HashMap<String, Any>()
        news["title"] = newsTitle
        news["desc"] = newsDesc
        news["image"] = imageUrl

        if (id != null) {
            dbNews.collection("news").document(id ?: "")
                .update(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "News Updated Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "News updating news: " + e.message, Toast.LENGTH_SHORT)
                        .show()
                    Log.w("NewsAdd", "Error updating document", e)
                }
        } else {
            dbNews.collection("news")
                .add(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this@NewsAdd, "News added successfully", Toast.LENGTH_SHORT)
                        .show()
                    title.setText("")
                    desc.setText("")
                    imageView.setImageResource(0) // Clear the imageView
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewsAdd,
                        "Error adding news : &{e.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.w("NewsAdd", "Error adding document", e)
                }
        }
    }
}