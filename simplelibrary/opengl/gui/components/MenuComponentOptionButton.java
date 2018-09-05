package simplelibrary.opengl.gui.components;
import java.util.logging.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
public class MenuComponentOptionButton extends MenuComponent{
    private static final Logger LOG = Logger.getLogger(MenuComponentOptionButton.class.getName());
    private final String label;
    public boolean enabled;
    public int startingIndex;
    private int currentIndex;
    private final String[] options;
    private boolean isPressed;
    private boolean isRightPressed;
    public double textInset = -1;
    private boolean useMouseover = true;
    protected String textureRoot = "/gui/button";
    public MenuComponentOptionButton(double x, double y, double width, double height, String label, boolean enabled, int startingOption, String... options){
        super(x, y, width, height);
        this.label = label;
        this.enabled = enabled;
        this.startingIndex = startingOption;
        this.currentIndex = startingOption;
        this.options = options;
    }
    public MenuComponentOptionButton(double x, double y, double width, double height, String label, boolean enabled, boolean useMouseover, int startingOption, String... options){
        this(x, y, width, height, label, enabled, startingOption, options);
        this.useMouseover = useMouseover;
    }
    public MenuComponentOptionButton(double x, double y, double width, double height, String label, boolean enabled, boolean useMouseover, String textureRoot, int startingOption, String... options){
        this(x, y, width, height, label, enabled, useMouseover, startingOption, options);
        this.textureRoot = textureRoot;
    }
    public boolean isChanged(){
        return currentIndex!=startingIndex;
    }
    @Override
    public void mouseEvent(double x, double y, int button, boolean isDown){
        if(button==0&&isDown==true&&enabled){
            isPressed = true;
        }else if(button==0&&isDown==false&&isPressed&&enabled){
            isPressed = false;
            action();
        }
        if(button==1&&isDown==true&&enabled){
            isRightPressed = true;
        }else if(button==1&&isDown==false&&isRightPressed&&enabled){
            isRightPressed = false;
            reverseAction();
        }
    }
    @Override
    public void processKeyboard(char character, int key, boolean pressed, boolean repeat){
        if(pressed==true&&enabled){
            if(key==Keyboard.KEY_TAB){
                parent.onTabPressed(this);
            }else if(key==Keyboard.KEY_RETURN){
                parent.onReturnPressed(this);
            }else if(key==Keyboard.KEY_SPACE||key==Keyboard.KEY_RIGHT){
                action();
            }else if(key==Keyboard.KEY_LEFT){
                reverseAction();
            }
        }
    }
    @Override
    public void render(){
        int texture = -1;
        if(enabled){
            if(isPressed||isRightPressed){
                texture = ImageStash.instance.getTexture(textureRoot+"Pressed.png");
            }else{
                if(isMouseOver){
                    texture = ImageStash.instance.getTexture(textureRoot+(useMouseover?"Mouseover":"")+".png");
                }else{
                    texture = ImageStash.instance.getTexture(textureRoot+".png");
                }
            }
        }else{
            if(isMouseOver){
                texture = ImageStash.instance.getTexture(textureRoot+"Disabled.png");
            }else{
                texture = ImageStash.instance.getTexture(textureRoot+"Disabled.png");
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
        drawRect(x, y, x+width, y+height, texture);
        GL11.glColor4f(0, 0, 0, 1);
        drawCenteredText(x+textInset, y+textInset, x+width-textInset, y+height-textInset, label+": "+options[currentIndex]);
        GL11.glColor3f(1, 1, 1);
    }
    @Override
    public void mouseover(double x, double y, boolean isMouseOver){
        super.mouseover(x, y, isMouseOver);
        if(!isMouseOver){
            isPressed = false;
            isRightPressed = false;
        }
    }
    @Override
    public void mouseDragged(double x, double y, int button){}
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
}
