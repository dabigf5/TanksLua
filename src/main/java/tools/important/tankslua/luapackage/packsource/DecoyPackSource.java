package tools.important.tankslua.luapackage.packsource;

public class DecoyPackSource implements PackSource{
    @Override
    public String readPlaintextFile(String fileName) {
        return null;
    }

    @Override
    public byte[] readBinaryFile(String fileName) {
        return null;
    }

    @Override
    public String getPackName() {
        return "decoy";
    }
}