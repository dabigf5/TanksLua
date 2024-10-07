package tools.important.tankslua.tkv

class TKVDecodeException(message: String) : Exception(message)
private fun decodeFail(message: String): Nothing = throw TKVDecodeException(message)

private class ParseState(val text: String, var position: Int = 0) {
    fun peek(offset: Int = 0): Char? {
        val peekPos = position + offset
        if (peekPos < 0 || peekPos >= text.length) return null
        return text[peekPos]
    }
    fun consume(amt: Int = 1) {
        position += amt
    }
    val finished: Boolean
        get() = position >= text.length

    fun readWhile(predicate: (Char) -> Boolean): String {
        val builder = StringBuilder()

        while (!finished && predicate(peek()!!)) {
            builder.append(peek())
            consume()
        }

        return builder.toString()
    }

    fun readIdentifier(): String {
        return readWhile { !it.isWhitespace() }
    }

    fun readValue(): String {
        return readWhile { it != '\n' }
    }

    fun readType(): TKVType {
        val typeName = readIdentifier()

        return TKVType.entries.find { it.codeName == typeName } ?: decodeFail("Unknown TKV type \"$typeName\"")
    }

    fun skipWhitespace() {
        while (peek()?.isWhitespace() == true) {
            consume()
        }
    }
}

private fun String.decodeTKVValue(type: TKVType): TKVValue {
    when (type) {
        TKVType.INT -> {
            return TKVValue(TKVType.INT, toIntOrNull() ?: decodeFail("malformed int"))
        }
        TKVType.FLOAT -> {
            return TKVValue(TKVType.FLOAT, toFloatOrNull() ?: decodeFail("malformed float"))
        }
        TKVType.DOUBLE -> {
            return TKVValue(TKVType.DOUBLE, toDoubleOrNull() ?: decodeFail("malformed double"))
        }
        TKVType.STRING -> {
            if (!startsWith('"') && endsWith('"')) decodeFail("malformed string")
            val noQuotes = try {
                substring(1..<length-1)
            } catch (_: StringIndexOutOfBoundsException) {
                decodeFail("malformed string")
            }

            val unescaped = noQuotes
                .replace("\\t", "\t")
                .replace("\\b", "\b")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")

            return TKVValue(TKVType.STRING, unescaped)
        }
    }
}

fun decodeTKV(text: String): Map<String, TKVValue> {
    val map = mutableMapOf<String, TKVValue>()

    val state = ParseState(text)

    while (!state.finished) {
        if (state.peek() == '\n') {
            state.consume()
            continue
        }
        val type = state.readType()

        state.skipWhitespace()
        val name = state.readIdentifier()
        state.skipWhitespace()

        if (state.peek() != '=') decodeFail("expected '='")
        state.consume()

        state.skipWhitespace()
        val value = state.readValue()
        if (!state.finished && state.peek() != '\n') decodeFail("expected newline or eof")

        val decoded = value.decodeTKVValue(type)

        map[name] = decoded
    }

    return map.toMap()
}

fun main() {
        decodeTKV("""
string myString = "skibidi string"
float myFloat = 2.0
int myInt = 2
double myDouble = 2.0
"""
        )
}