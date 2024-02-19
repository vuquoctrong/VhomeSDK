package com.example.lib2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.lib2.databinding.ActivityMain4Binding

class MainActivity3 : AppCompatActivity() {
    private lateinit var binding: ActivityMain4Binding

    companion object {
        private var onDataChangeListener: LibCallback? = null

        fun setOnDataChangeListener(listener: LibCallback) {
            onDataChangeListener = listener
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain4Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.btnEnd.setOnClickListener {
            Log.e("Trong","binding.btnEnd.")
            onDataChangeListener?.onLibResult("Trong hihi")
            finish()
        }
    }
}