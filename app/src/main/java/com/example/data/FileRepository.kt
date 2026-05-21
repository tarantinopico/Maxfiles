package com.example.data

import android.content.Context
import com.example.domain.models.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.os.Environment

class FileRepository(private val context: Context) {
    private val rootDir = Environment.getExternalStorageDirectory()
    private val prefs = context.getSharedPreferences("codeflow_prefs", Context.MODE_PRIVATE)

    fun getFavorites(): Set<String> {
        return prefs.getStringSet("favorites", emptySet()) ?: emptySet()
    }

    fun toggleFavorite(path: String) {
        val favs = getFavorites().toMutableSet()
        if (favs.contains(path)) favs.remove(path) else favs.add(path)
        prefs.edit().putStringSet("favorites", favs).apply()
    }

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

    suspend fun duplicateFile(path: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) return@withContext Result.failure(Exception("File not found"))
            
            val ext = file.extension
            val nameWithoutExt = file.nameWithoutExtension
            var newFile = File(file.parentFile, "${nameWithoutExt}_copy${if(ext.isNotEmpty()) ".$ext" else ""}")
            var counter = 1
            while (newFile.exists()) {
                counter++
                newFile = File(file.parentFile, "${nameWithoutExt}_copy$counter${if(ext.isNotEmpty()) ".$ext" else ""}")
            }
            
            if (file.isDirectory) {
                file.copyRecursively(newFile, true)
            } else {
                file.copyTo(newFile, true)
            }
            Result.success(FileItem(newFile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pasteFiles(paths: Set<String>, destDir: String, isMove: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val dest = File(destDir)
            if (!dest.exists() || !dest.isDirectory) return@withContext Result.failure(Exception("Invalid destination"))
            
            for (path in paths) {
                val file = File(path)
                if (file.exists()) {
                    val destFile = File(dest, file.name)
                    if (isMove) {
                        file.renameTo(destFile)
                    } else {
                        if (file.isDirectory) file.copyRecursively(destFile, true)
                        else file.copyTo(destFile, true)
                    }
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun zipFiles(paths: Set<String>, destDir: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val dest = File(destDir, "Archive.zip")
            var counter = 1
            var finalDest = dest
            while (finalDest.exists()) {
                finalDest = File(destDir, "Archive_$counter.zip")
                counter++
            }
            
            java.util.zip.ZipOutputStream(java.io.FileOutputStream(finalDest)).use { zos ->
                for (path in paths) {
                    val file = File(path)
                    if (file.exists()) {
                        if (file.isDirectory) {
                            file.walkTopDown().forEach { f ->
                                val zipEntry = java.util.zip.ZipEntry(f.relativeTo(file.parentFile).path + if (f.isDirectory) "/" else "")
                                zos.putNextEntry(zipEntry)
                                if (f.isFile) {
                                    f.inputStream().use { it.copyTo(zos) }
                                }
                                zos.closeEntry()
                            }
                        } else {
                            val zipEntry = java.util.zip.ZipEntry(file.name)
                            zos.putNextEntry(zipEntry)
                            file.inputStream().use { it.copyTo(zos) }
                            zos.closeEntry()
                        }
                    }
                }
            }
            Result.success(true)
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
