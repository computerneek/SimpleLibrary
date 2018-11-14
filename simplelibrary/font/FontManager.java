package simplelibrary.font;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import simplelibrary.Sys;
import simplelibrary.config.Config;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.opengl.ImageStash;
public class FontManager {
    private static final HashMap<String, Object> fontImages = new HashMap<>();
    private static final HashMap<String, int[]> charLengths = new HashMap<>();
    private static final HashMap<String, Integer> charHeights = new HashMap<>();
    private static final HashMap<String, String> fontTypes = new HashMap<>();
    private static Object currentText;
    private static int[] currentCharLengths = new int[0x1_0000];
    private static int currentCharHeight;
    private static String currentFontType;
    private static final Logger LOG = Logger.getLogger(FontManager.class.getName());
    public static BufferedImage generateFontTexture(boolean full){
        if(full){
            BufferedImage img = new BufferedImage(8_192, 8_192, 6);
            for(int i = 0; i<8_192; i++){
                for(int j = 0; j<8_192; j++){
                    img.setRGB(i, j, 0xFFFF_FFFF);
                }
            }
            Graphics2D g = (Graphics2D)img.getGraphics();
            char[] chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
            g.setColor(Color.BLACK);
            for(int i = 0; i<=0x10; i++){
                for(int j = 0; j<=0x1-0; j++){
                    for(int k = 0; k<=0x10; k++){
                        for(int l = 0; l<=0x10; l++){
                            String hex1 = ""+chars[i]+chars[j];
                            String hex2 = ""+chars[k]+chars[l];
                            int down = i*0x10+j;
                            int across = k*0x10+l;
                            down*=32;
                            across*=32;
                            g.drawRect(across, down, 32, 1);
                            g.drawRect(across, down, 1, 32);
                            g.drawString(hex1, across+2, down+16);
                            g.drawString(hex2, across+2, down+32);
                            if(!(""+(char)(i*0x1000+j*0x100+k*0x10+l)).trim().isEmpty()){
                                g.drawString(""+(char)(i*0x1000+j*0x100+k*0x10+l), across+18, down+24);
                            }
                        }
                    }
                }
            }
            g.dispose();
            return img;
        }else{
            BufferedImage img = new BufferedImage(512, 512, 6);
            for(int i = 0; i<512; i++){
                for(int j = 0; j<512; j++){
                    img.setRGB(i, j, 0xFFFF_FFFF);
                }
            }
            Graphics2D g = (Graphics2D)img.getGraphics();
            char[] chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
            g.setColor(Color.BLACK);
            for(int i = 0; i<256; i++){
                int down = (int)Math.floor(i/16D);
                int across = i%16;
                String hex = ""+chars[down]+chars[across];
                down*=32;
                across*=32;
                g.drawRect(across, down, 32, 1);
                g.drawRect(across, down, 1, 32);
                g.drawString(hex, across+4, down+16);
                g.drawString(""+(char)i, across+4, down+28);
            }
            g.dispose();
            return img;
        }
    }
    public static void setFont(String fontName){
        if(!fontImages.containsKey(fontName)){
            throw new IllegalArgumentException("No font has been registered at "+fontName+"!");
        }
        currentText = fontImages.get(fontName);
        currentCharLengths = charLengths.get(fontName);
        currentCharHeight = charHeights.get(fontName);
        currentFontType = fontTypes.get(fontName);
    }
    public static double getLengthForStringWithHeight(String text, double height){
        if(text==null){
            return 0;
        }
        double scale = height/currentCharHeight;
        double length = 0;
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        for(char character : chars){
            character%=currentCharLengths.length;
            length+=currentCharLengths[character]*scale;
        }
        return length;
    }
    public static int getCharLength(char character){
        character%=currentCharLengths.length;
        return currentCharLengths[character];
    }
    public static void addFont(String baseFileName){
        if(baseFileName==null){
            throw new IllegalArgumentException("Text image name cannot be null!");
        }
        Config config = Config.loadConfig(baseFileName+".info");
        if(!config.hasProperty("font height")){
            Sys.error(ErrorLevel.severe, "Font config file at '"+baseFileName+".info' must contain a 'font height' key!", null, ErrorCategory.config);
            return;
        }else if(!config.hasProperty("font name")){
            Sys.error(ErrorLevel.severe, "Font config file at '"+baseFileName+".info' must contain a 'font name' key!", null, ErrorCategory.config);
            return;
        }else if(!config.hasProperty("font type")){
            Sys.error(ErrorLevel.severe, "Font config file at '"+baseFileName+".info' must contain a 'font type' key!", null, ErrorCategory.config);
            return;
        }
        String name = config.str("font name");
        String type = config.str("font type");
        if(type.equalsIgnoreCase("ASCII")){
            type = "ascii";
        }else if(type.equalsIgnoreCase("Unicode")){
            type = "unicode";
            //TODO Add Unicode_Segmented format; using 256 ASCII-size files, may or may not be present.
        }else{
            Sys.error(ErrorLevel.severe, "Invalid font type at "+baseFileName+"- "+type+" (Expected 'ascii' or 'unicode')!", null, ErrorCategory.config);
            return;
        }
        Object image;
        if(Sys.canUseLWJGL()){
            image = baseFileName+".png";
        }else{
            InputStream in = FontManager.class.getResourceAsStream(baseFileName+".png");
            if(in==null){
                Sys.error(ErrorLevel.minor, "Could not find texture file!", new FileNotFoundException(baseFileName+".png"), ErrorCategory.fileIO);
                return;
            }else{
                try{
                    image = ImageIO.read(in);
                }catch(IOException ex){
                    Sys.error(ErrorLevel.minor, "Could not read texture file!", ex, ErrorCategory.fileIO);
                    return;
                }
            }
        }
        int[] sizes=null;
        switch (type) {
            case "unicode":
                {
                    sizes = new int[0x1_0000];
                    char[] chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
                    for(int i = 0; i<=0x10; i++){
                        for(int j = 0; j<=0x10; j++){
                            for(int k = 0; k<=0x10; k++){
                                for(int l = 0; l<=0x10; l++){
                                    String hex = ""+chars[i]+chars[j]+chars[k]+chars[l];
                                    int index = i*0x1000+j*0x100+k*0x10+l;
                                    sizes[index] = config.hasProperty("char_"+hex+"_width")?Integer.parseInt(config.str("char_"+hex+"_width")):Integer.parseInt(config.str("font height"));
                                }
                            }
                        }
                    }       break;
                }
            case "ascii":
            {
                sizes = new int[256];
                char[] chars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
                for(int i = 0; i<256; i++){
                    int down = (int)Math.floor(i/16D);
                    int across = i%16;
                    String hex = ""+chars[down]+chars[across];
                    sizes[i] = config.hasProperty("char_"+hex+"_width")?Integer.parseInt(config.str("char_"+hex+"_width")):Integer.parseInt(config.str("font height"));
            }       break;
                }
        }
        charHeights.put(name, Integer.parseInt(config.str("font height")));
        fontImages.put(name, image);
        charLengths.put(name, sizes);
        fontTypes.put(name, type);
    }
    public static int getFontImage(){
        return ImageStash.instance.getTexture((String)currentText);
    }
    public static BufferedImage getCharacterImage(int character){
        BufferedImage img = (BufferedImage)currentText;
        double[] location = getTextureLocationForChar(character);
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        int x = (int)Math.round(location[0]*imgWidth);
        int y = (int)Math.round(location[1]*imgHeight);
        int width = (int)Math.round(location[2]*imgWidth)-x;
        int height = (int)Math.round(location[3]*imgWidth)-y;
        return img.getSubimage(x, y, width, height);
    }
    public static double[] getTextureLocationForChar(int character){
        switch(currentFontType){
            case "unicode":
                {
                    int down = (character>>8)%256;
                    int across = character%256;
                    double fullSize = 1f/0xFF;
                    double perPixel = fullSize/currentCharHeight;
                    return new double[]{across*fullSize, down*fullSize, across*fullSize+perPixel*currentCharLengths[character], down*fullSize+fullSize};
                }
            case "ascii":
                {
                    int down = (character>>4)%16;
                    int across = character%16;
                    double fullSize = 1f/0x10;
                    double perPixel = fullSize/currentCharHeight;
                    return new double[]{across*fullSize, down*fullSize, across*fullSize+perPixel*currentCharLengths[Math.min(character, 255)], down*fullSize+fullSize};
                }
            default:
                return null;
        }
    }
    private FontManager(){}
}
