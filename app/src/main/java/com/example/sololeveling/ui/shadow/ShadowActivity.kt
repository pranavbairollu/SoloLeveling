package com.example.sololeveling.ui.shadow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.sololeveling.R
import com.example.sololeveling.databinding.ActivityShadowBinding
import com.example.sololeveling.data.db.SystemDatabase
import com.example.sololeveling.data.entity.ShadowEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ShadowActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShadowBinding
    private val database by lazy { SystemDatabase.getDatabase(this) }
    private val shadowDao by lazy { database.shadowDao() }

    private var shadowId: Int = -1
    private var shadow: ShadowEntity? = null
    private var speechRecognizer: SpeechRecognizer? = null

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShadowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shadowId = intent.getIntExtra("SHADOW_ID", -1)
        if (shadowId == -1) {
            finish()
            return
        }

        setupUI()
        loadShadow()
        setupSpeechRecognizer()
    }

    private fun setupUI() {
        binding.ivMic.setOnClickListener {
            checkPermissionAndStartListening()
        }
        binding.btnRetry.setOnClickListener {
            binding.btnRetry.visibility = View.GONE
            binding.tvStatus.text = getString(R.string.shadow_status_listening)
            startListening()
        }
    }

    private fun loadShadow() {
        lifecycleScope.launch {
            shadow = withContext(Dispatchers.IO) {
                shadowDao.getShadowById(shadowId)
            }
            
            shadow?.let {
                binding.tvShadowName.text = it.name
                if (it.isActive) {
                    binding.tvStatus.text = getString(R.string.shadow_already_unlocked)
                    binding.ivMic.isEnabled = false
                }
            }
        }
    }

    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    binding.tvStatus.text = getString(R.string.shadow_status_say_arise)
                    binding.ivMic.setColorFilter(resources.getColor(android.R.color.holo_green_light, null))
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    binding.ivMic.setColorFilter(resources.getColor(android.R.color.white, null))
                    binding.tvStatus.text = getString(R.string.shadow_status_processing)
                }
                override fun onError(error: Int) {
                    binding.tvStatus.text = getString(R.string.shadow_status_error)
                    binding.btnRetry.visibility = View.VISIBLE
                    binding.ivMic.setColorFilter(resources.getColor(android.R.color.holo_red_light, null))
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null) {
                        for (match in matches) {
                            if (match.contains("arise", ignoreCase = true)) {
                                unlockShadow()
                                return
                            }
                        }
                    }
                    binding.tvStatus.text = getString(R.string.shadow_status_incorrect)
                    binding.btnRetry.visibility = View.VISIBLE
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } else {
             binding.tvStatus.text = getString(R.string.shadow_no_recognition)
             binding.ivMic.isEnabled = false
        }
    }

    private fun checkPermissionAndStartListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        } else {
            startListening()
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun unlockShadow() {
        binding.tvStatus.text = getString(R.string.shadow_status_success)
        binding.tvStatus.setTextColor(resources.getColor(android.R.color.holo_purple, null))
        binding.ivShadowPlaceholder.setColorFilter(resources.getColor(android.R.color.holo_purple, null)) // "Glow"
        
        lifecycleScope.launch {
            shadow?.let {
                val unlocked = it.copy(isActive = true)
                withContext(Dispatchers.IO) {
                    shadowDao.insertShadow(unlocked)
                }
            }
            Toast.makeText(this@ShadowActivity, getString(R.string.shadow_unlocked_toast), Toast.LENGTH_LONG).show()
            kotlinx.coroutines.delay(2000)
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
             Toast.makeText(this, getString(R.string.shadow_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
    }
}
