package simplelibrary.config;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
/**
 * Configuration system, version 1
 * @author Bryan
 */
public class Config{
    private static final Logger LOG = Logger.getLogger(Config.class.getName());
    private final HashMap<String, String> map = new HashMap<>();
    /**
     * The file that is tied to this config
     */
    protected File file;
    /**
     * Loads the config at the specified path
     * @param path The path of the config to load
     * @return The config that was loaded
     * Note:  If the path starts with a forwards-slash ("/"), the config is loaded via
     * <code>Class.getResourceAsStream(java.lang.String)</code>.  Otherwise, it is loaded as a file with a
     * <code>FileInputStream</code>
     * @see FileInputStream
     */
    public static Config loadConfig(String path){
        return path.startsWith("/")?loadConfig(Config.class.getResourceAsStream(path)):loadConfig(new File(path));
    }
    /**
     * Loads the config at the specified location on the file system
     * @param file the config to load
     * @return the config that was loaded
     */
    public static Config loadConfig(File file){
        try{
            file.getParentFile().mkdirs();
            if(!file.exists()){
                if(!file.createNewFile()){
                    Sys.error(ErrorLevel.warning, "Could not create file "+file.getAbsolutePath()+"!", null, ErrorCategory.fileIO);
                    return null;
                }
                Config value = loadConfig(new FileInputStream(file));
                value.setFile(file);
                return value;
            }
            Config value = loadConfig(new FileInputStream(file));
            value.file = file;
            return value;
        }catch(IOException ex){
            Sys.error(ErrorLevel.warning, "Could not find file "+file.getAbsolutePath()+"!", ex, ErrorCategory.fileIO);
            return null;
        }
    }
    /**
     * Loads a config from the input stream
     * @param stream the stream to read the config from
     * @return the config that was read
     */
    public static Config loadConfig(InputStream stream){
        Config value = null;
        if(stream==null){
            return new Config();
        }
        try {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")))) {
                value = new Config();
                String line;
                while((line = in.readLine())!=null){
                    String[] spl = line.split("=");
                    if(spl.length<2){
                        continue;
                    }
                    if(spl[0]!=null&&!spl[0].isEmpty()&&spl[1]!=null&&!spl[1].isEmpty()){
                        value.map.put(spl[0], spl[1]);
                    }
                }
            }
        }catch(IOException ex){
            Sys.error(ErrorLevel.warning, "Could not read config from stream!", ex, ErrorCategory.config);
        }
        return value;
    }
    /**
     * Gets the value of the specified key
     *
     * As all the keys are stored as strings, no conversion is done here
     * @param key the key
     * @return the value of the specified key
     */
    public String str(String key){
        if(key==null||key.isEmpty()||key.contains("..")){
            throw new IllegalArgumentException("Empty strings cannot be keys!");
        }
        if(hasProperty(key)){
            return map.get(key);
        }
        return null;
    }
    /**
     * Tests if this config contains the specified key
     * @param key the key to test for
     * @return if this config contains the specified key
     */
    public boolean hasProperty(String key){
        return map.get(key)!=null&&!map.get(key).isEmpty();
    }
    /**
     * Saves the config to file
     *
     * Note:  Crashes the program if this config has no file associated with it!
     */
    public void save(){
        if(file==null){
            Sys.error(ErrorLevel.severe, "Cannot save a config that has no file!", null, ErrorCategory.config);
        }
        file.getParentFile().mkdirs();
        try{
            try (PrintWriter out = new PrintWriter(file, "UTF-8")) {
                List<String> lst = Arrays.asList(map.keySet().toArray(new String[map.keySet().size()]));
                Collections.sort(lst);
                String[] keys = lst.toArray(new String[lst.size()]);
                for(String key : keys){
                    out.println(key+"="+map.get(key));
                }
            }
        }catch(IOException ex){
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Checks for the specified key and, if not found, assigns it a default value
     * @param key the key
     * @param defaultValue the default value
     */
    public void checkProperty(String key, String defaultValue){
        if(!hasProperty(key)){
            putProperty(key, defaultValue);
        }
    }
    /**
     * Deletes the specified key from the config, returning the String value that the key contained
     * @param key the key to remove
     * @return the value the key had
     */
    public String remove(String key){
        if(!hasProperty(key)){
            return null;
        }
        String val = str(key);
        map.remove(key);
        return val;
    }
    /**
     * Gets the file of this config
     * @return the file associated with this config
     */
    public File getFile(){
        return file;
    }
    /**
     * Overwrites the key with the value.  If the key is not present, it just adds the key with the value.
     * @param key the key
     * @param value the value
     */
    public void putProperty(String key, String value){
        if(key.startsWith("#")||key.contains("=")){
            Sys.error(ErrorLevel.severe, "Config keys cannot start with '#' or contain any '='s!", null, ErrorCategory.config);
        }
        if(key.equals("config-version")){
            Sys.error(ErrorLevel.severe, "Cannot overwrite the key \"config-version\"!", null, ErrorCategory.config);
        }
        if(value.contains("=")){
            Sys.error(ErrorLevel.severe, "Config values cannot contain any '='s!", null, ErrorCategory.config);
        }
        if(file==null){
            Sys.error(ErrorLevel.warning, "Cannot change configs that have no file!", null, ErrorCategory.config);
            return;
        }
        if(!key.isEmpty()&&!value.isEmpty()){
            map.put(key, value);
        }else{
            Sys.error(ErrorLevel.warning, "Empty property setting- key="+key+" & value="+value, null, ErrorCategory.config);
        }
    }
    /**
     * Sets the file for this config
     *
     * If the file is already set, this method does absolutely nothing
     * @param file the file for this config
     */
    public void setFile(File file){
        if(this.file==null){
            this.file = file;
        }
    }
    /**
     * Checks if there is a file associated with this config
     *
     * If this returns <code>true</code>, then <code>setFile(java.io.File)</code> will do nothing.
     * @return if there is a file associated with this config
     */
    public boolean hasFile() {
        return file!=null;
    }
    /**
     * Tests if the specified config is the same config
     *
     * Probably should have been an override for the <code>Object.equals()</code> method.
     *
     * Note:  This does not for actually being the same config, but rather just having the same keys and values.
     * @param config the config to test for equality
     * @return if they are the same config
     */
    public boolean isSameConfig(Config config) {
        if(config.getClass()!=this.getClass()){
            return false;
        }
        if(config.equals(this)){
            return true;
        }
        if(config.file!=null&&file!=null&&file.getAbsolutePath().equals(config.file.getAbsolutePath())){
            return true;
        }
        List<String> lst = Arrays.asList(config.map.keySet().toArray(new String[config.map.keySet().size()]));
        List<String> lst2 = Arrays.asList(map.keySet().toArray(new String[map.keySet().size()]));
        Collections.sort(lst);
        Collections.sort(lst2);
        String[] keys = lst.toArray(new String[lst.size()]);
        String[] keys2 = lst2.toArray(new String[lst2.size()]);
        if(keys.length!=keys2.length){
            return false;
        }
        for(int i = 0; i<keys.length; i++){
            if(!keys[i].equals(keys2[i])){
                return false;
            }
            if(!config.str(keys[i]).equals(str(keys2[i]))){
                return false;
            }
        }
        return true;
    }
    /**
     * Gets a list of all the keys available in this config
     * @return a list of all the keys in this config
     */
    public String[] properties() {
        return map.keySet().toArray(new String[map.keySet().size()]);
    }
}
