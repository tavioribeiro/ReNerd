package com.example.renerd.features.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import coil.load
import com.example.renerd.R
import com.example.renerd.core.database.DatabaseHelper
import com.example.renerd.core.database.Episode
import com.example.renerd.core.utils.formatTime
import com.example.renerd.core.utils.log
import com.example.renerd.databinding.ActivityPlayerBinding
import com.example.renerd.services.AudioService
import com.example.renerd.view_models.EpisodeViewModel
import org.koin.android.ext.android.inject

class PlayerActivity: AppCompatActivity(), PlayerContract.View {

    private val context = this
    private val thisActivity = this

    private lateinit var binding: ActivityPlayerBinding
    private val presenter: PlayerContract.Presenter by inject()


    private var currentAction = "PLAY"
    private var totalDuration: Int = 0
    private var currenPosition: Int = 0

    private lateinit var bitmap: Bitmap

    private var isTheSameEpisodePlaying = false


    private val myReceiver = object : BroadcastReceiver() {
        override fun onReceive(contexta: Context, intent: Intent) {
            if(isTheSameEpisodePlaying) {
                log("devo mostrar att na ui")
                val playerTotalTime = intent.getStringExtra("playerTotalTime")
                playerTotalTime?.let {
                    totalDuration = it.toInt()
                    setSeekBarMax(it.toInt())
                    setTotalTime(it.toInt())
                }


                val playerCurrentTime = intent.getStringExtra("playerCurrentTime")
                playerCurrentTime?.let {
                    setCurrentTime(it.toInt())
                    setSeekBarPosition(it.toInt())
                }

                val recivePause = intent.getStringExtra("paused")
                recivePause?.let {
                    binding.fabPlayOrPause.setImageResource(R.drawable.ic_play)
                }

                val recivePlay = intent.getStringExtra("played")
                recivePlay?.let {
                    binding.fabPlayOrPause.setImageResource(R.drawable.ic_pause)
                }
            }
            else{
                log("nn devo mostrar att na ui")
            }
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#191919")


        presenter.attachView(this)

        val episodeModel = intent.getParcelableExtra<EpisodeViewModel>("episode")
        if(episodeModel != null){
            if(episodeModel.audioUrl == presenter.getCurrentEpisodePlaying()){
                isTheSameEpisodePlaying = true
            }
            else{
                isTheSameEpisodePlaying = false
            }
            this.setUpPodcast(episodeModel)
        }
    }



    private fun setUpPodcast(episode: EpisodeViewModel){
        binding.backgroundImage.load(episode.imageUrl) {
            target(
                onSuccess = { drawable ->
                    binding.backgroundImage.setImageDrawable(drawable)
                    bitmap = drawable.toBitmap()
                },
                onError = {
                    //binding.imageViewMediaCover.load(R.drawable.image_media_cover_loading)
                }
            )
        }

        binding.fabPlayOrPause.setOnClickListener {
            val intent = Intent(this, AudioService::class.java)
            if (currentAction == "PLAY"){
                intent.action = "PLAY"
                binding.fabPlayOrPause.setImageResource(R.drawable.ic_pause)
                currentAction = "PAUSE"
            } else {
                intent.action = "PAUSE"
                binding.fabPlayOrPause.setImageResource(R.drawable.ic_play)
                currentAction = "PLAY"
            }

            if(!isTheSameEpisodePlaying){
                stopService(Intent(this, AudioService::class.java))
                presenter.setCurrentEpisodePlaying(episode.audioUrl)





                val dbHelper = DatabaseHelper(context)
                //dbHelper.insertEpisode(episode)
                log(dbHelper.getAllEpisodes().size)


                isTheSameEpisodePlaying = true
            }


            intent.putExtra("title", episode.title)
            intent.putExtra("artist", "Nerdcast")
            intent.putExtra("url", episode.audioUrl)
            intent.putExtra("backgroundImageUrl", episode.imageUrl)
            intent.putExtra("position", currenPosition.toString())
            startService(intent)
        }


        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    //val position = calculatePartFromPercentage(progress.toDouble(), totalDuration.toDouble())

                    val intent = Intent(thisActivity, AudioService::class.java)
                    intent.action = "PLAY"
                    intent.putExtra("position", progress.toString())
                    startService(intent)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }





    private fun setCurrentTime(milliseconds: Int){
        currenPosition = milliseconds
        binding.playerCurrentTime.text = formatTime(currenPosition)
    }

    private fun setTotalTime(milliseconds: Int){
        binding.playerTotalTime.text = formatTime(milliseconds)
    }

    private fun setSeekBarPosition(percent: Int){
        binding.seekBar.progress = percent
    }

    private fun setSeekBarMax(number: Int){
        binding.seekBar.max = number
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter("MY_ACTION")
        registerReceiver(myReceiver, intentFilter, Context.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(myReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}



