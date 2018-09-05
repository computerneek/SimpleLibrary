package simplelibrary.opengl.gui.components;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
@Deprecated
public class ListComponentOptionButton extends ListComponent{
    private static final Logger LOG = Logger.getLogger(ListComponentOptionButton.class.getName());
    public final String label;
    public boolean enabled;
    public int startingIndex;
    private int currentIndex;
    private final String[] options;
    private boolean isPressed;
    private boolean isRightPressed;
    public double textInset;
    public ListComponentOptionButton(String label, boolean enabled, double width, double height, int startingOption, String... options){
        this.label = label;
        this.enabled = enabled;
        this.width = width;
        this.height = height;
        this.startingIndex = startingOption;
        this.currentIndex = startingOption;
        this.options = options;
    }
    public boolean isChanged(){
        return currentIndex!=startingIndex;
    }
    private void action(){
        currentIndex++;
        if(currentIndex>=options.length){
            currentIndex = 0;
        }
    }
    private void reverseAction(){
        if(currentIndex==0){
            currentIndex = options.length;
        }
        currentIndex--;
    }
    public int getIndex(){
        return currentIndex;
    }
    public void setIndex(int newIndex){
        currentIndex = newIndex;
        if(currentIndex>=options.length){
            currentIndex = 0;
        }
        if(currentIndex<0){
            currentIndex = options.length-1;
        }
    }
    public String getSelectedString(){
        return options[currentIndex];
    }
    @Override
    public double getWidthD(){
        return width;
    }
    @Override
    public double getHeightD(){
        return height;
    }
    @Override
    public void render(double x, double y, double minX, double minY, double maxX, double maxY){
        int texture = -1;
        if(enabled){
            texture = ImageStash.instance.getTexture("/gui/button.png");
        }else{
            texture = ImageStash.instance.getTexture("/gui/buttonDisabled.png");
        }
        if(textInset<0){
            switch(parent.gui.type){
                case GameHelper.MODE_2D:
                case GameHelper.MODE_HYBRID:
                case GameHelper.MODE_2D_CENTERED:
                    textInset = 5;
                    break;
                case GameHelper.MODE_3D:
                    textInset = Math.min(width/20, height/20);
                    break;
            }
        }
        drawRectWithBounds(x, y, x+width, y+height, minX, minY, maxX, maxY, texture);
        GL11.glColor4f(0, 0, 0, 1);
        drawCenteredTextWithBounds(x+textInset, y+textInset, x+width-textInset, y+height-textInset, minX, minY, maxX, maxY, label+": "+options[currentIndex]);
        GL11.glColor3f(1, 1, 1);
    }
    @Override
    public void onClicked(double x, double y, int button){
        if(button==0&&enabled){
            action();
        }
        if(button==1&&enabled){
            reverseAction();
        }
    }
}
