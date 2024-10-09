package tools.important.tankslua
import java.io.File

const val ILLEGAL_FILENAME_CHARS = "<>:\"\\/|?*"

fun toLegalFilename(filename: String): String {
    val nameWithoutExtension = filename.substringBeforeLast('.')
    when (nameWithoutExtension.lowercase()) {
        "con","prn","aux","nul",
        "com1","com2","com3","com4","com5","com6","com7","com8","com9",
        "lpt1","lpt2","lpt3","lpt4","lpt5","lpt6","lpt7","lpt8","lpt9" -> {
            return "$nameWithoutExtension-a"
        }
    }

    val mutableFilename = StringBuilder(filename)
    filename.forEachIndexed { i, ch ->
        if (ch in ILLEGAL_FILENAME_CHARS) mutableFilename[i] = '-'
    }
    return mutableFilename.toString()
}

fun verifyDirectory(directory: File) {
    if (!directory.isDirectory) {
        if (directory.exists()) directory.delete()
        directory.mkdir()
    }
}

fun verifyFile(file: File) {
    if (file.isDirectory) {
        if (file.exists()) file.delete()
    }
}

fun verifyDirectoryStructure() {
    verifyDirectory(tanksLuaDir)
    verifyDirectory(levelDir)
    verifyDirectory(extensionDir)

    verifyFile(settingsFile)
}

val tanksLuaDir = File(
    System.getProperty("user.home").replace('\\', '/') + "/.tanks/tankslua"
)

val levelDir = File(tanksLuaDir.path + "/level")

val extensionDir = File(tanksLuaDir.path + "/extension")
val extensionsDir = File(extensionDir.path + "/extensions")
val extensionOptionsDir = File(extensionDir.path + "/options")

val settingsFile = File(tanksLuaDir.path + "/settings.tkv")