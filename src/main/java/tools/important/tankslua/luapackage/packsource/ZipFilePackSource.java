package tools.important.tankslua.luapackage.packsource;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFilePackSource implements PackSource {
    private final File file;
    public ZipFilePackSource(File packFile) {
        this.file = packFile;
    }

    @Override
    public String readPlaintextFile(String fileName) {
        StringBuilder textBuilder = new StringBuilder();

        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry entry = zipFile.getEntry(fileName);
            if (entry == null) throw new RuntimeException("File "+fileName+" not found in zip");
            try (InputStream inputStream = zipFile.getInputStream(entry);
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
            ) {
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    textBuilder.append(line).append('\n');
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return textBuilder.toString();
    }

    @Override
    public byte[] readBinaryFile(String fileName) {
        // this might be the single most untested method in the whole project
        byte[] bytes;

        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry entry = zipFile.getEntry(fileName);
            if (entry == null) throw new RuntimeException("File "+fileName+" not found in zip");
            try (InputStream inputStream = zipFile.getInputStream(entry)
            ) {
                byte[] buffer = new byte[(int) entry.getSize()];
                int bytesRead;
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                bytes = outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bytes;
    }

    @Override
    public String getPackName() {
        return FilenameUtils.removeExtension(file.getName());
    }
}
