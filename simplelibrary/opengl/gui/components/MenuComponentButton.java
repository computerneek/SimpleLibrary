package simplelibrary.opengl.gui.components;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
public class MenuComponentButton extends MenuComponent implements ActionListener{
    private static final Logger LOG = Logger.getLogger(MenuComponentButton.class.getName());
    public String label;
    public boolean enabled;
    public boolean isPressed;
    public double textInset = -1;
    public boolean useMouseover = true;
    public boolean actOnPress = DefaultActOnPress;
    public static boolean DefaultActOnPress = false;
    protected String textureRoot = "/gui/button";
    private final ArrayList<ActionListener> listeners = new ArrayList<>();
    public MenuComponentButton(double x, double y, double width, double height, String label, boolean enabled){
        super(x, y, width, height);
        this.label = label;
        this.enabled = enabled;
    }
    public MenuComponentButton(double x, double y, double width, double height, String label, boolean enabled, boolean useMouseover){
        this(x, y, width, height, label, enabled);
        this.useMouseover = useMouseover;
    }
    public MenuComponentButton(double x, double y, double width, double height, String label, boolean enabled, boolean useMouseover, String textureRoot){
        this(x, y, width, height, label, enabled, useMouseover);
        this.textureRoot = textureRoot;
    }
    @Override
    public void onMouseButton(double x, double y, int button, boolean pressed, int mods) {
        if(pressed&&enabled&&button==0){
            isPressed = true;
            if(actOnPress) action();
        }else if(button==0&&!pressed){
            if(!actOnPress&&isPressed&&!Double.isNaN(x)) action();
            isPressed = false;
        }
        super.onMouseButton(x, y, button, pressed, mods); //To change body of generated methods, choose Tools | Templates.
    }
    @Override
    public void render(){
        int texture = -1;
        if(enabled){
            if(isPressed){
                texture = ImageStash.instance.getTexture(textureRoot+"Pressed.png");
            }else{
                if(isMouseOver){
                    texture = ImageStash.instance.getTexture(textureRoot+(useMouseover?"Mouseover":"")+".png");
                }else{
                    texture = ImageStash.instance.getTexture(textureRoot+".png");
                }
            }
        }else{
            if(isMouseOver){
                texture = ImageStash.instance.getTexture(textureRoot+"Disabled.png");
            }else{
                texture = ImageStash.instance.getTexture(textureRoot+"Disabled.png");
            }
        }
        if(textInset<0){
            switch(parent.gui.type){
                case GameHelper.MODE_2D:
                case GameHelper.MODE_HYBRID:
                case GameHelper.MODE_2D_CENTERED:
                    textInset = 5;
                    break;
                case GameHelper.MODE_3D:
                    textInset = Math.min(width/20, height/20);
                    break;
            }
        }
        drawRect(x, y, x+width, y+height, texture);
        GL11.glColor3f(foregroundColor.getRed()/255F, foregroundColor.getGreen()/255F, foregroundColor.getBlue()/255F);
        drawCenteredText(x+textInset, y+textInset, x+width-textInset, y+height-textInset, label);
        GL11.glColor3f(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F);
    }
    /**
     * Called when the button is clicked.  The default implementation points to Menu.buttonClicked(MenuComponentButton), unless an ActionListener is present.
     */
    public void action(){
        if(listeners.isEmpty()) parent.buttonClicked(this);
        else actionPerformed(new ActionEvent(this, parent.components.indexOf(this), label));
    }
    public void addActionListener(ActionListener a){
        listeners.add(a);
    }
    public void removeActionListener(ActionListener a){
        listeners.remove(a);
    }
    public void actionPerformed(ActionEvent e){
        for(ActionListener l : listeners){
            gui.addPendingAction(l, e);
        }
    }
    @Override
    protected Color defaultForegroundColor(){
        return Color.BLACK;
    }
}
