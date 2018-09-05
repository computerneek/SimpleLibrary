package simplelibrary.opengl.gui.components;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
public class MenuComponentTextArea extends MenuComponent{
    private static final Logger LOG = Logger.getLogger(MenuComponentTextArea.class.getName());
    public ArrayList<String> text;
    private double pixelsScrolled;
    private double pixelsScrollable;
    private boolean isScrolling;
    protected double textHeight;
    public double textInset = -1;
    public double scrollbarWidth = -1;
    public MenuComponentTextArea(double x, double y, double width, double height, double textHeight, String[] display){
        super(x, y, width, height);
        text = new ArrayList<>(Arrays.asList(display));
        this.textHeight = textHeight;
    }
    @Override
    public void render(){
        int line = 0;
        if(textInset<0){
            switch(parent.gui.type){
                case GameHelper.MODE_2D:
                case GameHelper.MODE_HYBRID:
                case GameHelper.MODE_2D_CENTERED:
                    textInset = 5;
                    break;
                case GameHelper.MODE_3D:
                    textInset = textHeight/18;
            }
        }
        for (String str : text) {
            do{
                GL11.glColor4f(0, 0, 0, 1);
                str = drawTextWithWrapAndBounds(x+textInset, y+textInset+line*textHeight-pixelsScrolled, x+width-textInset-(pixelsScrollable>0?(width/20):0), y+textInset+line*textHeight+textHeight-pixelsScrolled, x+textInset, y+textInset, x+width-textInset, y+height-textInset, str);
                GL11.glColor4f(1, 1, 1, 1);
                line++;
            }while(str!=null&&!str.isEmpty());
        }
        double bufferHeight = line*textHeight;
        pixelsScrollable = -height+bufferHeight;
        if(scrollbarWidth<0){
            switch(parent.gui.type){
                case GameHelper.MODE_2D:
                case GameHelper.MODE_HYBRID:
                case GameHelper.MODE_2D_CENTERED:
                    scrollbarWidth = 20;
                    break;
                case GameHelper.MODE_3D:
                    scrollbarWidth = width/20;
                    break;
            }
        }
        if(pixelsScrollable<=0){
            pixelsScrolled = 0;
        }else{
            if(pixelsScrolled>pixelsScrollable){
                pixelsScrolled = pixelsScrollable;
            }
            drawRect(x+width-scrollbarWidth, y, x+width, y+height, ImageStash.instance.getTexture("/gui/scrollbar/background.png"));
            double scrollbarPos = pixelsScrolled/pixelsScrollable;
            int distDown = (int)(scrollbarPos*(height-scrollbarWidth));
            drawRect(x+width-scrollbarWidth, y+distDown, x+width, y+distDown+scrollbarWidth, ImageStash.instance.getTexture("/gui/scrollbar/bar.png"));
        }
    }
    @Override
    public void mouseEvent(double x, double y, int button, boolean isDown){
        if(!isDown){
            isScrolling = false;
        }
        if(button==0){
            if(pixelsScrollable>0&&x>=width-20){
                isScrolling = isDown;
                mouseDragged(x, y, button);
            }
        }
    }
    @Override
    public void mouseDragged(double x, double y, int button){
        if(pixelsScrollable>0&&isScrolling&&button==0){
            double newPos = y;
            if(newPos<scrollbarWidth/2){
                newPos = scrollbarWidth/2;
            }else if(newPos>height-scrollbarWidth/2){
                newPos = height-scrollbarWidth/2;
            }
            newPos-=scrollbarWidth/2;
            double decimal = newPos/(height-scrollbarWidth);
            pixelsScrolled = (int)(decimal*pixelsScrollable);
        }
    }
}
