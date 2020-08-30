package simplelibrary.opengl.gui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.lwjgl.glfw.GLFW;
import simplelibrary.Queue;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.game.GameHelper;
/**
 * An OpenGL GUI system, designed to be accurate 
 * @author Bryan
 */
public class GUI{
    private static final Logger LOG=Logger.getLogger(GUI.class.getName());
    /**
     * The GameHelper that this GUI is running on
     */
    public final GameHelper helper;
    /**
     * The keyboard keys that were down last update
     */
    public ArrayList<Integer> keyboardWereDown = new ArrayList<>();
    /**
     * The currently open menu.  DO NOT MODIFY.  Use <code>open(Menu)</code> instead.
     */
    public Menu menu;
    /**
     * The mouse buttons that were down last update
     */
    public ArrayList<Integer> mouseWereDown = new ArrayList<>();
    /**
     * A counter variable, incremented every tick.  Used for caret flashing.
     */
    public int tick;
    /**
     * The type of the GUI.  See <code>GameHelper.TYPE_2D, GameHelper.TYPE_2D_CENTERED, GameHelper.TYPE_3D</code>
     */
    public int type;
    public double mouseX;
    public double mouseY;
    /**
     * @param type The type of the GUI.  See <code>GameHelper.TYPE_2D, GameHelper.TYPE_2D_CENTERED, GameHelper.TYPE_3D</code>
     * @param helper The helper that the GUI is running on.  Can be null.  Used for automatic type shifting whenever the helper's type changes.
     */
    public GUI(int type, GameHelper helper){
        this.type = type;
        this.helper=helper;
    }
    /**
     * Opens the specified menu.  This follows through with the proper close-switch-open sequence.
     * @param <V> Return type.  This used to be a plain <code>Menu</code>, but casting every time became too tedious.
     * @param menu The menu to open
     * @return The menu that was opened; equal to the parameter <code>menu</code>
     */
    public <V extends Menu> V open(Menu menu){
        if(this.menu!=null){
            this.menu.onGUIClosed();
        }
        this.menu = menu;
        if(menu!=null) menu.onGUIOpened();
        return (V)menu;
    }
    /**
     * Renders the menu, if any; also does the controller polling.
     * @param millisSinceLastTick The number of milliseconds since the last tick.  Can be any nonnegative number.
     */
    public synchronized void render(int millisSinceLastTick){
        if(millisSinceLastTick<0){
            throw new IllegalArgumentException("The most recent tick can't be in the future!");
        }
        Exception theException = null;
        doActions();
        if(type==GameHelper.MODE_3D||type==GameHelper.MODE_HYBRID){
            helper.make2D();
        }
        if(menu!=null){
            try{
                menu.render(millisSinceLastTick);
            }catch(Exception ex){
                theException = ex;
            }
        }
        if(type==GameHelper.MODE_3D||type==GameHelper.MODE_HYBRID){
            helper.make3D();
        }
        if(theException!=null){
            throw new RuntimeException(theException);
        }
    }
    public void onMouseMoved(double xpos, double ypos) {
        double x = xpos;
        double y = ypos;
        switch(type){
            case GameHelper.MODE_2D:
            case GameHelper.MODE_HYBRID:
                break;
            case GameHelper.MODE_2D_CENTERED:
                x-=helper.displayWidth()/2f;
                y-=helper.displayHeight()/2f;
                break;
            case GameHelper.MODE_3D:
                x/=helper.displayHeight()/2f;
                y/=helper.displayHeight()/2f;
                x-=(float)helper.displayWidth()/helper.displayHeight();
                y--;
        }
        x*=helper.guiScale;
        y*=helper.guiScale;
        mouseX = x;
        mouseY = y;
        if(menu!=null) menu.onMouseMove(x, y);
    }
    public void onMouseButton(int button, int action, int mods) {
        if(GLFW.GLFW_PRESS==action&&!mouseWereDown.contains(button)){
            mouseWereDown.add(button);
        }else if(action==GLFW.GLFW_RELEASE&&mouseWereDown.contains(button)){
            mouseWereDown.remove((Integer)button);
        }
        if(menu!=null) menu.onMouseButton(button, action==GLFW.GLFW_PRESS, mods);
    }
    public void onMouseScrolled(double xoffset, double yoffset) {
        if(menu!=null) menu.onMouseScrolled(xoffset, yoffset);
    }
    public void onKeyEvent(int key, int scancode, int event, int modifiers) {
        if(event==GLFW.GLFW_PRESS&&!keyboardWereDown.contains(key)){
            keyboardWereDown.add(key);
        }else if(event==GLFW.GLFW_RELEASE&&keyboardWereDown.contains(key)){
            keyboardWereDown.remove((Integer)key);
        }
        if(menu!=null) menu.keyEvent(key, scancode, GLFW.GLFW_PRESS==event, GLFW.GLFW_REPEAT==event, modifiers);
    }
    public void onCharTyped(char c) {
        if(menu!=null) menu.onCharTyped(c);
    }
    /**
     * Increments the <code>tick</code> variable and ticks the menu
     */
    public synchronized  void tick(){
        tick++;
        if(menu!=null){
            try{
                menu.tick();
            }catch(Throwable throwable){
                Sys.error(ErrorLevel.severe, "Could not tick GUI!", new RuntimeException(throwable), ErrorCategory.other);
            }
        }
    }
//    public void setFullscreenKey(int key){
//        fullscreenKey = key;
//        hasFullscreenKey = true;
//    }
//    public void clearFullscreenKey(){
//        hasFullscreenKey = false;
//    }
//    public boolean hasFullscreenKey(){
//        return hasFullscreenKey;
//    }
//    public int getFullscreenKey(){
//        return fullscreenKey;
//    }
    private static class PendingAction{
        private final ActionListener l;
        private final ActionEvent e;
        private PendingAction(ActionListener l, ActionEvent e){
            this.l = l;
            this.e = e;
        }
        private void perform(){
            l.actionPerformed(e);
        }
    }
    private Queue<PendingAction> pending = new Queue<>();
    public void addPendingAction(ActionListener l, ActionEvent e){
        pending.enqueue(new PendingAction(l, e));
    }
    private void doActions(){
        while(!pending.isEmpty()){
            pending.dequeue().perform();
        }
    }
    public void onWindowFocused(boolean focused) {
        if(menu!=null) menu.onWindowFocused(focused);
    }
    public void onFileDropped(String[] files) {
        if(menu!=null) menu.onFilesDropped(files);
    }
}
