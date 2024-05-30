package tools.important.tankslua.luapackage.packsource;

public interface PackSource {
    String readPlaintextFile(String fileName);
    byte[] readBinaryFile(String fileName);
    String getPackName();
}
