package tools.important.tankslua

import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipFile

fun openAsRepository(file: File): FileRepository {
    if (file.isDirectory) {
        return DirectoryFileRepository(file)
    }

    when (file.extension) {
        "zip" -> return ZipFileRepository(file)
    }

    error("Cannot open $file as repository.")
}

interface FileRepository {
    fun readFile(repoPath: String): String?
}

class DirectoryFileRepository(val dir: File) : FileRepository {
    init {
        if (!dir.isDirectory) error("Expected directory for DirectoryFileRepository")
    }

    override fun readFile(repoPath: String): String? {
        val file = File(dir.path+'/'+repoPath)
        if (!file.exists()) return null

        return file.bufferedReader().use {
            it.readText()
        }
    }
}

class ZipFileRepository(val file: File) : FileRepository {
    override fun readFile(repoPath: String): String? {
        ZipFile(file).use { zip ->
            val entry = zip.getEntry(repoPath) ?: return null

            zip.getInputStream(entry).use { ins ->
                InputStreamReader(ins).use { isr ->
                    return isr.readText()
                }
            }
        }
    }
}