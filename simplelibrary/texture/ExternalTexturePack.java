package simplelibrary.texture;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
/**
 * An external texture pack.  Draws resources from a ZIP file entered in at construction.
 * @author Bryan
 */
public class ExternalTexturePack extends TexturePack {
    private static final Logger LOG=Logger.getLogger(ExternalTexturePack.class.getName());
    private final File file;
    private final String name;
    private ZipFile zip;
    /**
     * @param file The ZIP file
     * @throws IOException if an IOException occurs while attempting to read the texture pack name from the texture pack ZIP file
     * @throws IllegalArgumentException if <code>texturepack.info</code> does not exist in the zip file or if the first line of the file is blank
     */
    public ExternalTexturePack(File file) throws IOException{
        this.file = file;
        InputStream in = getResourceAsStream("/texturepack.info");
        if(in==null){
            throw new IllegalArgumentException("Illegal texture pack file!");
        }
        try (BufferedReader reader=new BufferedReader(new InputStreamReader(in))) {
            name = reader.readLine();
        }
        if(name==null||name.trim().isEmpty()){
            throw new IllegalArgumentException("Couldn't find a texture pack name!");
        }
    }
    @Override
    public void close(){
        try{
            zip.close();
            zip = null;
        }catch(IOException ex){
            Sys.error(ErrorLevel.severe, "Could not close texture pack "+name+"!", ex, ErrorCategory.fileIO);
        }
    }
    @Override
    public InputStream getResourceAsStream(String name){
        if(zip==null){
            try{
                zip = new ZipFile(file);
            }catch(IOException ex){
                Sys.error(ErrorLevel.severe, "Could not open texture pack "+this.name+"!", ex, ErrorCategory.fileIO);
                return null;
            }
        }
        ZipEntry entry = zip.getEntry(name.substring(1));
        try{
            return entry==null?(manager==null?super.getResourceAsStream(name):manager.defaultTexturePack.getResourceAsStream(name)):
                    zip.getInputStream(entry);
        }catch(IOException ex){
            Sys.error(ErrorLevel.severe, "Could not read resource "+name+" in texture pack "+this.name+"!", ex, ErrorCategory.fileIO);
            return null;
        }
    }
    @Override
    public boolean hasResource(String name){
        if(zip==null){
            try{
                zip = new ZipFile(file);
            }catch(IOException ex){
                Sys.error(ErrorLevel.severe, "Could not open texture pack "+this.name+"!", ex, ErrorCategory.fileIO);
                return false;
            }
        }
        ZipEntry entry = zip.getEntry(name.substring(1));
        return entry!=null;
    }
    @Override
    public String name(){
        return name;
    }
}
