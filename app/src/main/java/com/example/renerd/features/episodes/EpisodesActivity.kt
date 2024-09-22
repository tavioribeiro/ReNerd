package com.example.renerd.features.episodes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.renerd.databinding.ActivityEpisodesBinding



class EpisodesActivity: AppCompatActivity() {

    private lateinit var binding: ActivityEpisodesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEpisodesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}