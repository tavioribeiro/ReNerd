package com.example.renerd.features.player

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.renerd.databinding.ActivityPlayerBinding

class PlayerActivity: AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}