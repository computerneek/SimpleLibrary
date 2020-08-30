package simplelibrary.opengl.gui;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import static org.lwjgl.glfw.GLFW.*;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
import simplelibrary.opengl.Renderer2D;
import simplelibrary.opengl.gui.components.MenuComponent;
import simplelibrary.opengl.gui.components.MenuComponentButton;
public abstract class Menu extends Renderer2D{
    private static boolean hasBackgroundTexture;
    public Menu parent;
    public ArrayList<MenuComponent> components = new ArrayList<>();
    public MenuComponent selected;
    public GUI gui;
    private static final String menuBackground = "/gui/menuBackground.png";
    static{
        try(InputStream in = Menu.class.getResourceAsStream(menuBackground)){
            if(in!=null) hasBackgroundTexture = true;
        }catch(IOException ex){
        }
    }
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
        for(int i = components.size()-1; i>=0; i--){
            if(i<components.size()) components.get(i).tick();
        }
    }
    public void render(int millisSinceLastTick){
        renderBackground();
        for(MenuComponent component : components){
            component.render(millisSinceLastTick);
        }
        renderForeground();
    }
    public void renderBackground(){
        if(!hasBackgroundTexture) return;
        switch(gui.type){
            case GameHelper.MODE_2D:
            case GameHelper.MODE_HYBRID:
                drawRect(0, 0, gui.helper.displayWidth(), gui.helper.displayHeight(), ImageStash.instance.getTexture(menuBackground));
                break;
            case GameHelper.MODE_2D_CENTERED:
                drawRect(-gui.helper.displayWidth()/2, -gui.helper.displayHeight()/2, gui.helper.displayWidth()/2, gui.helper.displayHeight()/2, ImageStash.instance.getTexture(menuBackground));
                break;
            case GameHelper.MODE_3D:
                drawRect(-gui.helper.displayWidth()/gui.helper.displayHeight(), -1, gui.helper.displayWidth()/gui.helper.displayHeight(), 1, ImageStash.instance.getTexture(menuBackground));
                break;
            default:
                throw new AssertionError(gui.type);
        }
    }
    public void renderForeground(){}
    public void buttonClicked(MenuComponentButton button){
        throw new UnsupportedOperationException("Override missing- "+getClass().getName()+" has buttons but never handles events!");
    }
    public boolean onTabPressed(MenuComponent component){return false;}
    public boolean onReturnPressed(MenuComponent component){return false;}
    public void onGUIOpened(){}
    public void onGUIClosed(){}
    public void keyEvent(int key, int scancode, boolean isPress, boolean isRepeat, int modifiers) {
        if(selected!=null){
            selected.keyEvent(key, scancode, isPress, isRepeat, modifiers);
        }
    }
    public void onCharTyped(char c){
        if(selected!=null){
            selected.onCharTyped(c);
        }
    }
    public void onMouseMove(double x, double y) {
        for(MenuComponent component : components){
            if(isClickWithinBounds(x, y, component.x, component.y, component.x+component.width, component.y+component.height)){
                component.onMouseMove(x-component.x, y-component.y);
            }else{
                component.onMouseMovedElsewhere(x-component.x, y-component.y);
            }
        }
    }
    void onMouseButton(int button, boolean pressed, int mods) {
        onMouseButton(gui.mouseX, gui.mouseY, button, pressed, mods);
    }
    public void onMouseButton(double x, double y, int button, boolean pressed, int mods){
        boolean clicked = false;
        for(int i = components.size()-1; i>=0; i--){
            if(i>=components.size()) continue;
            MenuComponent component = components.get(i);
            if(!Double.isNaN(x)&&!clicked&&isClickWithinBounds(x, y, component.x, component.y, component.x+component.width, component.y+component.height)){
                if(selected!=component&&pressed&&button==0){
                    if(selected!=null) selected.onDeselected();
                    selected = component;
                    component.onSelected();
                }
                clicked = true;
                component.onMouseButton(x-component.x, y-component.y, button, pressed, mods);
            }else if(!pressed){
                component.onMouseButton(Double.NaN, Double.NaN, button, false, mods);
            }
        }
    }
    void onMouseScrolled(double dx, double dy) {
        onMouseScrolled(gui.mouseX, gui.mouseY, dx, dy);
    }
    public boolean onMouseScrolled(double x, double y, double dx, double dy){
        //Pass scrollwheel event first to whatever the mouse is over, then to the selected component.
        for(MenuComponent component : components){
            if(isClickWithinBounds(x, y, component.x, component.y, component.x+component.width, component.y+component.height)){
                if(component.onMouseScrolled(x-component.x, y-component.y, dx, dy)) return true;
            }
        }
        return false;
    }
    public void onWindowFocused(boolean focused) {}
    void onFilesDropped(String[] files){
        onFilesDropped(gui.mouseX, gui.mouseY, files);
    }
    public boolean onFilesDropped(double x, double y, String[] files){
        for(MenuComponent component : components){
            if(isClickWithinBounds(x, y, component.x, component.y, component.x+component.width, component.y+component.height)){
                if(component.onFilesDropped(x-component.x, y-component.y, files)) return true;
            }
        }
        return false;
    }
}
