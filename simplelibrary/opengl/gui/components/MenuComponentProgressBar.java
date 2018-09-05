package simplelibrary.opengl.gui.components;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;
import simplelibrary.opengl.ImageStash;
public class MenuComponentProgressBar extends MenuComponent{
    private static final Logger LOG = Logger.getLogger(MenuComponentProgressBar.class.getName());
    public double progress;
    private final String backgroundTexture;
    private final String foregroundTexture;
    public MenuComponentProgressBar(int x, int y, int width, int height, double progress, String backgroundTexture, String foregroundTexture){
        super(x, y, width, height);
        this.progress = progress;
        this.backgroundTexture=backgroundTexture;
        this.foregroundTexture=foregroundTexture;
    }
    @Override
    public void render(){
        drawRect(x, y, x+width, y+height, ImageStash.instance.getTexture(backgroundTexture));
        drawRectWithBounds(x, y, x+width, y+height, x, y, x+width*(progress/100), y+height, ImageStash.instance.getTexture(foregroundTexture));
    }
}
