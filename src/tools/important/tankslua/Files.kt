package tools.important.tankslua
import java.io.File

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