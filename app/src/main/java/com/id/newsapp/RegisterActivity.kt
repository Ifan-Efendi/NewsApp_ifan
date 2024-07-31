package com.id.newsapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var name: EditText
    private lateinit var btnRegistrasi: Button
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        FirebaseApp.initializeApp(this)
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        email = findViewById(R.id.username_edittext)
        password = findViewById(R.id.password_edittext)
        name = findViewById(R.id.name)
        progressBar = findViewById(R.id.progressBar)
        btnRegistrasi = findViewById(R.id.register_button)
        btnRegistrasi.setOnClickListener {
            val user = email.text.toString().trim()
            val pass = password.text.toString().trim()
            val nameUser = name.text.toString().trim()

            if (TextUtils.isEmpty(nameUser)) {
                name.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(user)) {
                email.error = "Email tidak boleh kosong"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(pass)) {
                password.error = "Password tidak boleh kosong"
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE

            mAuth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener {
                progressBar.visibility = View.GONE
                if (it.isSuccessful) {
                    val userId = mAuth.currentUser?.uid
                    val userMap = hashMapOf(
                        "name" to nameUser,
                        "email" to user
                    )

                    userId?.let { it1 ->
                        db.collection("users").document(it1).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Register Berhasil", Toast.LENGTH_SHORT).show()
                                val intent = Intent(applicationContext, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }else{
                    if(it.exception?.message!!.contains("already in use")){
                        val userMap = hashMapOf(
                            "name" to nameUser,
                            "email" to user
                        )
                        db.collection("users").add(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Register Berhasil", Toast.LENGTH_SHORT).show()
                                val intent = Intent(applicationContext, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }else {
                        Toast.makeText(
                            this,
                            "Register Gagal: ${it.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}