package simplelibrary.window;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowListener;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
/**
 * A class designed to allow for rapid and easy creation of frames.
 * @author Bryan
 */
public class WindowHelper{
    private static final Logger LOG=Logger.getLogger(WindowHelper.class.getName());
    private static int[] lastValues = null;

    /**
     * Finds the size that the outside of a window should be for the inside of the window to be of the specified size.
     * @param size The desired internal dimensions of the window
     * @return The necessary external dimensions of the window
     */
    public static Dimension adjustSizeForContents(Dimension size){
        int x = size.width;
        int y = size.height;
        int[] expansion = getExpansion();
        x+=expansion[0];
        y+=expansion[1];
        return new Dimension(x, y);
    }
    /**
     * Centers a frame on the screen
     * @param Center The frame to center on the screen
     * @param width The desired width of the frame
     * @param height The desired height of the frame
     */
    public static void centerFrame(Window Center, int width, int height){
        if(Center==null){
            throw new IllegalArgumentException("Parameter 'Center' cannot be 'null'!");
        }else if(width==0){
            throw new IllegalArgumentException("Parameter 'width' cannot be '0'!");
        }else if(height==0){
            throw new IllegalArgumentException("Parameter 'height' cannot be '0'!");
        }
        Toolkit kit = Center.getToolkit();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration());
        Dimension d = kit.getScreenSize();
        int maxWidth = d.width-in.left-in.right;
        int maxHeight = d.height-in.top-in.bottom;
        Center.setSize(Math.min(maxWidth, width), Math.min(maxHeight, height));
        Center.setLocation((maxWidth-Center.getWidth())/2, (maxHeight-Center.getHeight())/2);
    }
    /**
     * Centers a frame on the screen
     * @param Center the frame to center on the screen
     * @param size The desired size of the frame
     */
    public static void centerFrame(Window Center, Dimension size){
        centerFrame(Center, size.width, size.height);
    }
    /**
     * Creates a JFrame, centers it on the screen, and sets it to be visible.
     * A known bug causes some components added to the frame immediately after return from this method to be somewhat iffy in appearance.  I recommend using <code>createFrameWithoutAppearance(String, int, int, WindowListener)</code> instead and making it visible after adding your components.
     * @param title The title for the JFrame, as set by the constructor <code>new JFrame(String)</code>
     * @param width The internal width of the frame
     * @param height The internal height of the frame
     * @param wl A window listener to add to the frame.  No effect if null.
     * @return The frame that was created
     */
    public static JFrame createFrame(String title, int width, int height, WindowListener wl){
        if(title==null||title.isEmpty()){
            title = "A program for "+System.getProperty("os.name");
        }
        JFrame value = new JFrame(title);
        int[] expansion = getExpansion();
        centerFrame(value, width+expansion[0], height+expansion[1]);
        value.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        if(wl!=null){
            value.addWindowListener(wl);
        }
        value.setVisible(true);
        return value;
    }
    /**
     * Creates a JFrame, centers it on the screen, and sets it to be visible.
     * A known bug causes some components added to the frame immediately after return from this method to be somewhat iffy in appearance.  I recommend using <code>createFrameWithoutAppearance(String, Dimension, WindowListener)</code> instead and making it visible after adding your components.
     * @param title The title for the JFrame, as set by the constructor <code>new JFrame(String)</code>
     * @param size The internal dimensions of the frame
     * @param wl A window listener to add to the frame.  No effect if null.
     * @return The frame that was created
     */
    public static JFrame createFrame(String title, Dimension size, WindowListener wl){
        return createFrame(title, size.width, size.height, wl);
    }
    /**
     * Creates a JFrame and centers it on the screen, though it is left invisible.
     * @param title The title for the JFrame, as set by the constructor <code>new JFrame(String)</code>
     * @param width The internal width of the frame
     * @param height The internal height of the frame
     * @param wl A window listener to add to the frame.  No effect if null.
     * @return The frame that was created
     */
    public static JFrame createFrameWithoutAppearance(String title, int width, int height, WindowListener wl){
        if(title==null||title.isEmpty()){
            title = "A program for "+System.getProperty("os.name");
        }
        JFrame value = new JFrame(title);
        int[] expansion = getExpansion();
        centerFrame(value, width+expansion[0], height+expansion[1]);
        value.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        if(wl!=null){
            value.addWindowListener(wl);
        }
        return value;
    }
    /**
     * Creates a JFrame and centers it on the screen, though it is left invisible.
     * @param title The title for the JFrame, as set by the constructor <code>new JFrame(String)</code>
     * @param size The internal dimensions of the frame
     * @param wl A window listener to add to the frame.  No effect if null.
     * @return The frame that was created
     */
    public static JFrame createFrameWithoutAppearance(String title, Dimension size, WindowListener wl){
        return createFrameWithoutAppearance(title, size.width, size.height, wl);
    }
    /**
     * Finds the expansion values returned by <code>getExpansion()</code>.  This is automatically called by <code>getExpansion()</code> if it has not yet been called.
     * Calling this method may cause a minimum-sized JFrame to appear on the upper left corner of the screen (position 0, 0).
     */
    public static void findExpansion(){
        JFrame frame = new JFrame("Test frame");
        JPanel panel = new JPanel();
        frame.add(panel);
        frame.setVisible(true);
        Rectangle frameBounds = frame.getBounds();
        Rectangle panelBounds = panel.getBounds();
        lastValues = new int[]{frameBounds.width-panelBounds.width, frameBounds.height-panelBounds.height};
        frame.dispose();
    }
    /**
     * @return The increase in size to get the external size of a window to the internal size of the window; (x, y).
     */
    public static int[] getExpansion(){
        if(lastValues==null){
            findExpansion();
        }
        return lastValues.clone();
    }

    public static void positionFrame(Container frame, int x, int y, int width, int height){
        Toolkit kit = frame.getToolkit();
//        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice[] gs = ge.getScreenDevices();
//        Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration());
        Dimension d = kit.getScreenSize();
        x = Math.min(d.width-width, Math.max(0, x));
        y = Math.min(d.height-height, Math.max(0, y));
        frame.setBounds(x, y, width, height);
    }
    private WindowHelper(){
    }
}
