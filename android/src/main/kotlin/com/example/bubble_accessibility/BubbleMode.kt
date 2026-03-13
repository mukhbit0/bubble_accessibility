package com.example.bubble_accessibility

enum class BubbleMode {
    ACCESSIBILITY_SERVICE,  // Dart index 0
    FOREGROUND_SERVICE,     // Dart index 1
    AUTO,                   // Dart index 2
    ;

    companion object {
        fun fromIndex(i: Int) = values().getOrElse(i) { ACCESSIBILITY_SERVICE }
    }
}
