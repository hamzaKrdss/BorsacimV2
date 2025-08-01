package com.example.borsacimv1.classes

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.borsacimv1.R
import java.text.SimpleDateFormat
import java.util.*

class PodcastFragment : Fragment() {

    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var startText: TextView
    private lateinit var endTextView: TextView

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    val progress = (it.currentPosition * 100) / it.duration
                    seekBar.progress = progress
                    startText.text = formatTime(it.currentPosition)
                    handler.postDelayed(this, 500)
                }
            }
        }
    }

    private val audioUrl: String
        get() {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            // Raw github link formatına dikkat et
            return "https://raw.githubusercontent.com/hamzaKrdss/gunlukpodcast/main/$today.mp3"
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_podcast, container, false)


        playPauseButton = view.findViewById(R.id.playPauseButton)
        seekBar = view.findViewById(R.id.seekBar)
        startText = view.findViewById(R.id.startTxt)
        endTextView = view.findViewById(R.id.endTxt)

        seekBar.progress = 0
        startText.text = "00:00"
        endTextView.text = "00:00"

        playPauseButton.setOnTouchListener { v, event ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                v.animate().scaleX(1f).scaleY(1f).duration = 100
            }.start()
            false
        }

        playPauseButton.setOnClickListener {
            togglePlayback()
        }
        playPauseButton.setOnClickListener { togglePlayback() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer != null) {
                    val newPosition = (mediaPlayer!!.duration * progress) / 100
                    mediaPlayer!!.seekTo(newPosition)
                    startText.text = formatTime(mediaPlayer!!.currentPosition)
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        return view
    }

    private fun togglePlayback() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                playPauseButton.setImageResource(R.drawable.ic_play)
            } else {
                it.start()
                playPauseButton.setImageResource(R.drawable.ic_pause)
                handler.post(updateSeekBarRunnable)
            }
        } ?: run {
            mediaPlayer = MediaPlayer().apply {
                try {
                    setDataSource(audioUrl)
                    setVolume(1.0f,1.0f)
                    setOnPreparedListener {
                        start()
                        playPauseButton.setImageResource(R.drawable.ic_pause)
                        endTextView.text = formatTime(duration)
                        handler.post(updateSeekBarRunnable)
                    }
                    setOnCompletionListener {
                        playPauseButton.setImageResource(R.drawable.ic_play)
                        seekBar.progress = 0
                        startText.text = "00:00"
                        release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { _, _, _ ->
                        Toast.makeText(requireContext(), "🎧 Podcast henüz hazır değil \n\t\t\t\t\t 08:00 da yayınlanacak", Toast.LENGTH_SHORT).show()
                        playPauseButton.setImageResource(R.drawable.ic_play)
                        release()
                        mediaPlayer = null
                        true
                    }
                    prepareAsync()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "🎧 Bugünkü podcast mevcut değil", Toast.LENGTH_SHORT).show()
                    release()
                    mediaPlayer = null
                }
            }
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBarRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
