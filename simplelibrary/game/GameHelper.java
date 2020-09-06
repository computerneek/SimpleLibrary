package simplelibrary.game;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.opengl.ImageStash;
import simplelibrary.opengl.Renderer2D;
import simplelibrary.opengl.gui.GUI;
/**
 * An automatic tick & render management system, featuring full multithreading capability.
 * <hr>
 * The GameHelper can be constructed, configured, and managed on any thread- but, for the underlying GLFW backend (through LWJGL) to work correctly, <code>run()</code> MUST be called from the application main thread.
 * <p>Also, beware that <code>run()</code> will capture the main thread until the GameHelper is shut down.  Any thread can be used to manage the active tick and render operations.</p>
 * <p>If no tick or render setup was performed prior to calling <code>run()</code>, such that the GameHelper will not be doing anything, it will return immediately.</p>
 * <hr>
 * ----THE TICK SYSTEM----
 * <p>The GameHelper is actually equipped with two separate tick systems:  The Standard tick system, and the Pool tick system.  Both systems are internally synchronized, and will tick at the same rate;
 * both systems can be used at once, if desired.  Tick rate can be set with either <code>setTickRate(int)</code> or <code>setTickTime(long)</code>.  The master tick rate is controlled by the application main thread.</p>
 * <p>The Standard Tick System is activated and used by calling <code>addTickMethod(TickMethod)</code>.  Each new method is assigned its own new thread; single-threaded applications need only call it once.
 * If <code>addTickMethod()</code> is called before the GameHelper is launched with <code>run()</code>, the thread is not started immediately- rather, it is started during the initialization step.
 * A tick method created by <code>addTickMethod()</code> can be removed by setting its <code>TickMethod.terminate=true</code>; the thread will then self-terminate, and its <code>TickMethod</code> deleted.
 * If lost, the <code>TickMethod</code> can be retrieved by calling <code>getTickMethod()</code> <i>on the affected thread</i>.
 * If <code>addTickMethod()</code> is called after <code>run()</code>, its thread will be spawned on the next main thread tick.
 * When the <code>GameHelper</code> is shut down via <code>running==false</code>, all active Standard Tick System threads call <code>TickMethod.finalTick()</code> once before termination.
 * <code>TickMethod.finalTick()</code> is NOT called when a thread is terminated via <code>terminate=true</code>, only when the <code>GameHelper</code> is shut down- and a thread which has been terminated
 * will not call <code>TickMethod.finalTick()</code> on <code>GameHelper</code> shutdown.</p>
 * <p>The Pool Tick System is far more dynamic.  A thread pool is used to tick an arbitrary number of <code>TickObject</code>s; the number of objects can change dynamically from tick to tick by any scale,
 * without inducing thread creation or death events.  Objects can be added to the list at any time, and by any thread, via <code>addTickObject(TickObject)</code>; a reasonable effort is made to tick all objects in sync
 * with the main tick loop.  Objects can be removed by setting their <code>TickObject.dead=true</code>.  The number of threads in the pool can be adjusted with <code>setThreadPoolSize(int)</code>, default 0;
 * however, whenever there are any tick objects on the list, the thread pool will have a minimum size of 1.  Pool threads will self-terminate between tick operations if there are too many, and new threads will be
 * spawned as needed by the application main thread, maximum 1 per tick.
 * <code>TickObject</code>s are never informed of GameHelper shutdown, unlike <code>TickMethod</code>s in the Standard Tick System.  In exchange, the Pool Tick System can be placed into Turbo Mode via
 * <code>setPoolTurbo(boolean)</code>, where the Pool Tick System has an indefinite maximum tick rate.  This does not effect the Standard Tick System, which remains at the set rate.
 * When in Turbo Mode, the Pool Tick System will automatically yield to the Standard Tick System and any render threads,
 * preventing other parts of the application from being lagged down by excessive Pool activity.  The Pool Tick System is most efficient when the <code>TickObject</code>s all have at least some processing to do,
 * as all pool threads must use an exclusive lock when fetching a <code>TickObject</code> from the list or returning it.</p>
 * <hr>
 * ----THE RENDER SYSTEM----
 * <p>The GameHelper Render System can support any number of <code>Display</code>s.
 * As the GLFW backend in the underlying LWJGL library requires window creation, setup, and destruction events to be performed on the main thread, all such events are performed either when <code>run()</code>
 * is called, on the next main thread tick, or on GameHelper shutdown (destruction of all windows).  However, <i>any</i> thread can request such changes, by creating new <code>Display</code>s.
 * A single <code>Display</code> object can control multiple monitors; each window of each <code>Display</code> is assigned its own render thread and GUI object, allowing different menus to be displayed on
 * different windows.  GUI objects can be linked if desired, via <code>Display.link(GUI, GUI)</code> or <code>Display.linkAllGUIs()</code>, allowing the linked GUIs to render elements overtop each other-
 * and detect clicks on the same.
 * Note that all windows within the same <code>Display</code> run with exactly the same render function, and simply have different projections; as such, in multi-window rendering with a single Display, the
 * <code>render()</code> function may be invoked multiple times in parallel.  Any render thread may call <code>Display.getWindow()</code> to identify which window it is running on.</p>
 * @author Bryan
 */
public class GameHelper{
//    //<editor-fold defaultstate="collapsed" desc="Constants">
//    //</editor-fold>
//    //<editor-fold defaultstate="collapsed" desc="Variables">
//    /**
//     * If any thread is allowed to see the `running` variable as true.  Used for fatal errors, when a thread must be terminated.
//     */
//    private static boolean allowRunning = true;
//    /**
//     * All threads, both tick and render, will self-terminate gracefully when this is set to false.  Tick threads will be given a 'final tick' notification.  All open windows will be closed.
//     * This is set to <code>true</code> by the <code>run()</code> method.
//     */
//    private boolean running = false;
//    /**
//     * Desired number of worker threads in the thread pool driving the dynamic tick pool.
//     */
//    private int desiredTickPoolSize = 0;
//    /**
//     * Nanoseconds per tick.  Default 50mil, for 20 ticks per second.
//     */
//    private long nanosPerTick = 50_000_000;// 1/20 of a second; 20TPS
//    /**
//     * Nanosecond time of when the most recent tick started.  Main tick counter will wait for System.nanoTime()>=lastTickTime+nanosPerTick before authorizing the next tick.
//     */
//    private long lastTickTime = 0;
//    /**
//     * Internal tick counter
//     */
//    private long tickNum = 0;
//    /**
//     * Maximum amount of "lag" time allowed.  Recommend minimum of 1 tick in length.
//     * This is the amount of time by which the master tick counter is allowed to "lag" behind the point it's supposed to be.
//     * If it lags further than this, the "supposed to be" point is dragged back with it, and may result in slowing 
//     */
//    private long maxLagTicksNanos = 1_000_000_000;//1s max lag
//    /**
//     * Maximum number of ticks ahead that one thread or object is allowed to be than another.
//     * Negative values disable this function; objects/threads are then allowed to independently maintain appropriate tickrate.
//     * At all other values, for any arbitrary tick number X and misallign value N, any given tick thread/object is allowed to start tick X+N before other threads/objects have finished tick X,
//     * but NOT before other threads/objects have finished tick X-1.
//     * A value of 0 forces all threads/objects to complete one tick before any may start the next tick;
//     * a value of 1 allows the barrier between any two ticks to be blurred, but will not allow a third tick to be started until the first has been finished.
//     * At -1, the master tick timer 
//     */
//    private long maxMissalignTicks = -1;//Disable alignment forcing- if one thread locks up, don't let it lock everything else up.
//    /**
//     * Maximum number of nanoseconds a ticking thread or object is allowed to take before it is interrupted.
//     * Note:  Limit is not enforced with precision.
//     * Set to -1 to disable timeout.
//     * Highly recommended to set this value to at least twice the nanosPerTick, if used; shorter may truncate "normal" ticks.
//     * This timeout is designed to catch infinite loop or deadlock conditions- thread stack trace is captured and logged before Thread.interrupt() is called, and error logging is force-enabled.
//     * If a thread needs to be interrupted a 3rd time on the same tick with matching stack trace, or a 6th time with distinct stack trace, the thread is considered to have "crashed"- which will
//     * be fed into the CrashManager.
//     * All threads are terminated, via allowRunning=false, and the offending thread is terminated via Thread.terminate();
//     */
//    private long tickTimeout = -1;//Disable tick timeout- ticks will not be terminated for taking too long
//    /**
//     * Whether to run the tick pool in "turbo mode", which orders it to run the pool at maximum power- no pool thread should spend any time idling in "turbo mode", unless there are more threads than tick objects.
//     * When in "turbo mode", the tick pool runs at maximum rate, independent of the main tick rate.
//     * The main tick loops- "tick methods"- will run at the normal tick rate.
//     * When in turbo mode, the tick pool will self-suspend (via yield()) as necessary to allow the tick method threads, and all render threads, to run at an acceptable rate.
//     * This is done to prevent the pool from lagging the rest of the application, in the event that the pool has a size that is equal to the number of available logical cores.
//     */
//    private boolean poolTurbo = false;
//    private ArrayList<Thread> tickPool = new ArrayList<>();
//    private ArrayList<TickMethod> tickMethods = new ArrayList<>();
//    /**
//     * Shutdown hooks, to be run (sequential) on the main thread after tick/render shutdown, before release back to the calling function.
//     * Runs on shutdown of the GameHelper, rather than shutdown of the JVM; hooks may not run if the application is terminated externally.
//     * Hooks may be attempted on a fatal error, if the CrashManager allows.
//     */
//    private ArrayList<Runnable> shutdownHooks = new ArrayList<>();
//    /**
//     * In the event of a fatal error, this is run on the application main thread prior to application shutdown.
//     * If absent, a default crash manager is used, which describes the logfile location and asks the user to report the error to the application developer.
//     * Jarfile meta-inf data is used to determine developer, if possible; otherwise, the offending jarfile(s) are named,
//     * and the file in which the class that first created the GameHelper is located will be cited as the application.
//     * If this is <code>null</code>, the crash detection system- including deadlock or 
//     */
//    public CrashManager crashManager;
//    /**
//     * The fatal error that will be fed to the crash manager.
//     */
//    private final FatalError fate = new FatalError();
//    //</editor-fold>
//    //<editor-fold defaultstate="collapsed" desc="Inner Classes">
//    public abstract class TickMethod{
//        public boolean terminate = false;
//        private Thread thread;
//        public abstract void tick();
//        public abstract void finalTick();
//    }
//    public abstract class TickObject{
//        public boolean dead = false;
//        public abstract void tick();
//    }
//    private class FatalError{
//        /**
//         * The stack trace at the creation of the FatalError, which is created when the GameHelper is created.
//         * May be used, when no developer data can be found in the offending jarfiles in a fatal error, to determine the "main" application file for examination.
//         */
//        private StackTraceElement[] launchTrace;
//        {
//            launchTrace = Thread.currentThread().getStackTrace();
//        }
//    }
//    public abstract class CrashManager{
//        /**
//         * <p>Whether the application should shut down because of this crash or not.  Default <code>true</code>.  If set to <code>false</code>, the terminated thread will be restarted, and this variable
//         * will be reset to <code>true</code> after the error completes.
//         * It is highly recommended to leave this at <code>true</code>, unless the cause of the error is KNOWN to be recoverable, and recovery of any damaged data was verified successful.
//         * Try not to rely on this function to detect recoverable errors, as by the time the CrashManager is triggered, the application user will likely have noticed the problem.</p>
//         * <p>Additionally, this is a last-ditch crash detection system, specifically designed to catch a deadlock or infinite loop, and force the application to close properly.</p>
//         * <p>A repeated failure on a single thread or object may also be reported through a <code>FatalError</code>-
//         * for example, if a <code>TickObject</code>, <code>TickMethod</code>, or render thread encounters an identical error many times in a row, such that the GameHelper must catch every instance of it.
//         * This will not trigger a full crash- instead, <code>CrashManager.threadCrash()</code> will be called.  If this function returns <code>true</code>, as its default implementation does, it will fall
//         * through to a full application crash.
//         */
//        public boolean shutdown = true;
//        /**
//         * Called when a <code>TickObject</code>, <code>TickMethod</code> or <code>DisplayWindow</code> (render thread) crashes on the exact same error several times in a row.
//         * @param crashing The object causing the crash.  It will be a <code>TickObject</code>, <code>TickMethod</code>, or <code>DisplayWindow</code> for render-related errors.
//         * @param ex The exception that was thrown multiple times.
//         * @return Whether the error should cascade into a full application crash, via this <code>CrashManager</code>.  Default <code>true</code>.
//         */
//        public boolean threadCrash(Object crashing, Throwable ex){
//            return true;//By default, we don't care what's crashing, or why, it's a dead failure, and an "application crash".  Application logic can be used to override- not recommended.
//        }
//    }
//    private class DisplayWindow{}
//    //</editor-fold>
//    //<editor-fold defaultstate="collapsed" desc="Methods">
//    /**
//     * Attempts to get the <code>TickMethod</code> object governing the current thread.  If no <code>TickMethod</code> was found, <code>null</code> is returned.
//     */
//    public TickMethod getTickMethod(){
//        synchronized(tickMethods){
//            Thread t = Thread.currentThread();
//            for(TickMethod m : tickMethods){
//                if(m.thread==Thread.currentThread()) return m;
//            }
//        }
//        return null;
//    }
//    //addTickMethod(TickMethod)
//    //</editor-fold>
    
    
    
    
    
    
    
    
    
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
    private Method tickMethod;
    private Method renderMethod;
    private Method renderInitMethod;
    private Method tickInitMethod;
    private String windowTitle;
    public boolean running = true;
    private long lastTime;
    private java.awt.Color background;
    private float frameOfView = 45;
    private int lastWidth;
    private int lastHeight;
    private boolean hasColorChanged;
    private Object tickInitObject;
    private Object renderInitObject;
    private Object tickObject;
    private Object renderObject;
    private int mode = MODE_2D;
    private int tickRate = 20;
    public float guiScale = 1;
    private float lastGuiScale;
    private Thread renderThread;
    private long tickTime;
    private Method finalInitMethod;
    private Object finalInitObject;
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
    private Object timer2 = new Object();
    private long tickTarget;
    private int multisampleCount;
    private GLFWErrorCallback errCall;
    private long window;
    //</editor-fold>
    public void setDisplaySize(java.awt.Dimension size){
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
    public void setBackground(java.awt.Color background){
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
    public void setAntiAliasing(int multisampleCount){
        this.multisampleCount = multisampleCount;
    }
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
            createDisplay();
            kickoffRenderThread();
            try {
                wait();
            } catch (InterruptedException ex) {}
        }
        callFunction("FinalInit", finalInitMethod, finalInitObject, new Object[0]);
        long targTime, curTime;
        while(running){
            targTime = tickTime+(1_000_000_000/tickRate);
            while((curTime=targTime-getTime())>0){
                if(curTime>2_000_000) try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {}
            }
            glfwPollEvents();
            callFunction("Tick", tickMethod, tickObject, false);
            tickTime = targTime;
        }
        try {
            renderThread.join();
        } catch (InterruptedException ex) {}
        destroyDisplay();
        callFunction("FinalTick", tickMethod, tickObject, true);
    }
    private void kickoffRenderThread() {
        renderThread = new Thread(){
            public void run(){
                glfwMakeContextCurrent(window);
                glfwSwapInterval(1);
                GL.createCapabilities();
                synchronized(GameHelper.this){
                    callFunction("renderInit", renderInitMethod, renderInitObject, new Object[0]);
                    GameHelper.this.notifyAll();
                }
                while(running){
                    render();
                    if(glfwWindowShouldClose(window)){
                        running = false;
                    }
                }
            }
        };
        renderThread.setName("GameHelper Render Thread");
        renderThread.start();
    }
    private void createDisplay(){
//        Method m;
//        try {
//            m = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[] { String.class });
//            m.setAccessible(true);
//            if(m.invoke(ClassLoader.getSystemClassLoader(), "java.awt.Toolkit")!=null){
//                throw new RuntimeException("AWT cannot be initialized before GLFW!  Possible fix:  add org.lwjgl.glfw.GLFW.glfwInit() to the beginning of your main method.");
//            }
//        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {}
        glfwInit();
        glfwSetErrorCallback(errCall=GLFWErrorCallback.createPrint(System.err));
        glfwWindowHint(GLFW_RESIZABLE, GL11.GL_TRUE);
        window = glfwCreateWindow(width, height, windowTitle, 0, 0);
        glfwShowWindow(window);
        hasColorChanged = true;
    }
    private void destroyDisplay(){
        glfwDestroyWindow(window);
        glfwTerminate();
    }
    public long getTime(){
        return System.nanoTime();
    }
    int[] wHeight=new int[1], wWidth=new int[1];
    public int displayWidth(){ return wWidth[0]; }
    public int displayHeight(){ return wHeight[0]; }
    private void render(){
//        glfwGetWindowSize(window, wWidth, wHeight);
        glfwGetFramebufferSize(window, wWidth, wHeight);
        if(displayWidth()!=lastWidth||displayHeight()!=lastHeight||guiScale!=lastGuiScale||rebuildRenderSetup){
            if(is2D&&(mode==MODE_3D||mode==MODE_HYBRID)) make3D();//So we don't confuse ourselves
            rebuildRenderMode(mode, displayWidth(), displayHeight(), guiScale);
            lastWidth = displayWidth();
            lastHeight = displayHeight();
            lastGuiScale = guiScale;
        }
        if(hasColorChanged){
            refreshBackgroundColor();
        }
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glLoadIdentity();
        int tTime;
        synchronized(timer){
            tTime = (int)((getTime()-tickTime)/1000_000);
        }
        Object value = callFunction("Render", renderMethod, renderObject, tTime);
        if(value!=null&&value instanceof Boolean&&(boolean)value){
            return;
        }
        glfwSwapBuffers(window);
    }
    public void refreshBackgroundColor(){
        if(background==null) background = java.awt.Color.BLACK;
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
//                GL11.glOrtho(-guiScale, guiScale, -guiScale, guiScale, -minRenderDistance, -maxRenderDistance);
                setupPerspective();
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
    private void setupPerspective(){
        float[] projectionMatrix = new float[16];
        float fieldOfView = this.frameOfView;
        float aspectRatio = (float)wWidth[0] / (float)wHeight[0];
        float near_plane = minRenderDistance;
        float far_plane = maxRenderDistance;

        float y_scale = (float) (1/Math.tan(Math.toRadians(fieldOfView / 2f)));
        float x_scale = y_scale / aspectRatio;
        float frustum_length = far_plane - near_plane;

        projectionMatrix[0] = x_scale;
        projectionMatrix[5] = y_scale;
        projectionMatrix[10] = -((far_plane + near_plane) / frustum_length);
        projectionMatrix[11] = -1;
        projectionMatrix[14] = -((2 * near_plane * far_plane) / frustum_length);
        GL11.glLoadMatrixf(projectionMatrix);
    }
    public boolean getMouseButtonState(int button){
        return glfwGetMouseButton(window, button)==GLFW_PRESS;
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
            float width = displayWidth()/(float)displayHeight()*guiScale;
            GL11.glOrtho(-width, width, guiScale, -guiScale, -1, 1);
        }else{
            GL11.glOrtho(0, displayWidth()*guiScale, displayHeight()*guiScale, 0, 0, 100F);
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
            GL11.glLoadIdentity();
            if(renderMode==MODE_2D){
                //For some reason, framebuffers have (0,0) in the lower left corner,
                //  but the main display has it in the upper left, on the exact same init code.  This is the fix.
                GL11.glScalef(1, -1, 1);
                GL11.glTranslatef(0, -height, 0);
            }
        }
    }
    public void assignGUI(GUI gui){
        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int event, int modifiers) {
                gui.onKeyEvent(key, scancode, event, modifiers);
            }
        });
        glfwSetCharCallback(window, new GLFWCharCallback() {
            @Override
            public void invoke(long window, int codepoint) {
                gui.onCharTyped((char)codepoint);
            }
        });
        glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                gui.onMouseMoved(xpos, ypos);
            }
        });
        glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                gui.onMouseButton(button, action, mods);
            }
        });
        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                gui.onMouseScrolled(xoffset, yoffset);
            }
        });
        glfwSetWindowFocusCallback(window, new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, boolean focused) {
                gui.onWindowFocused(focused);
            }
        });
        glfwSetDropCallback(window, new GLFWDropCallback() {
            @Override
            public void invoke(long window, int count, long names) {
                String[] files = new String[count];
                for(int i = 0; i<files.length; i++){
                    files[i] = GLFWDropCallback.getName(names, i);
                }
                gui.onFileDropped(files);
            }
        });
    }
    public boolean isKeyDown(int key){
        return glfwGetKey(window, key)==GLFW_PRESS;
    }
}
