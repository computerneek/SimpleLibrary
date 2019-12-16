package simplelibrary.opengl.gui.components;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import simplelibrary.opengl.gui.Menu;
/**
 * A component for a SimpleLibrary OpenGL Menu.
 * @author Bryan
 */
public abstract class MenuComponent extends Menu{
    /**
     * The color of the component
     */
    public Color color=Color.WHITE;
    /**
     * The height of the component, in GL coordinates
     */
    public double height;
    /**
     * Whether or not the mouse is over the component
     */
    public boolean isMouseOver;
    /**
     * Whether or not the component is selected.  This is set by the parent menu when the component is selected or deselected.
     */
    public boolean isSelected;
    /**
     * The color of the component when it is selected
     */
    public Color selectedColor = Color.WHITE;
    /**
     * The width of the component, in GL coordinates
     */
    public double width;
    /**
     * The X location of the component, in GL coordinates
     */
    public double x;
    /**
     * The Y location of the component, in GL coordinates
     */
    public double y;
    /**
     * Creates a new component.  Note:  This is an abstract class.
     * @param x The X location, in GL coordinates
     * @param y The Y location, in GL coordinates
     * @param width The width, in GL coordinates
     * @param height The height, in GL coordinates
     */
    public MenuComponent(double x, double y, double width, double height){
        super(null, null);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    /**
     * Draws the component on the screen.  This adjusts the color and calls the <code>render</code> method.
     */
    public void draw(){
        if(!isSelected&&color!=Color.WHITE){
            GL11.glColor3f(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F);
        }else if(isSelected&&selectedColor!=Color.WHITE){
            GL11.glColor3f(selectedColor.getRed()/255F, selectedColor.getGreen()/255F, selectedColor.getBlue()/255F);
        }
        render();
        if((!isSelected&&color!=Color.WHITE)||(isSelected&&selectedColor!=Color.WHITE)){
            GL11.glColor3f(1, 1, 1);
        }
    };
    public void onAdded(){
        gui = parent.gui;
        for(MenuComponent c : components){
            c.onAdded();
        }
    }
    /**
     * Called when the mouse is dragged on the component
     * @param x The mouse X location 
     * @param y
     * @param button
     */
    public void mouseDragged(double x, double y, int button){}
    /**
     * Called when a mouse event occurs on the component, as different from a subcomponent.
     * @param x The mouse X location relative to the component, in GL coordinates
     * @param y The mouse Y location relative to the component, in GL coordinates
     * @param button Which mouse button has produced the event (-1 is no button, 0 is left click, 1 is middle click, 2 is right click)
     * @param isDown If the acting button is up or down.  Will always be <code>false</code> when the button is -1.
     */
    public void mouseEvent(double x, double y, int button, boolean isDown){}
    public void mouseover(double x, double y, boolean isMouseOver){
        this.isMouseOver = isMouseOver;
        if(!isSelected&&selected!=null){
            selected.isSelected = false;
            selected = null;
        }
        for(MenuComponent c : components){
            c.mouseover(x-c.x, y-c.y, isMouseOver&&x>=c.x&&y>=c.y&&x<=c.x+c.width&&y<=c.y+c.height);
        }
    }
    /**
     * Called when a keyboard event occurs while this component is selected.
     * @param character The character that was typed; may be -1 if no character was typed, may also be unprintable
     * @param key The key that was pressed (LWJGL indexes; use the variables from <code>org.lwjgl.input.Keyboard</code>)
     * @param pressed If the key is down
     * @param repeat If this is a repeat event from a key being held down
     */
    public void processKeyboard(char character, int key, boolean pressed, boolean repeat){
        parent.processKeyboard(character, key, pressed, repeat);
    }
    /**
     * Draws this component on the screen
     */
    public abstract void render();
    public MenuComponent setSelectedColor(Color color){
        selectedColor = color;
        return this;
    }
    public MenuComponent setColor(Color color){
        this.color = color;
        return this;
    }
    public void setLocation(Point p) {
        x = p.x;
        y = p.y;
    }
    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void setSize(Dimension d) {
        width = d.width;
        height = d.height;
    }
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    public void setBounds(Rectangle r) {
        x = r.x;
        y = r.y;
        width = r.width;
        height = r.height;
    }
    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public void buttonClicked(MenuComponentButton button){
        if(parent!=null) parent.buttonClicked(button);
        else throw new UnsupportedOperationException("Override missing- "+getClass().getName()+" has buttons but never handles events!");
    }
    public void listButtonClicked(ListComponentButton button){
        if(parent!=null) parent.listButtonClicked(button);
        else throw new UnsupportedOperationException("Override missing- "+getClass().getName()+" has list buttons but never handles events!");
    }
    public boolean onTabPressed(MenuComponent component){return false;}
    public boolean onReturnPressed(MenuComponent component){return false;}
    public void keyboardEvent(char character, int key, boolean pressed, boolean repeat){
        if(selected!=null){
            selected.keyboardEvent(character, key, pressed, repeat);
        }
        else{
            processKeyboard(character, key, pressed, repeat);
        }
    }
    public void mouseEvent(int button, boolean pressed, float x, float y, float xChange, float yChange, int wheelChange){
        boolean found = false;
        for(MenuComponent component : components){
            if(isClickWithinBounds(x, y, component.x, component.y, component.x+component.width, component.y+component.height)){
                component.mouseEvent(button, pressed, x-(float)component.x, y-(float)component.y, xChange, yChange, wheelChange);
                found = true;
                if(button>=0&&button<3&&gui.mouseWereDown.contains(button)!=pressed){
                    selected = component;
                    component.isSelected = true;
                }
            }else{
                if(button>=0&&button<3&&gui.mouseWereDown.contains(button)!=pressed){
                    if(selected==component){
                        selected=null;
                    }
                    component.isSelected=false;
                }
                component.mouseover(-1, -1, false);
            }
        }
        if(wheelChange!=0&&!found){
            if(selected==null||!selected.mouseWheelChange(wheelChange)){
                mouseWheelChange(wheelChange);
            }
        }
        if(!found){
            mouseover(x, y, true);
            for(int i = 0; i<3; i++){
                if(button==i&&gui.mouseWereDown.contains(button)!=pressed){
                    mouseEvent(x, y, i, pressed);
                }else if(button==-1&&Mouse.isButtonDown(i)){
                    mouseDragged(x, y, i);
                }
            }
        }
    }
    @Override
    public void render(int millisSinceLastTick) {
        if(addRenderBound(x, y, x+width, y+height)){//Accounts for position and size-restriction; also, if component is offscreen, don't draw.
            renderBackground();
            draw();//renderBackground() and draw() functions can both be used to draw the component
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, 0);
            translate(x, y);//Adjust limit grid to match parents...  (Translate never fails when addRenderBound returns 'true')
            for(MenuComponent c : components){
                c.render(millisSinceLastTick);
            }
            removeRenderBound();//Reverse the translate
            removeRenderBound();//Reverse the pos/size restrictions
            GL11.glPopMatrix();
            renderForeground();//Anything to draw OVER components
        }
    }
    @Override
    public void renderBackground() {}
    @Override
    public void renderForeground(){}
}
