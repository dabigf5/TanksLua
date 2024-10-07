package tools.important.tankslua.tkv

import kotlin.reflect.KClass

enum class TKVType(
    val typeClass: KClass<*>,
    val codeName: String,
) {
    STRING(String::class, "string"),
    INT(Int::class, "int"),
    FLOAT(Float::class, "float"),
    DOUBLE(Double::class, "double")
}

class TKVValue(
    val type: TKVType,
    val value: Any
) {
    init {
        if (!type.typeClass.isInstance(value)) error("Invalid value given for TKVValue")
    }
}

fun main() {
    val originalMap = mapOf(
        "myString" to TKVValue(TKVType.STRING, "skibidi string"),
        "myInteger" to TKVValue(TKVType.INT, 2),
        "myFloat" to TKVValue(TKVType.FLOAT, 2.0f),
        "myDouble" to TKVValue(TKVType.DOUBLE, 2.0),
    )

    val decoded = decodeTKV(encodeTKV(originalMap))
    assert(decoded["myString"]!!.value == "skibidi string")
    assert(decoded["myInteger"]!!.value == 2)
    assert(decoded["myFloat"]!!.value == 2.0f)
    assert(decoded["myDouble"]!!.value == 2.0)

    val preEncoded = """string myString = "skibidi string"
int myInteger = 2
float myFloat = 2.0
double myDouble = 2.0
"""
    assert(preEncoded == encodeTKV(decodeTKV(preEncoded)))
}