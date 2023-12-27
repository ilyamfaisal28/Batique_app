package com.example.batique

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.LeadingMarginSpan
import com.example.batique.databinding.ActivityAboutBinding
import com.example.batique.databinding.ActivityTutorialBinding

private lateinit var binding: ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        val actionbar = supportActionBar
        actionbar?.title = getString(R.string.cara_penggunaan)
        actionbar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textInstructions()
    }

    private fun textInstructions(){
        val listText = resources.getStringArray(R.array.steps_of_how_to_use)
        val builder = SpannableStringBuilder()
        for (i in listText.indices) {
            val line = listText[i]
            val lineNumber = (i + 1).toString()
            val numberedLine = "$lineNumber.  $line"
            val ss = SpannableString(numberedLine)
            ss.setSpan(
                LeadingMarginSpan.Standard(0, 50),
                0,
                numberedLine.length,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            builder.append(ss)
        }
        binding.information0.text = builder
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}