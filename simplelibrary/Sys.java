package simplelibrary;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.logging.Logger;
import simplelibrary.config.Config;
import simplelibrary.error.ErrorAdapter;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorHandler;
import simplelibrary.error.ErrorLevel;
import simplelibrary.game.GameHelper;
/**
 * Core SimpleLibrary class; contains general SimpleLibrary utilities.
 * @author Bryan Dolan
 */
public class Sys{
    /**
     * The error log file.  Normally null; this file is used when logging errors to file.
     * If it is found to be null at the time of an error, it is set.  Never nulled out by the library.
     */
    public static File errorLog;
    /**
     * The list of values that the generateRandomString() method has returned.  Used by it to avoid duplicate return values.
     */
    public static ArrayList<String> generatedStrings = new ArrayList<>();
    private static ArrayList<String> generatedPasskeys = new ArrayList<>();
    private static final Logger LOG=Logger.getLogger(Sys.class.getName());
    static ErrorHandler handler;
    private static boolean initialized;
    static boolean log = true;
    private static final Random rand=new Random(1);
    private static File root;
    private static UncaughtExceptionHandler uncaughtExceptionHandler;
    private static boolean useLWJGL;
    public static ErrorList suppressedErrors = new ErrorList();
    private static ErrorCategory[] suppress = new ErrorCategory[0];
    private static boolean suppressUncaughtExceptionHandler;
    /**
     * @return the LWJGL flag; as in, if SimpleLibrary will use LWJGL where it can but doesn't have to
     */
    public static boolean canUseLWJGL(){
        return useLWJGL;
    }
    /**
     * Produces an error.  Probably not all that usefull.  A complicated system of null checks and value fills means that any of the parameters can be null, if enough of the others are non-null.
     * @param level The level of the error
     * @param message A message to print in front of the error
     * @param error The exception.  If this is found to be null, it is set to <code>new UnknownError()</code> to allow for maximum error traceability.
     * @param category The category of the error.
     */
    public static void error(ErrorLevel level, String message, Throwable error, ErrorCategory category){
        error(level, message, error, category, log);
    }
    public static void error(ErrorLevel level, String message, Throwable error, ErrorCategory category, boolean log){
        if(!initialized) throw new RuntimeException(error);
        if(level==null&&error!=null){
            level = ErrorLevel.minor;
        }else if(level==null){
            error(ErrorLevel.severe, null, new IllegalArgumentException("Error must have a level or an exception!"), ErrorCategory.bug);
            return;
        }
        if(message==null&&error==null){
            error(ErrorLevel.severe, null, new IllegalArgumentException("A message or exception must be tied to an error!"), ErrorCategory.bug);
            return;
        }
        if(error == null&&(level==ErrorLevel.critical||level==ErrorLevel.severe)){
            error = new UnknownError();
        }
        if(message==null){
            message = "";
        }
        if(level==null||category==null){
            String amessage = "";
            if(level==null&&category==null){
                amessage = "Error must be assigned a level and a catagory!";
            }else if(level==null){
                amessage = "Error must be assigned a level!";
            }else if(category==null){
                amessage = "Error must be assigned a catagory!";
            }
            error(ErrorLevel.severe, null, new IllegalArgumentException(amessage), ErrorCategory.bug);
            return;
        }
        for(ErrorCategory c : suppress){
            if(category==c){
                suppress(level, message, error, category, log);
                return;
            }
        }
        if(log){
            log(message, error);
        }
        //</editor-fold>
        level.error(handler, message, error, category);
        if(suppressedErrors.getCount()>0){
            suppressedErrors.throwAll();
        }
    }
    public static void suppress(ErrorLevel level, String message, Throwable error, ErrorCategory category){
        suppressedErrors.add(level, message, error, category);
    }
    public static void suppress(ErrorLevel level, String message, Throwable error, ErrorCategory category, boolean log){
        suppressedErrors.add(level, message, error, category, log);
    }
    public static void suppressAll(ErrorCategory... categories){
        if(categories!=null) suppress = categories;
    }
    public static boolean hasSuppressed(){
        return suppressedErrors.getCount()>0;
    }
    /**
     * Generates a pseudorandom and probably unprintable string.  The return value is automatically added to the <code>generatedStrings</code> variable
     * before it is returned.  It is, however, guaranteed that it will be unique.
     *
     * If the first 500,000 strings it comes up with are already in the list, this method returns null, in which case it may not be unique.
     * @param characters the length of the string, in characters
     * @return The random string
     */
    public static String generateRandomString(int characters){
        String value;
        int taken = 0;
        while (generatedStrings.contains(value=genRandString(characters)) && taken < 500_000) {
            taken++;
        }
        if(generatedStrings.contains(value)){
            return null;
        }
        generatedStrings.add(value);
        return value;
    }
    /**
     * Generates a pseudorandom string.  This function will never return the same value twice, save <code>null</code>, if it
     * can't find one it hasn't generated yet, after 500,000 tries.
     * @param characters The number of characters desired
     * @param allowedChars A String containing all characters that are to be used in the string
     * @param rand A random to be used; internal rand is used if null
     * @return A randomized String, likely suitable for a passkey (Apply password strength checking rules before accepting, possibly regenerating)
     * @throws NullPointerException if <code>allowedChars</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>allowedChars</code> is blank
     */
    public static String generateRandomPasskey(int characters, String allowedChars, Random rand){
        String value;
        int taken = 0;
        while(generatedPasskeys.contains(value=genRandPasskey(characters, allowedChars, rand))&&taken<500_000){
            taken++;
        }
        if(generatedPasskeys.contains(value)) return null;
        generatedPasskeys.add(value);
        return value;
    }
    /**
     * @return The ErrorHandler being used by SimpleLibrary.  This will be equal to the value entered into the <code>init()</code> method or, if that was null, a default one.
     */
    public static ErrorHandler getErrorHandler(){
        return checkHandler(handler);
    }
    /**
     * @return The root directory, as fed in to the <code>init(File, ErrorHandler)</code> method
     */
    public static File getRoot(){
        return root;
    }
    /**
     * @return An uncaught exception handler that routes uncaught exceptions through the <code>error()</code> method.
     */
    public static UncaughtExceptionHandler getUncaughtExceptionHandler(){
        if(uncaughtExceptionHandler!=null){
            return uncaughtExceptionHandler;
        }else{
            return createUncaughtExceptionHandler();
        }
    }
    /**
     * <p>calls <code>init(File, ErrorHandler)</code> with the supplied params (<code>root</code> and <code>handler</code>)</p>
     * <p>Sets the default uncaught exception handler to use this error system</p>
     * <p>Reads the settings from any file found at the <code>settings</code> parameter</p>
     * <p>Checks for registered exception levels in the defaults by calling <code>ErrorLevel.checkRegistry(Config)</code></p>
     * @param root The root folder in which error logs should be placed; cannot be null
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     * @param settings The string from which the settings are loaded from; a value of <code>null</code> tells the system that there are no settings in a config v1
     * @return the settings that were loaded
     */
    public static Config init(File root, ErrorHandler handler, String settings){
        init(root, handler);
        if(!suppressUncaughtExceptionHandler) Thread.setDefaultUncaughtExceptionHandler(Sys.getUncaughtExceptionHandler());
        Config config = settings==null?null:Config.loadConfig(settings);
        return config;
    }
    /**
     * Initializes SimpleLibrary; this doesn't do much at the moment, beyond storing the parameters for future use and setting the uncaught exception handler of the current thread.
     * @param root The root folder in which error logs should be placed; cannot be null
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     */
    public static void init(File root, ErrorHandler handler){
        if(root==null){
            throw new IllegalArgumentException("Root folder cannot be null!");
        }
        Sys.root = root;
        Sys.handler = checkHandler(handler);
        if(!suppressUncaughtExceptionHandler) Thread.setDefaultUncaughtExceptionHandler(getUncaughtExceptionHandler());
        initialized = true;
    }
    /**
     * <p>calls <code>init(File, ErrorHandler)</code> with the supplied params (<code>root</code> and <code>handler</code>)</p>
     * <p>Sets the default uncaught exception handler to use this error system</p>
     * <p>Reads the settings from any file found at the <code>settings</code> parameter</p>
     * <p>Sets the LWJGL flag to <code>true</code>, meaning that, when possible, this library will use LWJGL</p>
     * @param root The root folder in which error logs should be placed; cannot be null
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     * @param settings The string from which the settings are loaded from; a value of <code>null</code> tells the system that there are no settings in a config v1
     * @return the settings that were loaded
     */
    public static Config initLWJGL(File root, ErrorHandler handler, String settings){
        Config config = init(root, handler, settings);
        useLWJGL = true;
        System.out.println("Using LWJGL v"+org.lwjgl.Version.getVersion());
        return config;
    }
    /**
     * <p>calls <code>init(File, ErrorHandler)</code> with the supplied params (<code>root</code> and <code>handler</code>)</p>
     * <p>Sets the LWJGL flag to <code>true</code>, meaning that, when possible, this library will use LWJGL</p>
     * @param root The root folder in which error logs should be placed; cannot be null
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     */
    public static void initLWJGL(File root, ErrorHandler handler){
        init(root, handler);
        useLWJGL = true;
    }
    /**
     * <p>calls <code>init(File, ErrorHandler)</code> with the supplied params (<code>root</code> and <code>handler</code>)</p>
     * <p>Sets the default uncaught exception handler to use this error system</p>
     * <p>Reads the settings from any file found at the <code>settings</code> parameter</p>
     * <p>Sets the LWJGL flag to <code>true</code>, meaning that, when possible, this library will use LWJGL</p>
     * <p>Initializes the game interface with the settings found in the <code>helper</code> parameter</p>
     * @param root The root folder in which error logs should be placed; cannot be null
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     * @param settings The string from which the settings are loaded from; a value of <code>null</code> tells the system that there are no settings in a config v1
     * @param helper The game helper in which all game interface info is handled, including keyboard and mouse info; cannot be null
     * @return the settings that were loaded
     */
    public static Config initLWJGLGame(File root, ErrorHandler handler, String settings, GameHelper helper){
        if(helper==null){
            throw new IllegalArgumentException("Game helper cannot be null!");
        }
        Config config = initLWJGL(root, handler, settings);
        helper.start();
        return config;
    }
    /**
     * <p>calls <code>initLWJGLGame(File, ErrorHandler, String)</code> with the supplied params (<code>root</code>  and <code>handler</code></p>
     * <p>Sets the logging flag to <code>false</code>, meaning that error log files will not be saved automatically.</p>
     * @param root The root folder in which error logs should be placed; cannot be null.  Errors can be logged with the <code>log(String, Throwable)</code> method.
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     * @param settings The string from which the settings are loaded from; a value of <code>null</code> tells the system that there are no settings in a config v1
     * @param helper The game helper in which all game interface info is handled, including keyboard and mouse info; cannot be null
     * @return the settings that were loaded
     */
    public static Config initLWJGLGameUnlogged(File root, ErrorHandler handler, String settings, GameHelper helper){
        log = false;
        return initLWJGLGame(root, handler, settings, helper);
    }
    /**
     * <p>calls <code>initLWJGL(File, ErrorHandler, String)</code> with the supplied params (<code>root</code>  and <code>handler</code></p>
     * <p>Sets the logging flag to <code>false</code>, meaning that error log files will not be saved automatically.</p>
     * @param root The root folder in which error logs should be placed; cannot be null.  Errors can be logged with the <code>log(String, Throwable)</code> method.
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     * @param settings The string from which the settings are loaded from; a value of <code>null</code> tells the system that there are no settings in a config v1
     * @return the settings that were loaded
     */
    public static Config initLWJGLUnlogged(File root, ErrorHandler handler, String settings){
        log = false;
        return initLWJGL(root, handler, settings);
    }
    /**
     * <p>calls <code>initLWJGL(File, ErrorHandler)</code> with the supplied params (<code>root</code>  and <code>handler</code></p>
     * <p>Sets the logging flag to <code>false</code>, meaning that error log files will not be saved automatically.</p>
     * @param root The root folder in which error logs should be placed; cannot be null.  Errors can be logged with the <code>log(String, Throwable)</code> method.
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     */
    public static void initLWJGLUnlogged(File root, ErrorHandler handler){
        initLWJGL(root, handler);
        log = false;
    }
    /**
     * <p>calls <code>init(File, ErrorHandler, String)</code> with the supplied params (<code>root</code>  and <code>handler</code></p>
     * <p>Sets the logging flag to <code>false</code>, meaning that error log files will not be saved automatically.</p>
     * @param root The root folder in which error logs should be placed; cannot be null.  Errors can be logged with the <code>log(String, Throwable)</code> method.
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     * @param settings The string from which the settings are loaded from; a value of <code>null</code> tells the system that there are no settings in a config v1
     * @return the settings that were loaded
     */
    public static Config initUnlogged(File root, ErrorHandler handler, String settings){
        log = false;
        return init(root, handler, settings);
    }
    /**
     * <p>calls <code>init(File, ErrorHandler)</code> with the supplied params (<code>root</code>  and <code>handler</code></p>
     * <p>Sets the logging flag to <code>false</code>, meaning that error log files will not be saved automatically.</p>
     * @param root The root folder in which error logs should be placed; cannot be null.  Errors can be logged with the <code>log(String, Throwable)</code> method.
     * @param handler The error handler to be used when an error is caught; a value of <code>null</code> selects the default
     */
    public static void initUnlogged(File root, ErrorHandler handler){
        init(root, handler);
        log = false;
    }
    /**
     * @return If any of the initialization methods have been called
     */
    public static boolean isInitialized(){
        return initialized;
    }
    /**
     * Logs an error to the error log.  If none exists, the error log will be generated.  Threadsafe.  This method does not log anything to the system output, only the log file.  Use the <code>error(ErrorLevel, String, Throwable, ErrorCategory)</code> method for error handling; unless initialized as unlogged, this method is automatically called by it.
     * @param message A message to precede the error in the log.  Cannot be null, can be blank
     * @param error The error to log.  Cannot be null.
     */
    public static synchronized void log(String message, Throwable error){
        if(!initialized) return;
        try (PrintWriter out=getErrorLog()) {
            out.println(message);
            if(error!=null){
                out.println("error="+error.getClass().getName());
                out.println("errorMessage="+error.getMessage());
                StackTraceElement[] stack = error.getStackTrace();
                for(StackTraceElement stack1:stack){
                    out.println(" at "+stack1.toString());
                }
                int sSize = stack.length;
                while((error = error.getCause())!=null){
                    out.println("causedBy="+error.getClass().getName());
                    out.println("errorMessage="+error.getMessage());
                    stack = error.getStackTrace();
                    for(int i = 0; i<stack.length-sSize; i++){
                        StackTraceElement stack1 = stack[i];
                        out.println("  at "+stack1.toString());
                    }
                    out.println("    ..."+sSize+" more");
                    sSize = stack.length;
                }
            }else{
                out.println("!!No Exception!!");
            }
            out.println("--END EXCEPTION--");
        }
    }
    /**
     * Restarts the program.  This method will return normally if the program was properly restarted or throw an exception if it could not be restarted.
     * @param vmArgs The VM arguments for the new instance
     * @param applicationArgs The application arguments for the new instance
     * @param additionalFiles Any additional files to include in the classpath
     * @param mainClass The program's main class.  The new instance is started with this as the specified main class.
     * @return The handle to the spawned process
     * @throws URISyntaxException if a URI cannot be created to obtain the filepath to the jarfile
     * @throws IOException if Java is not in the PATH environment variable
     */
    public static Process restart(String[] vmArgs, String[] applicationArgs, String[] additionalFiles, Class<?> mainClass) throws URISyntaxException, IOException{
        return restart(vmArgs, applicationArgs, additionalFiles, mainClass, null);
    }
    /**
     * Restarts the program.  This method will return normally if the program was properly restarted or throw an exception if it could not be restarted.
     * @param vmArgs The VM arguments for the new instance
     * @param applicationArgs The application arguments for the new instance
     * @param additionalFiles Any additional files to include in the classpath
     * @param mainClass The program's main class.  The new instance is started with this as the specified main class.
     * @param workingDir The program's working directory
     * @return The handle to the spawned process
     * @throws URISyntaxException if a URI cannot be created to obtain the filepath to the jarfile
     * @throws IOException if Java is not in the PATH environment variable
     */
    public static Process restart(String[] vmArgs, String[] applicationArgs, String[] additionalFiles, Class<?> mainClass, File workingDir) throws URISyntaxException, IOException{
        ArrayList<String> params = new ArrayList<>();
        params.add("java");
        params.addAll(Arrays.asList(vmArgs));
        params.add("-classpath");
        String filepath = mainClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        String separator = System.getProperty("path.separator");
        if(additionalFiles!=null){
            for(String str : additionalFiles){
                filepath+=separator+str;
            }
        }
        params.add(filepath);
        params.add(mainClass.getName());
        params.addAll(Arrays.asList(applicationArgs));
        ProcessBuilder builder = new ProcessBuilder(params);
        return builder.start();
    }
    /**
     * A functional duplicate of String.split() that will allow characters such as '\\'.  However, it cannot accept strings- only single characters.
     * @param string The string to split
     * @param c The character to break it up on
     * @return The broken strings
     */
    public static String[] splitString(String string, char c){
        char[] chars = new char[string.length()];
        string.getChars(0, string.length(), chars, 0);
        ArrayList<String> lst = new ArrayList<>();
        String current = "";
        for(int i = 0; i<chars.length; i++){
            if(chars[i]==c){
                lst.add(current);
                current = "";
            }else{
                current+=chars[i];
            }
        }
        lst.add(current);
        return lst.toArray(new String[lst.size()]);
    }
    /**
     * Starts the requested Java application.  This method will return normally if the program was properly started or throw an exception if it could not be started.
     * @param vmArgs The VM arguments for the new instance
     * @param applicationArgs The application arguments for the new instance
     * @param file The program file.
     * @return The handle to the spawned process
     * @throws IOException if Java is not in the PATH environment variable
     */
    public static Process startJava(String[] vmArgs, String[] applicationArgs, File file) throws IOException{
        ArrayList<String> params = new ArrayList<>();
        params.add("java");
        params.addAll(Arrays.asList(vmArgs));
        params.add("-jar");
        params.add(file.getAbsolutePath());
        params.addAll(Arrays.asList(applicationArgs));
        ProcessBuilder builder = new ProcessBuilder(params);
        return builder.start();
    }
    /**
     * Starts the requested Java application via the javaw command.  This method will return normally if the program was properly started or throw an exception if it could not be started.
     * @param vmArgs The VM arguments for the new instance
     * @param applicationArgs The application arguments for the new instance
     * @param file The program file.
     * @return The handle to the spawned process
     * @throws IOException if Java is not in the PATH environment variable
     */
    public static Process startJavaw(String[] vmArgs, String[] applicationArgs, File file) throws IOException{
        ArrayList<String> params = new ArrayList<>();
        params.add("javaw");
        params.addAll(Arrays.asList(vmArgs));
        params.add("-jar");
        params.add(file.getAbsolutePath());
        params.addAll(Arrays.asList(applicationArgs));
        ProcessBuilder builder = new ProcessBuilder(params);
        return builder.start();
    }
    /**
     * Creates a small file with its name based on the program name.  This is designed for single-instance programs to use.  Uses a shutdown hook to unlock it on shutdown.
     * @param programName A name to be used for the program.  May cause problems if it contains invalid filename characters.
     * @return If this is the only program using a single instance via this method.
     */
    public static boolean startSingleInstance(String programName){
        final File singleInstanceFile = new File(System.getenv("APPDATA")+"\\Dolan Programmers\\Single Instance Locks\\"+programName+".lock");
        if(singleInstanceFile.exists()&&!singleInstanceFile.delete()){
            return false;
        }
        final FileInputStream[] out = new FileInputStream[1];
        Thread unlocker = new Thread(){
            @Override
            public void run(){
                try{
                    out[0].close();
                }catch(IOException ex){
                    throw new RuntimeException(ex);
                }
                singleInstanceFile.delete();
            }
        };
        singleInstanceFile.getParentFile().mkdirs();
        try{
            singleInstanceFile.createNewFile();
            out[0] = new FileInputStream(singleInstanceFile);
        }catch(IOException ex){
            return false;
        }
        Runtime.getRuntime().addShutdownHook(unlocker);
        return true;
    }
    private static ErrorHandler checkHandler(ErrorHandler handler){
        if(handler!=null){
            return handler;
        }else{
            return new ErrorAdapter() {};
        }
    }
    private static UncaughtExceptionHandler createUncaughtExceptionHandler(){
        uncaughtExceptionHandler = new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable error){
                threadError(thread, error);
            }
        };
        return uncaughtExceptionHandler;
    }
    private static String genRandString(int characters){
        char[] chars = new char[characters];
        for (int i = 0; i<chars.length; i++) {
            chars[i] = (char) rand.nextInt(0x1_0000);
        }
        return String.valueOf(chars);
    }
    private static String genRandPasskey(int characters, String allowedChars, Random rand){
        if(rand==null) rand = Sys.rand;
        int strLength = allowedChars.length();
        char[] chars = new char[characters];
        for(int i = 0; i<chars.length; i++){
            chars[i] = allowedChars.charAt(rand.nextInt(strLength));
        }
        return String.valueOf(chars);
    }
    private static synchronized PrintWriter getErrorLog(){
        GregorianCalendar cal = new GregorianCalendar();
        if(errorLog==null){
            root.mkdirs();
            errorLog = new File(root, "Error "+cal.getTime().toString().replace(':', '-')+".log");
            int which = 1;
            while(errorLog.exists()){
                errorLog = new File(root, "Error "+cal.getTime().toString().replace(':', '-')+" "+which+".log");
                which++;
            }
        }
        try{
            PrintWriter out = new PrintWriter(new FileOutputStream(errorLog, true));
            out.println("--EXCEPTION OCCURED--");
            out.println(cal.getTime().toString());
            return out;
        }catch(FileNotFoundException ex){
            throw new RuntimeException(ex);
        }
    }
    private static void threadError(Thread thread, Throwable error){
        if(error!=null){
            error(null, "Error in thread "+thread.getName()+":  "+error.getMessage(), error, ErrorCategory.uncaught);
        }else{
            error(null, "Error in thread "+thread.getName()+"!", error, ErrorCategory.uncaught);
        }
    }
    private Sys(){
    }
    /**
     * Call before calling any init() function to block SimpleLibrary from overriding the default uncaught exception handler.  Error handling system will still initialize.
     * To restore this function later, call Thread.setDefaultUncaughtExceptionHandler(Sys.getUncaughtExceptionHandler());
     */
    public static void suppressUncaughtExceptionHandler(){
        suppressUncaughtExceptionHandler = true;
    }
}
