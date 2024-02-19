package com.example.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.lib1.Lib1
import com.example.lib2.LibActivityUtils
import com.example.lib2.LibCallback
import com.example.sample.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() , LibCallback {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        Lib1().print()
        Timber.e("trong","fafasfaf")
        Timber.tag("Trong").e("Hihi")
        binding.btnOpenLib.setOnClickListener {
            LibActivityUtils.startSecondActivity(this,this)
        }
    }

    override fun onLibResult(data: String) {
        Toast.makeText(this,data,Toast.LENGTH_LONG).show()
    }
}