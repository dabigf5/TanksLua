package tools.important.tankslua.luapackage.packsource;

public class DecoyPackSource implements PackSource{
    @Override
    public boolean isFile(String filePath) {
        return false;
    }

    @Override
    public boolean isDirectory(String filePath) {
        return false;
    }

    @Override
    public String readPlaintextFile(String filePath) {
        return null;
    }

    @Override
    public byte[] readBinaryFile(String filePath) {
        return null;
    }

    @Override
    public String getPackName() {
        return "decoy";
    }
}