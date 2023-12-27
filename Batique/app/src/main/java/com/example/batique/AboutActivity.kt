package com.example.batique

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.batique.databinding.ActivityAboutBinding


class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding
    private val myEmail = "ilyam.faisal28@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val actionbar = supportActionBar
        actionbar?.title = getString(R.string.about)
        actionbar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.gmail.setOnClickListener {
            myEmail.composeEmail()
        }

        binding.github.setOnClickListener {
            val githubUrl = "https://github.com/ilyamfaisal28"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
            startActivity(intent)
        }

        binding.linkedin.setOnClickListener {
            val linkedinUrl = "https://linkedin.com/in/ilyam-faisal"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkedinUrl))
            startActivity(intent)
        }
    }

    private fun String.composeEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(this@composeEmail))
        }
        if (intent.resolveActivity(packageManager) != null){
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}