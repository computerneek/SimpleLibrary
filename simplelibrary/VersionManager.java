package simplelibrary;
import java.util.ArrayList;
import java.util.logging.Logger;
/**
 * A version manager class, created to make version management easy.
 * @author Bryan Dolan
 * @since 8.3
 */
public class VersionManager{
    private static final Logger LOG = Logger.getLogger(VersionManager.class.getName());
    /**
     * The current version (This version)
     * @since 8.3
     */
    public String currentVersion;
    /**
     * List of all recognized versions.
     * @since 8.3
     */
    private final ArrayList<String> versions = new ArrayList<>();
    /**
     * The earliest version that the current version has back compatibility for.
     * @since 8.3
     */
    private String backCompatibleTo;
    /**
     * Creates a new <code>VersionManager</code> with the specified first version.  Add further versions with the <code>addVersion</code> methods.
     * @param firstVersion The first and earliest version in the version manager
     * @since 8.3
     */
    public VersionManager(String firstVersion){
        addVersion(firstVersion);
        breakBackCompatability();
    }
    /**
     * Adds a version to the versions list.  Add versions with this starting with the oldest and finishing with the newest.
     * @param string The name of the version to add
     * @since 8.3
     */
    public void addVersion(String string){
        if(versions.contains(string)){
            throw new IllegalArgumentException("Cannot add same version twice!");
        }
        versions.add(string);
        currentVersion = string;
    }
    /**
     * Gets the version ID for the specified String version.  The version ID is its index in the list of versions- the higher it is, the newer it is.
     * @param version The version to ID
     * @return The version ID (-1 if <code>version</code> is not a valid version)
     * @since 8.3
     */
    public int getVersionID(String version){
        return versions.indexOf(version);
    }
    /**
     * Gets the String version for the specified version ID
     * @param ID the version ID
     * @return The String version
     * @throws IndexOutOfBoundsException if the ID is not a valid version ID
     * @since 8.3
     */
    public String getVersion(int ID){
        return versions.get(ID);
    }
    /**
     * Informs the version manager that there is no back compatibility past the currently added version.  This only effects the isCompatible method.
     * @since 8.3
     */
    public void breakBackCompatability(){
        backCompatibleTo = currentVersion;
    }
    /**
     * Checks if the system has back compatibility for the specified version ID
     * @param versionID The version ID to check
     * @return If back compatibility exists for the specified version ID
     * @since 8.3
     */
    public boolean isCompatible(int versionID){
        return getVersionID(backCompatibleTo)<=versionID;
    }
}
