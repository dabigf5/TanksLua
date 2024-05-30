package tools.important.tankslua.luapackage.packsource;

public interface PackSource {
    String readPlaintextFile(String fileName);

    // this method is 100% unused and untested, but i hope all of its impls work as they should
    byte[] readBinaryFile(String fileName);

    String getPackName();
}
