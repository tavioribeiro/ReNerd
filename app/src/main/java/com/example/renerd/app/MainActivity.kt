package com.example.renerd.app


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import coil.load
import com.example.renerd.R
import com.example.renerd.databinding.ActivityMainBinding
import com.example.renerd.core.network.PodcastClient
import com.example.renerd.core.network.model.EpisodeModel
import com.example.renerd.services.AudioService
import com.example.renerd.core.utils.formatTime
import com.example.renerd.core.utils.log
import com.example.renerd.features.episodes.EpisodesActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.URLDecoder
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private val context = this
    private val thisActivity = this
    private lateinit var binding: ActivityMainBinding
    private var currentAction = "PLAY"
    private var totalDuration: Int = 0
    private var currenPosition: Int = 0

    private lateinit var bitmap: Bitmap

    private val myReceiver = object : BroadcastReceiver() {
        override fun onReceive(contexta: Context, intent: Intent) {

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
                binding.fabPlayOrPause.setImageResource(R.drawable.icon_play)
            }

            val recivePlay = intent.getStringExtra("played")
            recivePlay?.let {
                binding.fabPlayOrPause.setImageResource(R.drawable.icon_pause)
            }
        }
    }



    fun getNerdcasts(after: String, before: String? = null) {
        GlobalScope.launch {
            try {
                val afterDecoded = URLDecoder.decode(after, "UTF-8")
                val beforeDecoded = URLDecoder.decode(before, "UTF-8")
                val response = PodcastClient.api.getNerdcasts(after = afterDecoded, before = beforeDecoded).execute() // Executa a chamada
                if(response.isSuccessful) {
                    log("FIMMMMM")
                    val podcasts = response.body()
                    // Processar a lista de podcasts
                    if (podcasts != null) {
                        log(podcasts.size)
                        setUpPodcast(podcasts[Random.nextInt(0, podcasts.size + 1)])
                    }
                    //log(podcasts)
                }
            } catch (e: SocketTimeoutException) {
                log(e)
            } catch (e: Exception) {
                log(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        this.goTo(EpisodesActivity::class.java)
        //getNerdcasts("2024-09-06%2000%3A00%3A00","2024-09-13%2000%3A00%3A00")



        //getNerdcasts("2000-01-01%2000%3A00%3A00", "2024-09-13%2000%3A00%3A00")

//        val call = PodcastClient.api.getNerdcasts(after = "2024-09-06%2000%3A00%3A00", before = "2024-09-13%2000%3A00%3A00")
//        val url = call.request().url
//        log("URL: $url")
        //call.enqueue(...)



    }

    private fun goTo(destiny: Class<EpisodesActivity>){
        val intent = Intent(this, destiny)
        startActivity(intent)
    }


    private fun setUpPodcast(podcast: EpisodeModel){
        binding.backgroundImage.load(podcast.thumbnails.img4x3355x266) {
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
                binding.fabPlayOrPause.setImageResource(R.drawable.icon_pause)
                currentAction = "PAUSE"
            } else {
                intent.action = "PAUSE"
                binding.fabPlayOrPause.setImageResource(R.drawable.icon_play)
                currentAction = "PLAY"
            }

            intent.putExtra("title", podcast.title)
            intent.putExtra("artist", "Nerdcast")
            intent.putExtra("url", podcast.audioHigh)
            intent.putExtra("backgroundImageUrl", podcast.thumbnails.img4x3355x266)
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
}




















//package com.example.renerd
//
//
//import android.media.MediaPlayer
//import android.os.Bundle
//import android.view.View
//import android.widget.SeekBar
//import androidx.appcompat.app.AppCompatActivity
//import com.example.renerd.databinding.ActivityMainBinding
//import java.io.IOException
//import java.util.Timer
//import java.util.TimerTask
//
//
//class MainActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityMainBinding
//    private var isPlaying: Boolean = false
//    private var player: MediaPlayer? = null
//    private lateinit var timer: Timer
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//
//        binding.fabPlayOrPause.setOnClickListener {
//            if (isPlaying) {
//                if (player != null) {
//                    player!!.pause()
//                    isPlaying = false
//                    binding.fabPlayOrPause.setImageResource(R.drawable.ic_play)
//                    timer.cancel() // Cancela o timer quando pausa
//                }
//            } else {
//                val url = "https://nerdcast.jovemnerd.com.br/nerdtech_103.mp3"
//                if (player == null) {
//                    player = MediaPlayer()
//                    try {
//                        player!!.setDataSource(url)
//                        player!!.prepareAsync()
//                        player!!.setOnPreparedListener {
//                            binding.seekBar.max = player!!.duration
//                            binding.tvTotalTime.text = formatTime(player!!.duration) // Define o tempo total
//
//                            timer = Timer()
//                            timer.schedule(object : TimerTask() {
//                                override fun run() {
//                                    runOnUiThread {
//                                        if (player != null) {
//                                            binding.seekBar.progress = player!!.currentPosition
//                                            binding.tvCurrentTime.text = formatTime(player!!.currentPosition)
//                                        }
//                                    }
//                                }
//                            }, 0, 1000) // Atualiza a SeekBar a cada segundo
//
//
//                            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//                                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                                    if (fromUser) {
//                                        player!!.seekTo(progress)
//                                    }
//                                }
//
//                                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
//
//                                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
//                            })
//
//
//                            player!!.start()
//                            player!!.seekTo(180000)
//                        }
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    }
//                } else {
//                    player!!.start()
//                    timer.schedule(object : TimerTask() { // Reinicia o timer se já existe
//                        override fun run() {
//                            runOnUiThread {
//                                if (player != null) {
//                                    binding.seekBar.progress = player!!.currentPosition
//                                    binding.tvCurrentTime.text = formatTime(player!!.currentPosition)
//                                }
//                            }
//                        }
//                    }, 0, 1000)
//                }
//                isPlaying = true
//                with(binding) {
//                    fabPlayOrPause.setImageResource(R.drawable.ic_pause)
//                }
//            }
//        }
//
//
//        player?.setOnCompletionListener {
//            stopPlayer()
//        }
//    }
//
//
//    private fun formatTime(milliseconds: Int): String {
//        val minutes = milliseconds / 1000 / 60
//        val seconds = (milliseconds / 1000) % 60
//        return String.format("%02d:%02d", minutes, seconds)
//    }
//
//
//    private fun stopPlayer() {
//        if (player != null) {
//            player!!.release()
//            player = null
//            timer.cancel() // Cancela o timer quando para a reprodução
//        }
//        isPlaying = false
//        binding.fabPlayOrPause.setImageResource(R.drawable.ic_play)
//        binding.seekBar.progress = 0 // Reseta a SeekBar
//        binding.tvCurrentTime.text = "00:00" // Reseta o tempo atual
//    }
//
//
//    override fun onStop() {
//        super.onStop()
//        stopPlayer()
//    }
//}