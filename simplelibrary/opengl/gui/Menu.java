package simplelibrary.opengl.gui;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
import simplelibrary.opengl.Renderer2D;
import simplelibrary.opengl.gui.components.ListComponentButton;
import simplelibrary.opengl.gui.components.MenuComponent;
import simplelibrary.opengl.gui.components.MenuComponentButton;
public abstract class Menu extends Renderer2D{
    public Menu parent;
    public ArrayList<MenuComponent> components = new ArrayList<>();
    public MenuComponent selected;
    public GUI gui;
    public Menu(GUI gui, Menu parent){
        this.gui = gui;
        this.parent = parent;
    }
    public <V extends MenuComponent> V add(V component){
        components.add(component);
        component.gui = gui;
        component.parent = this;
        component.onAdded();
        return component;
    }
    public void tick(){
        for(MenuComponent c : new ArrayList<>(components)) c.tick();
    }
    public void render(int millisSinceLastTick){
        renderBackground();
        for(MenuComponent component : components){
            component.render(millisSinceLastTick);
        }
        renderForeground();
    }
    public void renderBackground(){
        switch(gui.type){
            case GameHelper.MODE_2D:
            case GameHelper.MODE_HYBRID:
                drawRect(0, 0, Display.getWidth(), Display.getHeight(), ImageStash.instance.getTexture("/gui/menuBackground.png"));
                break;
            case GameHelper.MODE_2D_CENTERED:
                drawRect(-Display.getWidth()/2, -Display.getHeight()/2, Display.getWidth()/2, Display.getHeight()/2, ImageStash.instance.getTexture("/gui/menuBackground.png"));
                break;
            case GameHelper.MODE_3D:
                drawRect(-Display.getWidth()/Display.getHeight(), -1, Display.getWidth()/Display.getHeight(), 1, ImageStash.instance.getTexture("/gui/menuBackground.png"));
                break;
            default:
                throw new AssertionError(gui.type);
        }
    }
    public void renderForeground(){}
    public void buttonClicked(MenuComponentButton button){
        throw new UnsupportedOperationException("Override missing- "+getClass().getName()+" has buttons but never handles events!");
    }
    @Deprecated
    public void listButtonClicked(ListComponentButton button){
        throw new UnsupportedOperationException("WARNING:  "+getClass().getName()+" uses deprecated ListComponentsButtons!");
    }
    public boolean onTabPressed(MenuComponent component){return false;}
    public boolean onReturnPressed(MenuComponent component){return false;}
    public void keyboardEvent(char character, int key, boolean pressed, boolean repeat){
        if(selected!=null){
            selected.keyboardEvent(character, key, pressed, repeat);
        }
    }
    public void processKeyboard(char character, int key, boolean pressed, boolean repeat){}
    public void mouseEvent(int button, boolean pressed, float x, float y, float xChange, float yChange, int wheelChange){
        boolean found = false;
        for(MenuComponent component : components){
            if(isClickWithinBounds(x, y, component.x, component.y, component.x+component.width, component.y+component.height)){
                component.mouseEvent(button, pressed, x-(float)component.x, y-(float)component.y, xChange, yChange, wheelChange);
                found = true;
                if(button>=0&&button<3&&gui.mouseWereDown.contains(button)!=pressed){
                    selected = component;
                }
            }else{
                component.mouseover(-1, -1, false);
            }
            component.isSelected = selected==component;
        }
        if(selected!=null&&wheelChange!=0){
            selected.mouseWheelChange(wheelChange);
        }
    }
    public void mouseWheelChange(int wheelChange){}
    public void onGUIOpened(){}
    public void onGUIClosed(){}
    public void persistMouseEvent(int button, boolean pressed, float x, float y) {
        for(MenuComponent component : components){
            if(isClickWithinBounds(x, y, component.x, component.y, component.x+component.width, component.y+component.height)){
                component.persistMouseEvent(button, pressed, x-(float)component.x, y-(float)component.y);
            }
        }
    }
}
