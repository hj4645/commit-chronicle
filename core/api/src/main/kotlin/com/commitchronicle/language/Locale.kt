package com.commitchronicle.language

enum class Locale(val code: String, val languageName: String) {
    ENGLISH("en", "English"),
    KOREAN("ko", "한국어");

    companion object {
        fun fromCode(code: String): Locale {
            return values().find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
} 