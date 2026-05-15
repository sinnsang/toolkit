package com.noowar.smsforwarder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marquee_items")
data class MarqueeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String = "",
    val fontType: String = "SANS",       // SANS, SERIF, MONO
    val fontSize: Int = 60,              // sp
    val direction: String = "LEFT",      // LEFT, RIGHT, UP, DOWN, BOUNCE
    val textColorKey: String = "WHITE",  // WHITE, RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET, RAINBOW
    val strokeColorKey: String = "NONE", // NONE, BLACK, WHITE, RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET
    val speed: Int = 3,                  // 1–5
    val ttsEnabled: Boolean = false,
    val bgColorKey: String = "BLACK"     // BLACK, WHITE, RED, ORANGE, YELLOW, GREEN, BLUE, INDIGO, VIOLET
)
