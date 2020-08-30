package simplelibrary.opengl;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import simplelibrary.Queue;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.texture.TexturePack;
import simplelibrary.texture.TexturePackManager;
/**
 * An image system
 *
 * Uses the <code>TexturePackManager</code> class to get textures;
 * create at least one instance of <code>TexturePackManager</code> before using this.
 * @author Bryan
 * @since 1.5
 * @see TexturePackManager
 */
public class ImageStash{
    /**
     * The base instance; it is recommended to use this, but more can be made.
     * @since 1.5
     */
    public static final ImageStash instance = new ImageStash();
    private static final Logger LOG = Logger.getLogger(ImageStash.class.getName());
    private final HashMap<String, Integer> textureMap = new HashMap<>();
    private final ArrayList<Integer> textureNameList = new ArrayList<>();
    private final IntBuffer singleIntBuffer = createDirectIntBuffer(1);
    private final ByteBuffer imageData = createDirectByteBuffer(16_777_216);
    private final BufferedImage missingTextureImage;
    private final HashMap<String, BufferedImage> multithreadedInserts = new HashMap<>();
    private int boundImage;
    private final HashMap<String, Integer> bufferMap = new HashMap<>();
    private final ArrayList<Integer> buffers = new ArrayList<>();
    private final HashMap<Integer, Integer> bufferToTextureMap = new HashMap<>();
    private final Queue<Integer> buffersToDelete = new Queue<>();
    private int boundBuffer;
    private static void generateTextureNames(IntBuffer intBuffer) {
        GL11.glGenTextures(intBuffer);
    }
    public static ByteBuffer createDirectByteBuffer(int bufferSize) {
        return ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
    }
    public static IntBuffer createDirectIntBuffer(int bufferSize) {
        return createDirectByteBuffer(bufferSize << 2).asIntBuffer();
    }
    private Thread myThread;
    {
        missingTextureImage = new BufferedImage(256, 256, 2);
        Graphics g = missingTextureImage.getGraphics();
        String text = "Missing Texture!";
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        g.setColor(Color.BLACK);
        g.drawChars(chars, 0, chars.length, 75, 127);
        g.dispose();
        myThread = Thread.currentThread();
    }
    /**
     * Gets the integer name associated with the string
     *
     * If the string is not associated with anything, the string is assigned an integer name and it is returned.
     * @param filename the path to the file containing a texture
     * @return the integer name tied to the file
     * @since 1.5
     */
    public int getTexture(String filename){
        if(!buffersToDelete.isEmpty()&&myThread==Thread.currentThread()){
            deleteBuffer(buffersToDelete.dequeue());
        }
        if(filename==null||filename.isEmpty()||filename.equals("X")){
            return -1;
        }
        TexturePack texture = TexturePackManager.instance.currentTexturePack;
        Integer textureName = textureMap.get(filename);
        if (textureName != null){
            return textureName;
        }else if(loadMultithreadedInsert(filename)){
            return getTexture(filename);
        }else{
            try{
                int name;
                InputStream var7 = texture.getResourceAsStream(filename);
                if (var7 == null){
                    Sys.error(ErrorLevel.warning, "Could not find texture file "+filename+"!  Allocating default texture...", null, ErrorCategory.fileIO);
                    name = allocateAndSetupTexture(this.missingTextureImage);
                }else{
                    name = allocateAndSetupTexture(this.readTextureImage(var7));
                }
                this.textureMap.put(filename, name);
                return name;
            }catch (Exception ex){
                Sys.error(ErrorLevel.warning, null, ex, ErrorCategory.fileIO);
                int name = allocateAndSetupTexture(missingTextureImage);
                this.textureMap.put(filename, name);
                return name;
            }
        }
    }
    public int allocateAndSetupTexture(BufferedImage image){
        singleIntBuffer.clear();
        generateTextureNames(this.singleIntBuffer);
        int name = this.singleIntBuffer.get(0);
        setupTexture(image, name);
        textureNameList.add(name);
        return name;
    }
    private void setupTexture(BufferedImage image, int name){
        boundImage = name;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, name);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        int width = image.getWidth();
        int height = image.getHeight();
        int[] imageRGBData = new int[width * height];
        byte[] imageData = new byte[width * height * 4];
        image.getRGB(0, 0, width, height, imageRGBData, 0, width);
        for(int i = 0; i < imageRGBData.length; ++i){
            imageData[i * 4 + 0] = (byte)(imageRGBData[i] >> 16 & 255);
            imageData[i * 4 + 1] = (byte)(imageRGBData[i] >> 8 & 255);
            imageData[i * 4 + 2] = (byte)(imageRGBData[i] & 255);
            imageData[i * 4 + 3] = (byte)(imageRGBData[i] >> 24 & 255);
        }
        this.imageData.clear();
        this.imageData.put(imageData);
        this.imageData.position(0).limit(imageData.length);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.imageData);
    }
    /**
     * Deletes the specified texture
     * @param textureName the texture to delete
     * @since 1.5
     */
    public void deleteTexture(int textureName){
        this.textureNameList.remove((Object)textureName);
        this.singleIntBuffer.clear();
        this.singleIntBuffer.put(textureName);
        this.singleIntBuffer.flip();
        GL11.glDeleteTextures(this.singleIntBuffer);
        String[] keys = textureMap.keySet().toArray(new String[textureMap.size()]);
        for(String key : keys){
            if(textureMap.get(key)==textureName){
                textureMap.remove(key);
            }
        }
    }
    private BufferedImage readTextureImage(InputStream input) throws IOException{
        BufferedImage image = ImageIO.read(input);
        input.close();
        return image;
    }
    /**
     * Binds the texture for use in LWJGL
     * @param image
     */
    public void bindTexture(int image){
        if (image >= 0){
            this.boundImage = image;
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, image);
        }
    }
    public int getBoundTexture(){
        return boundImage;
    }
    public void clearTextures(){
        Integer[] keys = textureNameList.toArray(new Integer[textureNameList.size()]);
        for(int key : keys){
            deleteTexture(key);
        }
    }
    public int multithreadedInsert(String filename){
        if(filename==null||filename.isEmpty()||filename.equals("X")){
            return -1;
        }
        TexturePack texture = TexturePackManager.instance.currentTexturePack;
        Integer textureName = textureMap.get(filename);
        if (textureName != null){
            return textureName;
        }else{
            try{
                InputStream var7 = texture.getResourceAsStream(filename);
                if (var7 == null){
                    Sys.suppressedErrors.add(ErrorLevel.warning, "Could not find texture file "+filename+"!  Cancelling multithreaded insert...", null, ErrorCategory.fileIO);
                    return -1;
                }else{
                    multithreadedInsert(filename, this.readTextureImage(var7));
                }
                return 0;
            }catch (Exception ex){
                Sys.suppressedErrors.add(ErrorLevel.warning, null, ex, ErrorCategory.fileIO);
                return -1;
            }
        }
    }
    public boolean loadMultithreadedInsert(String filename){
        BufferedImage img = multithreadedInserts.remove(filename);
        if(img!=null){
            textureMap.put(filename, allocateAndSetupTexture(img));
            return true;
        }
        return false;
    }
    /**
     * Sets up a multithreaded insert.
     * This uses the multithreading system, so can be called from any thread; provide the filename and the image.
     * NOTE:  Recommended to use the straight String parameter function if possible.
     * This is intended for generated, named textures.
     * 
     * When getTexture() is called on a texture name that hasn't been loaded, the multithreaded inserts are checked FIRST, before the file is looked for.
     * @param filename
     * @param image 
     */
    public void multithreadedInsert(String filename, BufferedImage image){
        multithreadedInserts.put(filename, image);
    }
    public int getBuffer(String name){
        if(!bufferMap.containsKey(name)){
            int buff = GL30.glGenFramebuffers();
            bufferMap.put(name, buff);
            buffers.add(buff);
        }
        return bufferMap.get(name);
    }
    public int getTextureForBuffer(int name){
        return bufferToTextureMap.getOrDefault(name, -1);
    }
    public boolean isBufferConfigured(int name){
        return bufferToTextureMap.containsKey(name);
    }
    public void bindBuffer(int name){
        if(name>=0){
            boundBuffer = name;
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, name);
        }
    }
    public void configureBuffer(int name, int width, int height){
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, name);
        singleIntBuffer.clear();
        generateTextureNames(singleIntBuffer);
        int texName = singleIntBuffer.get(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texName);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, texName, 0);
        bufferToTextureMap.put(name, texName);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, boundImage);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, boundBuffer);
    }
    public boolean hasBuffer(String name) {
        return bufferMap.containsKey(name);
    }
    public boolean hasTexture(String path){
        return textureMap.containsKey(path);
    }
    public void deleteBuffer(int bufferName){
        if(myThread!=Thread.currentThread()){
            buffersToDelete.enqueue(bufferName);
            return;
        }
        if(bufferToTextureMap.containsKey(bufferName)) deleteTexture(bufferToTextureMap.remove(bufferName));
        GL30.glDeleteFramebuffers(bufferName);
        String[] keys = bufferMap.keySet().toArray(new String[bufferMap.size()]);
        for(String key : keys){
            if(bufferMap.get(key)==bufferName){
                bufferMap.remove(key);
            }
        }
    }
}