package tools.important.javalkv;

/**
 * Class to represent a semantic version (see <a href="https://semver.org/">semver.org</a>)<br><br>
 *
 * Do note that it only supports the version core, not prereleases nor builds.<br>
 * Used by TanksLua for extension and level script versions.
 */
public class SemanticVersion {
    public final int versionMajor;
    public final int versionMinor;
    public final int versionPatch;

    public SemanticVersion(int versionMajor, int versionMinor, int versionPatch) {
        if (versionMajor == 0 && versionMinor == 0) {
            throw new IllegalArgumentException("Version cannot begin with 0.0");
        }

        this.versionMajor = versionMajor;
        this.versionMinor = versionMinor;
        this.versionPatch = versionPatch;
    }

    public static SemanticVersion fromVersionString(String string) throws IllegalArgumentException {
        String[] versionNumbers = string.split("\\.",-1);
        if (versionNumbers.length != 3) throw new IllegalArgumentException("\""+string+"\" is not a properly formatted version!");
        return new SemanticVersion(
                Integer.parseInt(versionNumbers[0]),
                Integer.parseInt(versionNumbers[1]),
                Integer.parseInt(versionNumbers[2])
        );
    }

    public String toVersionString() {
        return versionMajor+"."+versionMinor+"."+versionPatch;
    }

    @Override
    public String toString() {
        return "[Version: "+toVersionString()+"]";
    }
}
