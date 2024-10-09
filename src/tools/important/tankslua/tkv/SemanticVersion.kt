package tools.important.tankslua.tkv

data class SemanticVersion(
    val versionMajor: Int,
    val versionMinor: Int,
    val versionPatch: Int
) {
    init {
        if (versionMajor < 0 || versionMinor < 0 || versionPatch < 0)
            throw IllegalArgumentException("Semantic versioning does not allow negative numbers!")

        if (versionMajor == 0 && versionMinor == 0) throw IllegalArgumentException("Semantic versioning forbids the minor version from being 0 while the major version is 0")
    }

    fun toVersionString(): String {
        return "$versionMajor.$versionMinor.$versionPatch"
    }

    companion object {
        fun fromVersionString(str: String): SemanticVersion {
            val split = str.split('.')
            if (split.size != 3) throw IllegalArgumentException("Malformed semantic version '$str'")

            val major = split[0].toInt()
            val minor = split[1].toInt()
            val patch = split[2].toInt()

            return SemanticVersion(major, minor, patch)
        }
    }
}