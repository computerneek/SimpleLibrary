package simplelibrary.opengl.gui.components;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
public class MenuComponentSlider extends MenuComponent{
    private static final Logger LOG = Logger.getLogger(MenuComponentSlider.class.getName());
    public boolean enabled;
    public double textInset = -1;
    public double minimum;
    public double maximum;
    public double value;
    public final int digits;
    public boolean isPressed;
    public double sliderHeight;
    public double maxSliderX;
    public double sliderX;
    public MenuComponentSlider(double x, double y, double width, double height, int minimum, int maximum, int initial, boolean enabled){
        super(x, y, width, height);
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = initial;
        digits = 0;
        this.enabled = enabled;
        updateSlider();
    }
    public MenuComponentSlider(double x, double y, double width, double height, double minimum, double maximum, double initial, int digits, boolean enabled){
        super(x, y, width, height);
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = initial;
        this.digits = digits;
        this.enabled = enabled;
        updateSlider();
    }
    @Override
    public void onMouseButton(double x, double y, int button, boolean pressed, int mods) {
        if(button==0&&pressed&&enabled){
            isPressed = true;
            updateSlider(x);
        }else if(button==0&&!pressed){
            isPressed = false;
        }
    }
    @Override
    public void onMouseMove(double x, double y) {
        if(isPressed) updateSlider(x);
    }
    @Override
    public void onMouseMovedElsewhere(double x, double y) {
        if(isPressed) updateSlider(x);
    }
    @Override
    public void render(){
        int texture = -1;
        if(enabled){
            if(isPressed){
                texture = ImageStash.instance.getTexture("/gui/sliderPressed.png");
            }else{
                if(isMouseOver){
                    texture = ImageStash.instance.getTexture("/gui/slider.png");
                }else{
                    texture = ImageStash.instance.getTexture("/gui/slider.png");
                }
            }
        }else{
            if(isMouseOver){
                texture = ImageStash.instance.getTexture("/gui/sliderDisabled.png");
            }else{
                texture = ImageStash.instance.getTexture("/gui/sliderDisabled.png");
            }
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
        drawRect(x, y, x+width, y+height, ImageStash.instance.getTexture("/gui/sliderBackground.png"));
        drawRect(x+sliderX, y, x+sliderX+sliderHeight, y+sliderHeight, texture);
        GL11.glColor3f(0, 0, 0);
        drawCenteredText(x+textInset, y+sliderHeight+textInset, x+width-textInset, y+height-textInset, getValueS());
        GL11.glColor3f(1, 1, 1);
    }
    private void updateSlider(double x){
        x-=sliderHeight/2;
        double percent = x/maxSliderX;
        if(percent>1){
            percent = 1;
        }else if(percent<0){
            percent = 0;
        }
        value = percent*(maximum-minimum)+minimum;
        updateSlider();
    }
    private void updateSlider(){
        sliderHeight = height/2;
        maxSliderX = width-sliderHeight;
        sliderX = 0;
        double percent = (value-minimum)/(maximum-minimum);
        sliderX = maxSliderX*percent;
    }
    public String getValueS(){
        if(Math.round(getValue())==getValue()){
            return ""+Math.round(getValue());
        }else{
            return ""+getValue();
        }
    }
    public double getValue(){
        if(digits==0){
            return Math.round(value);
        }else{
            return (double)Math.round(value*digits)/digits;
        }
    }
    public void setValue(double value){
        this.value = Math.min(maximum, Math.max(minimum, value));
    }
}
