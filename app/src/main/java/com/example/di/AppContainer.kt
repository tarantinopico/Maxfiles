package com.example.di

import android.content.Context
import com.example.data.FileRepository

class AppContainer(private val context: Context) {
    val fileRepository: FileRepository by lazy {
        FileRepository(context)
    }
}
