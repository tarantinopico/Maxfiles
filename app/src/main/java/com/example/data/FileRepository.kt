package com.example.data

import android.content.Context
import com.example.domain.models.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileRepository(private val context: Context) {
    // Start at internal files dir for reliable preview behavior
    private val rootDir = context.filesDir

    init {
        try {
            // Create some sample data if empty so the UI doesn't look barren
            if (rootDir.listFiles()?.isEmpty() == true) {
                val docs = File(rootDir, "Documents").apply { mkdirs() }
                val src = File(rootDir, "NexusEngine").apply { mkdirs() }
                
                if (docs.exists()) File(docs, "README.md").writeText("# CodeFlow\n\nWelcome to your new premium file manager and editor.")
                if (src.exists()) {
                    File(src, "main.kt").writeText("fun main() {\n    println(\"Initializing CodeFlow engine...\")\n}")
                    File(src, "config.json").writeText("{\n  \"theme\": \"SophisticatedDark\",\n  \"git_enabled\": true\n}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // Protect against IO/Permissions crash on startup
        }
    }

    fun getRootPath(): String = rootDir.absolutePath

    suspend fun getFiles(path: String = rootDir.absolutePath): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val directory = File(path)
            if (!directory.exists() || !directory.isDirectory) {
                return@withContext Result.failure(Exception("Path is not a valid directory"))
            }

            val files = directory.listFiles()?.map { FileItem(it) }?.sortedWith(
                compareBy<FileItem> { !it.isDirectory }.thenBy { it.name.lowercase() }
            ) ?: emptyList()
            
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFile(parentPath: String, name: String, isFolder: Boolean): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val file = File(parentPath, name)
            if (file.exists()) return@withContext Result.failure(Exception("File already exists"))
            
            val success = if (isFolder) file.mkdir() else file.createNewFile()
            if (success) {
                Result.success(FileItem(file))
            } else {
                Result.failure(Exception("Could not create file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            val success = if (file.isDirectory) file.deleteRecursively() else file.delete()
            if (success) Result.success(true) else Result.failure(Exception("Failed to delete"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun readFileContext(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists() || file.isDirectory) {
                return@withContext Result.failure(Exception("Invalid file"))
            }
            Result.success(file.readText())
        } catch (e: Exception) {
             Result.failure(e)
        }
    }
    
    suspend fun saveFileContent(path: String, content: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            File(path).writeText(content)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
