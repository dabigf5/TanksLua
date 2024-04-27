package tools.important.tankslua.luapackage.packsource;

import java.io.File;

public class DirectoryPackSource implements PackSource {
    private final File directory;

    public DirectoryPackSource(File directory) {
        this.directory = directory;
    }

    @Override
    public File getFile(String fileName) {
        File[] matches = directory.listFiles((dir, name) -> name.equals(fileName));

        if (matches == null) return null;
        if (matches.length != 1) return null;

        return matches[0];
    }
}
