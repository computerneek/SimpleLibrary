package simplelibrary.texture;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
/**
 * An automatic texture pack management system
 * @author Bryan
 * @since 1.5
 */
public class TexturePackManager{
    /**
     * The current texture pack manager
     *
     * NOTE:  This value is set by the TexturePackManager constructor.
     * @since 1.5
     */
    public static TexturePackManager instance;
    private static final Logger LOG=Logger.getLogger(TexturePackManager.class.getName());
    /**
     * The current texture pack
     *
     * NOTE:  Must never be null!
     * @since 1.5
     */
    public TexturePack currentTexturePack;
    /**
     * The folder in which to get the texture packs from
     * @since 1.5
     */
    public final File folder;
    /**
     * The list of texture packs that have been registered
     * @since 1.5
     */
    public final ArrayList<TexturePack> texturePacks;
    final TexturePack defaultTexturePack;
    /**
     * Creates a new texture pack manager.  It stores itself in the <code>instance</code> variable at the end of the constructor, so it is safe to ignore the new object.
     * @param file The folder to search for new texture packs.  If it does not exist, it is created.  If it is null, no external texture pack searching is done.
     * @param defaultTexturePack The default texture pack.  Null value may cause errors.
     */
    public TexturePackManager(File file, TexturePack defaultTexturePack){
        folder = file;
        if(folder!=null) folder.mkdirs();
        texturePacks = new ArrayList<>();
        texturePacks.add(defaultTexturePack);
        this.defaultTexturePack = defaultTexturePack;
        currentTexturePack = texturePacks.get(0);
        instance = this;
        findTexturePacks();
    }
    /**
     * Searches the texture pack directory for texture packs
     * @return -1 if search failed; otherwise, the number of texturepacks that were found, not including the default texture pack.
     */
    public int findTexturePacks(){
        if(folder==null) return 0;
        File[] files = folder.listFiles();
        if(files==null){
            return -1;
        }
        while(!texturePacks.isEmpty()){
            texturePacks.remove(0).close();
        }
        texturePacks.add(defaultTexturePack);
        if(files.length==0){
            return 0;
        }
        int loaded = 0;
        for(File file : files){
            loaded+=tryLoadTexturepack(file)?1:0;
        }
        return loaded;
    }
    /**
     * @return The names of all the texture packs currently identified.
     */
    public ArrayList<String> listTexturePacks(){
        ArrayList<String> names = new ArrayList<>();
        for(TexturePack texturepack : texturePacks){
            names.add(texturepack.name());
        }
        return names;
    }
    /**
     * Sets the current texture pack to the first one it can find with the specified name
     * @param name The name of the desired texture pack
     * @return If any applicable texture pack was found
     */
    public boolean setTexturePack(String name){
        for(TexturePack texturePack:texturePacks){
            if(texturePack.name().equals(name)){
                currentTexturePack=texturePack;
                texturePack.manager = this;
                return true;
            }
        }
        return false;
    }
    private boolean tryLoadTexturepack(File file){
        try{
            TexturePack texturepack = new ExternalTexturePack(file);
            texturePacks.add(texturepack);
            return true;
        }catch(IOException | RuntimeException ex){
            return false;
        }
    }
}
