package tools.important.tankslua.luapackage.packsource;

import tools.important.tankslua.TanksLua;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class DirectoryPackSource implements PackSource {
    private final File directory;

    public DirectoryPackSource(File directory) {
        this.directory = directory;
    }

    @Override
    public String readPlaintextFile(String fileName) {
        File file = new File(directory + "/" + fileName);
        if (!file.exists()) throw new RuntimeException("No file exists called "+fileName);
        return TanksLua.readContentsOfFile(file);
    }

    @Override
    public byte[] readBinaryFile(String fileName) {
        File file = new File(directory + "/" + fileName);
        if (!file.exists()) throw new RuntimeException("No file exists called "+fileName);
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getPackName() {
        return directory.getName();
    }
}
