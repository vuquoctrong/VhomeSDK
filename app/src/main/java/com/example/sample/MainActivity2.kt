package com.example.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.lib2.LibCallback

class MainActivity2 : AppCompatActivity(),LibCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

    override fun onLibResult(data: String) {

    }
}