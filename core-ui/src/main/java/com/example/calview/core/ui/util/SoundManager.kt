package com.example.calview.core.ui.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

class SoundManager(private val context: Context) {
    
    // We use ToneGenerator for now as we don't have custom assets.
    // In a real implementation, we would use SoundPool with R.raw resources.
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    
    enum class SoundType {
        SUCCESS,
        ERROR,
        CLICK,
        COMPLETE
    }

    fun play(type: SoundType) {
        when (type) {
            SoundType.SUCCESS -> toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT)
            SoundType.COMPLETE -> toneGenerator.startTone(ToneGenerator.TONE_DTMF_0, 150) // Thud-like
            SoundType.CLICK -> toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 20)
            SoundType.ERROR -> toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK)
        }
    }
    
    fun release() {
        toneGenerator.release()
    }
}

@Composable
fun rememberSoundManager(): SoundManager {
    val context = LocalContext.current
    return remember(context) { SoundManager(context) }
}
