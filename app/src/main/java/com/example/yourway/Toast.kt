package com.example.yourway

import android.content.Context
import kotlin.coroutines.CoroutineContext

class Toast(private val message: String, private val context: Context) {
    init {
        val toast = android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT)
        toast.show()
    }
}