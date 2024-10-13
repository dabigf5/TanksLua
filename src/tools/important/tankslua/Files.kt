package tools.important.tankslua
import tanks.extension.Extension
import tools.important.tankslua.gui.Notification
import tools.important.tankslua.gui.NotificationType
import java.io.File
import java.io.IOException

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

fun Extension.getFileText(path: String): String? {
    val extClass = this::class.java

    return extClass.getResource(path)?.readText()
}

fun openFileManagerTo(file: File) {
    openFileManagerTo(file.path)
}

// thank you to Lancelot for the original java version of this function in modapi
fun openFileManagerTo(path: String) {
    val os = System.getProperty("os.name").lowercase()

    val command = if (os.contains("win")) {
        "explorer"
    } else if (os.contains("mac")) {
        "open"
    } else if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) {
        "xdg-open"
    } else {
        Notification("Unable to open '$path', unsupported operating system.", NotificationType.ERR, 1000.0)
        return
    }

    try {
        ProcessBuilder(command, path).start()
    } catch (e: IOException) {
        Notification("Unable to open '$path', IO error occurred: ${e.message}", NotificationType.ERR, 1000.0)
    }
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
    System.getProperty("user.home") + "/.tanks/tankslua"
)

val levelDir = File(tanksLuaDir.path + "/level")

val extensionDir = File(tanksLuaDir.path + "/extension")
val extensionsDir = File(extensionDir.path + "/extensions")
val extensionOptionsDir = File(extensionDir.path + "/options")

val settingsFile = File(tanksLuaDir.path + "/settings.tkv")