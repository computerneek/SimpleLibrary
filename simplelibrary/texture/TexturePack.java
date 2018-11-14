package simplelibrary.texture;
import java.io.InputStream;
import java.util.logging.Logger;
/**
 * An internal texture pack.  Draws resources from the application classpath.
 * @author Bryan
 */
public class TexturePack {
    private static final Logger LOG=Logger.getLogger(TexturePack.class.getName());
    TexturePackManager manager;
    /**
     * Closes any files opened by the texturepack.
     */
    public void close(){}
    /**
     * Gets an InputStream of the resource at the specified location
     * @param name the location
     * @return the stream
     */
    public InputStream getResourceAsStream(String name){
        return TexturePack.class.getResourceAsStream(name);
    }
    /**
     * Checks if the texture pack has a resource at the specified location without any automatic fallthrough done by <code>getResourceAsStream(String)</code>
     * @param name the location
     * @return if it has the specified resource
     */
    public boolean hasResource(String name){
        try(InputStream in = getResourceAsStream(name)){
            return in!=null;
        }catch(Exception ex){}
        return false;
    }
    /**
     * @return The name of the texturepack
     */
    public String name(){
        return "Default";
    }
}
