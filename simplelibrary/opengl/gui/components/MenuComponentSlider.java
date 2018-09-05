package simplelibrary.opengl.gui.components;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
public class MenuComponentSlider extends MenuComponent{
    private static final Logger LOG = Logger.getLogger(MenuComponentSlider.class.getName());
    public boolean enabled;
    public double textInset = -1;
    private double minimum;
    private double maximum;
    private double value;
    private final int digits;
    private boolean isPressed;
    private double sliderHeight;
    private double maxSliderX;
    private double sliderX;
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
    public void mouseEvent(double x, double y, int button, boolean isDown){
        if(button==0&&isDown==true&&enabled){
            isPressed = true;
            updateSlider(x);
        }else if(button==0&&isDown==false&&isPressed&&enabled){
            isPressed = false;
        }
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
    @Override
    public void mouseover(double x, double y, boolean isMouseOver){
        super.mouseover(x, y, isMouseOver);
        if(!isMouseOver){
            isPressed = false;
        }
    }
    @Override
    public void mouseDragged(double x, double y, int button){
        if(button==0&&enabled){
            updateSlider(x);
        }
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
