package com.justmusic.app.data.model

enum class SeekBarStyle(val displayName: String, val description: String) {
    WAVEFORM("Waveform Bars", "Aesthetic animated audio amplitude bars"),
    CAPSULE("Smooth Capsule", "Sleek rounded progress bar with glowing thumb"),
    SEGMENTED("Dashed Pulse", "Modern segmented dot pulse visualizer"),
    FLUID_WAVE("Fluid Wave", "Clean thin line with dynamic wave motion")
}
