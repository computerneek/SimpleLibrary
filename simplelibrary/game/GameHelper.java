package simplelibrary.game;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.opengl.ImageStash;
import simplelibrary.opengl.Renderer2D;
import simplelibrary.window.WindowHelper;
public class GameHelper extends Thread{
    //<editor-fold defaultstate="collapsed" desc="variables">
    public static final int MODE_2D = 1;
    public static final int MODE_2D_CENTERED = 2;
    /**
     * GUI.render() automatically switches (effectively, at least) to MODE_2D_CENTERED and back
     */
    public static final int MODE_3D = 3;
    /**
     * World is rendered in MODE_3D
     * GUI is rendered in MODE_2D
     */
    public static final int MODE_HYBRID = 4;
    private static final Logger LOG = Logger.getLogger(GameHelper.class.getName());
    private int height = 500;
    private int width = 800;
    private boolean usesControllers;
    private boolean usesMouse;
    private boolean usesKeyboard;
    private int framerateCap;
    private Method tickMethod;
    private Method renderMethod;
    private Method renderInitMethod;
    private Method tickInitMethod;
    private String windowTitle;
    public boolean running = true;
    private long lastTime;
    private Color background = Color.BLACK;
    private float frameOfView = 45;
    private int lastWidth;
    private int lastHeight;
    private boolean hasColorChanged;
    private Object tickInitObject;
    private Object renderInitObject;
    private Object tickObject;
    private Object renderObject;
    public JFrame frame;
    public Canvas canvas;
    private int mode = MODE_2D;
    private int tickRate = 20;
    public float guiScale = 1;
    private float lastGuiScale;
    private boolean self;
    private boolean fullscreen;
    private boolean wasFullscreen;
    private Thread renderThread;
    private long tickTime;
    private Method finalInitMethod;
    private Object finalInitObject;
    private boolean autoExitFullscreen = true;
    private int framesSinceFullscreen;
    private boolean lightingEnabled;
    private boolean depthTestEnabled;
    private boolean is2D;
    private boolean rebuildRenderSetup;
    private float minRenderDistance = 0.1F;
    private float maxRenderDistance = 100F;
    private boolean usingFramebuffer;
    private int stackDepthAtFramebuffer = -1;
    private int boundDepthAtFramebuffer = -1;
    private boolean was2DBeforeFramebuffer;
    private boolean lightingEnabledBeforeFramebuffer;
    private boolean depthTestEnabledBeforeFramebuffer;
    private boolean wasLightingEnabledBeforeFramebuffer;
    private boolean wasDepthTestEnabledBeforeFramebuffer;
    private Object timer = new Object();//Synchronization object to prevent data races in render loop timing
    //</editor-fold>
    public void setMaximumFramerate(int maxFramerate){
        framerateCap = maxFramerate;
    }
    public void setUsesControllers(boolean flag){
        usesControllers = flag;
    }
    public void setUsesMouse(boolean flag){
        usesMouse = flag;
    }
    public void setUsesKeyboard(boolean flag){
        usesKeyboard = flag;
    }
    public void setDisplaySize(Dimension size){
        setDisplaySize(size.width, size.height);
    }
    public void setDisplaySize(int displayWidth, int displayHeight){
        width = displayWidth;
        height = displayHeight;
    }
    public void setTickMethod(Method method){
        if(method!=null&&(method.getParameterTypes().length!=1||method.getParameterTypes()[0]!=boolean.class)){
            throw new IllegalArgumentException("Tick method must have a boolean type argument for if it is the final tick!");
        }
        tickMethod = method;
    }
    public void setRenderMethod(Method method){
        if(method!=null&&(method.getParameterTypes().length!=1||method.getParameterTypes()[0]!=int.class)){
            throw new IllegalArgumentException("Render method must have a int type argument for the time since the last tick!");
        }
        renderMethod = method;
    }
    @Deprecated
    public void setInitMethod(Method method){
        setRenderInitMethod(method);
    }
    public void setTickInitMethod(Method method){
        if(method!=null&&method.getParameterTypes().length!=0){
            throw new IllegalArgumentException("Tick init method can take no arguments!");
        }
        tickInitMethod = method;
    }
    public void setFinalInitMethod(Method method){
        if(method!=null&&method.getParameterTypes().length!=0){
            throw new IllegalArgumentException("Final init method can take no arguments!");
        }
        finalInitMethod = method;
    }
    public void setRenderInitMethod(Method method){
        if(method!=null&&method.getParameterTypes().length!=0){
            throw new IllegalArgumentException("Render init method can take no arguments!");
        }
        renderInitMethod = method;
    }
    public void setBackground(Color background){
        this.background = background;
        hasColorChanged = true;
    }
    public void setFrameOfView(float newFOV){
        frameOfView = newFOV;
    }
    public void setWindowTitle(String title){
        windowTitle = title;
    }
    public void setMode(int mode){
        this.mode = mode;
        lastHeight = 0;
        lastWidth = 0;
    }
    public void setMinRenderDistance(float minDistance){
        minRenderDistance = minDistance;
    }
    public void setRenderRange(float min, float max){
        minRenderDistance = min;
        maxRenderDistance = max;
    }
    @Deprecated
    public void setInitObject(Object obj){
        setRenderInitObject(obj);
    }
    public void setTickInitObject(Object obj){
        tickInitObject = obj;
    }
    public void setFinalInitObject(Object obj){
        finalInitObject = obj;
    }
    public void setRenderInitObject(Object obj){
        renderInitObject = obj;
    }
    public void setTickObject(Object obj){
        tickObject = obj;
    }
    public void setRenderObject(Object obj){
        renderObject = obj;
    }
    @Deprecated
    public void setMillisPerTick(int millisPerTick){
        setTickRate((int)(1000f/millisPerTick));
    }
    public void setTickRate(int tickRate){
        this.tickRate = tickRate;
    }
    @Override
    public void start(){
        if(tickInitMethod==null&&renderInitMethod==null&&finalInitMethod==null){
            Sys.error(ErrorLevel.warning, "GameHelper Init is unused!  This is probably a bug.", null, ErrorCategory.bug, false);
        }
        if(tickMethod==null){
            Sys.error(ErrorLevel.warning, "GameHelper Tick Loop is unused!  This is probably a bug.", null, ErrorCategory.bug, false);
        }
        if(renderMethod==null){
            throw new IllegalStateException("ERROR:  Cannot create a GameHelper with no render loop!");
        }
        if(tickMethod!=null&&Modifier.isStatic(tickMethod.getModifiers())!=(tickObject==null)){
            throw new IllegalStateException("ERROR:  Tick method is "+(Modifier.isStatic(tickMethod.getModifiers())?"":"not ")+"static!");
        }
        if(renderMethod!=null&&Modifier.isStatic(renderMethod.getModifiers())!=(renderObject==null)){
            throw new IllegalStateException("ERROR:  Render method is "+(Modifier.isStatic(renderMethod.getModifiers())?"":"not ")+"static!");
        }
        if(tickInitMethod!=null&&Modifier.isStatic(tickInitMethod.getModifiers())!=(tickInitObject==null)){
            throw new IllegalStateException("ERROR:  Tick init method is "+(Modifier.isStatic(tickInitMethod.getModifiers())?"":"not ")+"static!");
        }
        if(renderInitMethod!=null&&Modifier.isStatic(renderInitMethod.getModifiers())!=(renderInitObject==null)){
            throw new IllegalStateException("ERROR:  Render init method is "+(Modifier.isStatic(renderInitMethod.getModifiers())?"":"not ")+"static!");
        }
        if(finalInitMethod!=null&&Modifier.isStatic(finalInitMethod.getModifiers())!=(finalInitObject==null)){
            throw new IllegalStateException("ERROR:  Final init method is "+(Modifier.isStatic(finalInitMethod.getModifiers())?"":"not ")+"static!");
        }
        setName("GameHelper Tick Thread");
        super.start();
    }
    @Override
    public void run(){
//            Runtime.getRuntime().addShutdownHook(new Thread(){
//                @Override
//                public void run(){
//                    Display.destroy();
//                    Controllers.destroy();
//                }
//            });
        callFunction("TickInit", tickInitMethod, tickInitObject, new Object[0]);
        synchronized(timer){
            tickTime = getTime();
        }
        synchronized(this){
            kickoffRenderThread();
            try {
                wait();
            } catch (InterruptedException ex) {}
        }
        callFunction("FinalInit", finalInitMethod, finalInitObject, new Object[0]);
        while(running){
            synchronized(timer){
                tickTime = getTime();
            }
            callFunction("Tick", tickMethod, tickObject, false);
            Display.sync(tickRate);
        }
        try {
            renderThread.join();
        } catch (InterruptedException ex) {}
        callFunction("FinalTick", tickMethod, tickObject, true);
    }
    private void kickoffRenderThread() {
        renderThread = new Thread(){
            public void run(){
                synchronized(GameHelper.this){
                    createDisplay();
                    setupControllers();
                    callFunction("renderInit", renderInitMethod, renderInitObject, new Object[0]);
                    GameHelper.this.notifyAll();
                }
                while(running){
                    try {
                        render();
                    } catch (LWJGLException ex) {}
                    if(framerateCap>0){
                        Display.sync(framerateCap);
                    }
                }
                Controllers.destroy();
                Display.destroy();
                if(self){
                    frame.setVisible(false);
                    frame.dispose();
                }
            }
        };
        renderThread.setName("GameHelper Render Thread");
        renderThread.start();
    }
    public void createDisplay(){
        try{
            if(canvas==null||!canvas.isVisible()){
                frame = WindowHelper.createFrameWithoutAppearance(windowTitle, width, height, null);
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                canvas = new Canvas();
                frame.add(canvas);
                self = true;
            }
            frame.setVisible(true);
            canvas.requestFocus();
            Display.setParent(canvas);
            if(background==null){
                background = Color.BLACK;
            }
            Display.setVSyncEnabled(true);
            Display.create();
            hasColorChanged = true;
        }catch(LWJGLException ex){
            Sys.error(ErrorLevel.severe, "Could not create display!", ex, ErrorCategory.bug);
        }
    }
    public void setupControllers(){
        if(!usesControllers&&!usesMouse&&!usesKeyboard){
            Sys.error(ErrorLevel.warning, "No input used!", null, ErrorCategory.bug);
        }
        if(usesControllers&&(usesMouse||usesKeyboard)){
            Sys.error(ErrorLevel.warning, "Using game controllers and mouse/keyboard at the same time is redundant!", null, ErrorCategory.bug);
            usesMouse = false;
            usesKeyboard = false;
        }
        if(usesControllers){
            try{
                Controllers.create();
            }catch(LWJGLException ex){
                Sys.error(ErrorLevel.critical, "Could not initialize controllers!", ex, ErrorCategory.LWJGL);
            }
        }
        if(usesMouse){
            try{
                Mouse.create();
            }catch(LWJGLException ex){
                Sys.error(ErrorLevel.critical, "Could not initialize mouse!", ex, ErrorCategory.LWJGL);
            }
        }
        if(usesKeyboard){
            try{
                Keyboard.create();
            }catch(LWJGLException ex){
                Sys.error(ErrorLevel.critical, "Could not initialize keyboard!", ex, ErrorCategory.LWJGL);
            }
        }
    }
    public boolean isFullscreen(){
        return fullscreen;
    }
    public void setFullscreen(boolean fullscreen){
        this.fullscreen = fullscreen;
    }
    public boolean willAutoExitFullscreen(){
        return autoExitFullscreen;
    }
    public void setAutoExitFullscreen(boolean autoExit){
        autoExitFullscreen = autoExit;
    }
    private long getTime(){
        return org.lwjgl.Sys.getTime()*1_000/org.lwjgl.Sys.getTimerResolution();
    }
    public void render() throws LWJGLException{
        if(autoExitFullscreen&&fullscreen&&!Display.isActive()&&framesSinceFullscreen>5){
            fullscreen = false;
        }
        framesSinceFullscreen++;
        if(wasFullscreen!=fullscreen){
            if(!fullscreen){
                frame.setVisible(true);
                Display.setParent(canvas);
                Display.setFullscreen(false);
                canvas.requestFocus();
            }else{
                Display.setFullscreen(true);
                frame.setVisible(false);
                framesSinceFullscreen = 0;
            }
            lastWidth = 0;
            lastHeight = 0;
            lastGuiScale = 0;
            wasFullscreen = fullscreen;
        }
        if(Display.getWidth()!=lastWidth||Display.getHeight()!=lastHeight||guiScale!=lastGuiScale||rebuildRenderSetup){
            if(is2D&&(mode==MODE_3D||mode==MODE_HYBRID)) make3D();//So we don't confuse ourselves
            rebuildRenderMode(mode, Display.getWidth(), Display.getHeight(), guiScale);
            lastWidth = Display.getWidth();
            lastHeight = Display.getHeight();
            lastGuiScale = guiScale;
        }
        if(hasColorChanged){
            refreshBackgroundColor();
        }
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glLoadIdentity();
        int tTime;
        synchronized(timer){
            tTime = (int)(getTime()-tickTime);
        }
        Object value = callFunction("Render", renderMethod, renderObject, tTime);
        if(value!=null&&value instanceof Boolean&&(boolean)value){
            return;
        }
        Display.update();
    }
    public void refreshBackgroundColor(){
        GL11.glClearColor(background.getRed()/255F, background.getGreen()/255F, background.getBlue()/255F, background.getAlpha()/255F);
        hasColorChanged = false;
    }
    private void rebuildRenderMode(int mode, int width, int height, float guiScale) {
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        switch(mode){
            case MODE_3D:
            case MODE_HYBRID:
                float aspect = width / (float)height;
                GL11.glOrtho(-guiScale, guiScale, -guiScale, guiScale, -minRenderDistance, -maxRenderDistance);
                GLU.gluPerspective(frameOfView, aspect, (float)minRenderDistance, (float)maxRenderDistance);
                break;
            case MODE_2D:
                GL11.glOrtho(0, width*guiScale, height*guiScale, 0, 0, 100F);
                break;
            case MODE_2D_CENTERED:
                GL11.glOrtho(-width*guiScale/2f, width*guiScale/2f, height*guiScale/2f, -height*guiScale/2f, 0, 100F);
                break;
            default:
                break;
        }
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }
    public void make2D() {
        if(mode!=MODE_3D&&mode!=MODE_HYBRID){
            throw new IllegalStateException("Can only be made 2D-3D or back in a 3D mode!");
        }
        if(is2D) return;
        is2D = true;
        //Remove the Z axis
        lightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_LIGHTING);
        depthTestEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        if(mode==MODE_3D){
            float width = Display.getWidth()/(float)Display.getHeight()*guiScale;
            GL11.glOrtho(-width, width, guiScale, -guiScale, -1, 1);
        }else{
            GL11.glOrtho(0, Display.getWidth()*guiScale, Display.getHeight()*guiScale, 0, 0, 100F);
        }
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }
    public void make3D() {
        if(mode!=MODE_3D&&mode!=MODE_HYBRID){
            throw new IllegalStateException("Can only be made 2D-3D or back in a 3D mode!");
        }
        if(!is2D) return;
        is2D = false;
        //Restore the Z axis
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        if(depthTestEnabled){
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        }
        if(lightingEnabled){
            GL11.glEnable(GL11.GL_LIGHTING);
        }
    }
    private Object callFunction(String name, Method method, Object obj, Object... params){
        try{
            if(method!=null){
                return method.invoke(obj, params);
            }
        }catch(IllegalAccessException ex){
            Sys.error(ErrorLevel.severe, "Access denied!", ex, ErrorCategory.bug);
        }catch(IllegalArgumentException ex){
            Sys.error(ErrorLevel.severe, "Bug in SimpleLibrary- Illegal method managed to get in!", ex, ErrorCategory.bug);
        }catch(InvocationTargetException ex){
            Sys.error(ErrorLevel.severe, name+" Error!", ex.getCause(), ErrorCategory.uncaught);
        }catch(NullPointerException ex){
            Sys.error(ErrorLevel.severe, name+" method is not static and render object is null!", ex, ErrorCategory.bug);
        }
        return null;
    }
    public void toggleFullscreen() {
        setFullscreen(!fullscreen);
    }
    /**
     * Sets the render target to the specified framebuffer.
     * NOTE:  GL11.glPushMatrix() and GL11.glPopMatrix() are used to store current render state.
     * Do not use glPopMatrix() without a matching glPushMatrix() while in Framebuffer; however, pushing without popping is handled, within the stack depth.
     * NOTE:  When releasing Framebuffer (returning to default), glClearColor is reset to background value, but glColor is NOT reset!
     * NOTE:  Various options (depth test, lighting, etc.) are NOT reset or changed when resetting the render mode for the framebuffer.  HOWEVER,
     * depth test and lighting (specifically) ARE reset to their pre-framebuffer values upon release.
     * NOTE:  If the GameHelper is not in framebuffer mode and this function is used to reset the render target, the render setup is reset on the next frame.
     * This includes 2D-3D state when operating in MODE_3D.
     * WARNING:  Bugs likely when using multiple framebuffers (without reverting to original between) and changing 2D-3D state in them (without resetting before progressing).
     * WARNING:  Ensure there is space on both the PROJECTION and MODELVIEW stacks BEFORE entering Framebuffer mode.  Otherwise, this may fail!
     * @param name Name of Framebuffer to use (0=no framebuffer, revert to original)
     * @param renderMode Render mode to use for framebuffer rendering (MODE_2D, etc; 0=no change)
     * @param width Width of render field.  MUST match the framebuffer's buffer size!
     * @param height Height of render field.  MUST match the framebuffer's buffer size!
     * @param guiScale GUI scale to use for the framebuffer; GL coordinate of top of screen (bottom=-guiScale) in 3D mode; in 2D modes, GL coordinate sizes = size/guiScale
     */
    public void renderTargetFramebuffer(int name, int renderMode, int width, int height, float guiScale){
        if(name==0&&usingFramebuffer){
            GL11.glViewport(0, 0, lastWidth, lastHeight);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            if(GL11.glGetInteger(GL11.GL_PROJECTION_STACK_DEPTH)>1) GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            while(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)>stackDepthAtFramebuffer) GL11.glPopMatrix();
            while(Renderer2D.getBoundStackDepth()>boundDepthAtFramebuffer) Renderer2D.popBoundStack();
            if(depthTestEnabledBeforeFramebuffer) GL11.glEnable(GL11.GL_DEPTH_TEST);
            else GL11.glDisable(GL11.GL_DEPTH_TEST);
            if(lightingEnabledBeforeFramebuffer) GL11.glEnable(GL11.GL_LIGHTING);
            else GL11.glDisable(GL11.GL_LIGHTING);
            depthTestEnabled = wasDepthTestEnabledBeforeFramebuffer;
            lightingEnabled = wasLightingEnabledBeforeFramebuffer;
            is2D = was2DBeforeFramebuffer;
            stackDepthAtFramebuffer = -1;
            ImageStash.instance.bindBuffer(name);
            hasColorChanged = true;
            usingFramebuffer = false;
        }else if(name==0){
            ImageStash.instance.bindBuffer(name);//We didn't set it into framebuffer mode- make no assumptions about configuration, just rebuild.
            hasColorChanged = true;
            rebuildRenderSetup = true;
        }else{
            if(!usingFramebuffer){
                depthTestEnabledBeforeFramebuffer = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
                lightingEnabledBeforeFramebuffer = GL11.glIsEnabled(GL11.GL_LIGHTING);
                wasDepthTestEnabledBeforeFramebuffer = depthTestEnabled;
                wasLightingEnabledBeforeFramebuffer = lightingEnabled;
                was2DBeforeFramebuffer = is2D;
            }
            is2D = false;
            stackDepthAtFramebuffer = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
            boundDepthAtFramebuffer = Renderer2D.getBoundStackDepth();
            Renderer2D.pushAndClearBoundStack();
            usingFramebuffer = true;
            GL11.glPushMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glPushMatrix();
            if(renderMode>0) rebuildRenderMode(renderMode, width, height, guiScale);
            else GL11.glMatrixMode(GL11.GL_MODELVIEW);
            ImageStash.instance.bindBuffer(name);
            if(renderMode==MODE_2D){
                GL11.glScalef(1, -1, 1);
                GL11.glTranslatef(0, -height, 0);//Don't ask why, it just works
            }
        }
    }
}
