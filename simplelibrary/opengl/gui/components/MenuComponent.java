package simplelibrary.opengl.gui.components;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
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
     * The color of foreground components.
     */
    public Color foregroundColor = defaultForegroundColor();
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
    public MenuComponent setForegroundColor(Color color){
        this.foregroundColor = color;
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
    public boolean onTabPressed(MenuComponent component){return false;}
    public boolean onReturnPressed(MenuComponent component){return false;}
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
    protected Color defaultForegroundColor(){
        return Color.WHITE;
    }
    public void onMouseMove(double x, double y){
        isMouseOver = true;
        for(MenuComponent c : components){
            if(isClickWithinBounds(x, y, c.x, c.y, c.x+c.width, c.y+c.height)){
                c.onMouseMove(x-c.x, y-c.y);
            }else{
                c.onMouseMovedElsewhere(x-c.x, y-c.y);
            }
        }
    }
    public void onMouseMovedElsewhere(double x, double y) {
        //Only isMouseOver==false.  Doing much else, or anything unrelated to isMouseOver changing state, is not recommended.
        isMouseOver = false;
        for(MenuComponent c : components){
            c.onMouseMovedElsewhere(x-c.x, y-c.y);
        }
    }
    public void onDeselected() {
        //Something else was selected
        isSelected = false;
    }
    public void onSelected() {
        isSelected = true;
    }
}
