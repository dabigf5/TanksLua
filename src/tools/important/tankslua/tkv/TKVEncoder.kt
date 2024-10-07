package tools.important.tankslua.tkv

class TKVEncodeException(message: String) : Exception(message)
private fun encodeFail(message: String): Nothing = throw TKVEncodeException(message)

private fun TKVValue.encode(): String {
    when (type) {
        TKVType.INT, TKVType.FLOAT, TKVType.DOUBLE -> {
            return value.toString()
        }

        TKVType.STRING -> {
            val str = value as String

            val escaped = str
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\"", "\\\"")
                .replace("\\", "\\\\")

            return "\"$escaped\""
        }
    }
}

fun encodeTKV(pairs: Map<String, TKVValue>): String {
    val builder = StringBuilder()
    for ((name, value) in pairs) {
        builder.append(value.type.codeName)
            .append(" ")
            .append(name)
            .append(" = ")
            .append(value.encode())
            .append("\n")
    }
    return builder.toString()
}

fun main() {
    encodeTKV(mapOf(
        "myString" to TKVValue(TKVType.STRING, "skibidi string")
    ))
}