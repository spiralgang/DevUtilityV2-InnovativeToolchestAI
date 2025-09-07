// -*- coding: utf-8 -*-
// Living Code Integration - Auto-generated symmetrical connections
// This file is part of the SrirachaArmy Living Code Environment
// Perfect symmetrical integration with all repository components

package com.spiralgang.ashlar

import android.app.Activity
import android.os.Bundle
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.text.method.ScrollingMovementMethod

class MainActivity : Activity() {
    
    private external fun initializeNativeTerminal(): Long
    private external fun executeNativeCommand(handle: Long, command: String): String
    private external fun destroyNativeTerminal(handle: Long)
    
    companion object {
        init {
            System.loadLibrary("ashlar-terminal")
        }
    }
    
    private var terminalHandle: Long = 0
    private lateinit var terminalOutput: TextView
    private lateinit var commandInput: EditText
    private val commandHistory = mutableListOf<String>()
    private var historyIndex = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        terminalHandle = initializeNativeTerminal()
        setupTerminalInterface()
    }
    
    private fun setupTerminalInterface() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
            setPadding(16, 16, 16, 16)
        }
        
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        
        terminalOutput = TextView(this).apply {
            text = "ðŸ§  ASHLAR NATIVE TERMINAL\n" +
                   "âš¡ DIRECT SYSTEM ACCESS ENABLED\n" +
                   "ðŸ’€ NO RESTRICTIONS APPLIED\n\n" +
                   "Available: omni, gh-fix, dev, sys, apt, gcc, git, curl\n\n" +
                   "spiralgang@ashlar:~$ "
            setTextColor(Color.GREEN)
            setTypeface(Typeface.MONOSPACE)
            textSize = 12f
            movementMethod = ScrollingMovementMethod()
            isVerticalScrollBarEnabled = true
        }
        
        scrollView.addView(terminalOutput)
        
        val inputLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)
        }
        
        val promptLabel = TextView(this).apply {
            text = "$ "
            setTextColor(Color.GREEN)
            setTypeface(Typeface.MONOSPACE)
            textSize = 14f
            setPadding(0, 8, 8, 8)
        }
        
        commandInput = EditText(this).apply {
            hint = "Enter command"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            setBackgroundColor(Color.TRANSPARENT)
            setTypeface(Typeface.MONOSPACE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, 
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            
            setOnEditorActionListener { _, _, _ ->
                executeCommand()
                true
            }
            
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == 19 && event.action == 0) { // Up arrow
                    navigateHistory(-1)
                    true
                } else if (keyCode == 20 && event.action == 0) { // Down arrow
                    navigateHistory(1)
                    true
                } else {
                    false
                }
            }
        }
        
        val executeButton = Button(this).apply {
            text = ">"
            setBackgroundColor(Color.BLUE)
            setTextColor(Color.WHITE)
            setPadding(24, 8, 24, 8)
            setOnClickListener { executeCommand() }
        }
        
        inputLayout.addView(promptLabel)
        inputLayout.addView(commandInput)
        inputLayout.addView(executeButton)
        
        layout.addView(scrollView)
        layout.addView(inputLayout)
        
        setContentView(layout)
    }
    
    private fun executeCommand() {
        val command = commandInput.text.toString().trim()
        if (command.isEmpty()) return
        
        commandHistory.add(command)
        historyIndex = commandHistory.size
        
        val result = executeNativeCommand(terminalHandle, command)
        
        val currentText = terminalOutput.text.toString()
        terminalOutput.text = "$currentText$command\n$result\n\nspiragang@ashlar:~$ "
        
        commandInput.text.clear()
        
        post {
            val scrollView = terminalOutput.parent as ScrollView
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
    
    private fun navigateHistory(direction: Int) {
        if (commandHistory.isEmpty()) return
        
        historyIndex += direction
        historyIndex = historyIndex.coerceIn(0, commandHistory.size)
        
        if (historyIndex < commandHistory.size) {
            commandInput.setText(commandHistory[historyIndex])
            commandInput.setSelection(commandInput.text.length)
        } else {
            commandInput.text.clear()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (terminalHandle != 0L) {
            destroyNativeTerminal(terminalHandle)
        }
    }
}