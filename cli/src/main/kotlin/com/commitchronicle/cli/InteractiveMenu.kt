package com.commitchronicle.cli

import java.io.IOException

class InteractiveMenu {
    companion object {
        private const val ANSI_CLEAR_LINE = "\u001B[2K"
        private const val ANSI_CURSOR_UP = "\u001B[1A"
        private const val ANSI_CURSOR_DOWN = "\u001B[1B"
        private const val ANSI_SAVE_CURSOR = "\u001B[s"
        private const val ANSI_RESTORE_CURSOR = "\u001B[u"
        private const val ANSI_HIDE_CURSOR = "\u001B[?25l"
        private const val ANSI_SHOW_CURSOR = "\u001B[?25h"
        private const val ANSI_RESET = "\u001B[0m"
        private const val ANSI_BOLD = "\u001B[1m"
        private const val ANSI_BLUE = "\u001B[34m"
        private const val ANSI_GREEN = "\u001B[32m"
        
        fun <T> showMenu(title: String, options: List<Pair<T, String>>): T {
            var selectedIndex = 0
            
            // Enable raw mode for terminal input
            enableRawMode()
            
            try {
                print(ANSI_HIDE_CURSOR)
                println(title)
                
                while (true) {
                    // Display menu options
                    for (i in options.indices) {
                        val (_, displayText) = options[i]
                        if (i == selectedIndex) {
                            println("${ANSI_BLUE}${ANSI_BOLD}> $displayText${ANSI_RESET}")
                        } else {
                            println("  $displayText")
                        }
                    }
                    
                    // Read key input
                    val key = readKey()
                    
                    when (key) {
                        "UP" -> {
                            selectedIndex = if (selectedIndex > 0) selectedIndex - 1 else options.size - 1
                        }
                        "DOWN" -> {
                            selectedIndex = if (selectedIndex < options.size - 1) selectedIndex + 1 else 0
                        }
                        "ENTER" -> {
                            // Clear menu
                            repeat(options.size) {
                                print(ANSI_CURSOR_UP + ANSI_CLEAR_LINE)
                            }
                            print(ANSI_SHOW_CURSOR)
                            println("${ANSI_GREEN}Selected: ${options[selectedIndex].second}${ANSI_RESET}")
                            return options[selectedIndex].first
                        }
                        "ESC" -> {
                            print(ANSI_SHOW_CURSOR)
                            throw InterruptedException("Menu cancelled")
                        }
                    }
                    
                    // Move cursor up to redraw menu
                    repeat(options.size) {
                        print(ANSI_CURSOR_UP + ANSI_CLEAR_LINE)
                    }
                }
            } finally {
                print(ANSI_SHOW_CURSOR)
                disableRawMode()
            }
        }
        
        private fun readKey(): String {
            val input = System.`in`.read()
            
            return when (input) {
                27 -> { // ESC sequence
                    val next1 = System.`in`.read()
                    if (next1 == 91) { // [
                        val next2 = System.`in`.read()
                        when (next2) {
                            65 -> "UP"    // A
                            66 -> "DOWN"  // B
                            67 -> "RIGHT" // C
                            68 -> "LEFT"  // D
                            else -> "ESC"
                        }
                    } else {
                        "ESC"
                    }
                }
                10, 13 -> "ENTER" // Enter key
                3 -> "CTRL_C"     // Ctrl+C
                else -> "OTHER"
            }
        }
        
        private fun enableRawMode() {
            try {
                // Try to enable raw mode using stty (Unix/Linux/Mac)
                val process = ProcessBuilder("stty", "-echo", "cbreak")
                    .inheritIO()
                    .start()
                process.waitFor()
            } catch (e: Exception) {
                // Fallback for Windows or if stty is not available
                // Raw mode may not work perfectly, but basic functionality should still work
            }
        }
        
        private fun disableRawMode() {
            try {
                // Restore normal terminal mode
                val process = ProcessBuilder("stty", "echo", "-cbreak")
                    .inheritIO()
                    .start()
                process.waitFor()
            } catch (e: Exception) {
                // Fallback - just continue
            }
        }
    }
} 