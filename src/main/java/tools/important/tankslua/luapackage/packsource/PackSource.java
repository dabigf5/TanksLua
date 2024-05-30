package tools.important.tankslua.luapackage.packsource;

public interface PackSource {
    boolean isFile(String filePath);
    boolean isDirectory(String filePath);

    /**
     * Read a file inside the package as plaintext
     * @param filePath The name of the file to read
     * @return A string with the contents of the file, or null if the file doesn't exist
     */
    String readPlaintextFile(String filePath);

    /**
     * Read a file inside the package as a byte array
     * @param filePath The name of the file to read
     * @return A byte array with the contents of the file, or null if the file doesn't exist
     */
    byte[] readBinaryFile(String filePath); // this method is 100% unused and untested, but i hope all of its impls work as they should

    String getPackName();
}
