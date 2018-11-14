package simplelibrary.opengl.gui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
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
    private int fullscreenKey;
    private boolean hasFullscreenKey;
    public float mouseX;
    public float mouseY;
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
        theException=processKeyboard(theException);
        theException=processMouse(theException);
        if(theException!=null){
            throw new RuntimeException(theException);
        }
        doActions();
    }
    private Exception processMouse(Exception theException){
        boolean needsStatic = !mouseWereDown.isEmpty();
        while(Mouse.next()){
            needsStatic = false;
            int button = Mouse.getEventButton();
            boolean pressed = Mouse.getEventButtonState();
            int wheelChange = Mouse.getEventDWheel();
            int xChange = Mouse.getEventDX();
            int yChange = -Mouse.getEventDY();
            float x = Mouse.getEventX();
            float y = Display.getHeight()-Mouse.getEventY();
            if(menu!=null){
                switch(type){
                    case GameHelper.MODE_2D:
                    case GameHelper.MODE_HYBRID:
                        break;
                    case GameHelper.MODE_2D_CENTERED:
                        x-=Display.getWidth()/2;
                        y-=Display.getHeight()/2;
                        break;
                    case GameHelper.MODE_3D:
                        x/=Display.getHeight()/2;
                        y/=Display.getHeight()/2;
                        x-=(float)Display.getWidth()/Display.getHeight();
                        y--;
                        break;
                }
                if(helper!=null&&GameHelper.MODE_3D!=type){
                    x/=helper.guiScale;
                    y/=helper.guiScale;
                }else if(helper!=null){
                    x*=helper.guiScale;
                    y*=helper.guiScale;
                }
                try{
                    mouseX = x;
                    mouseY = y;
                    mouseEvent(button, pressed, x, y, xChange, yChange, wheelChange);
                }catch(Exception ex){
                    theException = ex;
                }
            }
            if(pressed&&!mouseWereDown.contains(button)){
                mouseWereDown.add(button);
            }else if(!pressed&&mouseWereDown.contains(button)){
                mouseWereDown.remove((Integer)button);
            }
        }
        if(needsStatic&&menu!=null){
            for(int button : mouseWereDown){
                boolean pressed = true;
                float x = Mouse.getX();
                float y = Display.getHeight()-Mouse.getY();
                switch(type){
                    case GameHelper.MODE_2D:
                    case GameHelper.MODE_HYBRID:
                        break;
                    case GameHelper.MODE_2D_CENTERED:
                        x-=Display.getWidth()/2;
                        y-=Display.getHeight()/2;
                        break;
                    case GameHelper.MODE_3D:
                        x/=Display.getHeight()/2;
                        y/=Display.getHeight()/2;
                        x-=(float)Display.getWidth()/Display.getHeight();
                        y--;
                        break;
                }
                if(helper!=null&&GameHelper.MODE_3D!=type){
                    x/=helper.guiScale;
                    y/=helper.guiScale;
                }else if(helper!=null){
                    x*=helper.guiScale;
                    y*=helper.guiScale;
                }
                try{
                    persistMouseEvent(button, pressed, x, y);
                }catch(Exception ex){
                    theException = ex;
                }
            }
        }
        return theException;
    }
    protected void persistMouseEvent(int button, boolean pressed, float x, float y){
        menu.persistMouseEvent(button, pressed, x, y);
    }
    protected void mouseEvent(int button, boolean pressed, float x, float y, int xChange, int yChange, int wheelChange){
        menu.mouseEvent(button, pressed, x, y, xChange, yChange, wheelChange);
    }
    private Exception processKeyboard(Exception theException){
        while(Keyboard.next()){
            char character = Keyboard.getEventCharacter();
            int key = Keyboard.getEventKey();
            boolean pressed = Keyboard.getEventKeyState();
            boolean isRepeat = Keyboard.isRepeatEvent();
            if(menu!=null){
                if(hasFullscreenKey&&key==fullscreenKey&&pressed&&!isRepeat){
                    helper.toggleFullscreen();
                }
                try{
                    keyboardEvent(character, key, pressed, isRepeat);
                }catch(Exception ex){
                    theException = ex;
                }
            }
            if(pressed&&!keyboardWereDown.contains(key)){
                keyboardWereDown.add(key);
            }else if(!pressed&&keyboardWereDown.contains(key)){
                keyboardWereDown.remove((Integer)key);
            }
        }
        return theException;
    }
    protected void keyboardEvent(char character, int key, boolean pressed, boolean isRepeat){
        menu.keyboardEvent(character, key, pressed, isRepeat);
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
    public void setFullscreenKey(int key){
        fullscreenKey = key;
        hasFullscreenKey = true;
    }
    public void clearFullscreenKey(){
        hasFullscreenKey = false;
    }
    public boolean hasFullscreenKey(){
        return hasFullscreenKey;
    }
    public int getFullscreenKey(){
        return fullscreenKey;
    }
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
}
