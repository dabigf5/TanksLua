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
    public boolean isFile(String filePath) {
        File file = new File(directory + "/" + filePath);
        return file.exists() && file.isFile();
    }

    @Override
    public boolean isDirectory(String filePath) {
        File file = new File(directory + "/" + filePath);
        return file.exists() && file.isDirectory();
    }

    @Override
    public String readPlaintextFile(String filePath) {
        File file = new File(directory + "/" + filePath);
        if (!file.exists()) return null;
        return TanksLua.readContentsOfFile(file);
    }

    @Override
    public byte[] readBinaryFile(String filePath) {
        File file = new File(directory + "/" + filePath);
        if (!file.exists()) return null;

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
