package simplelibrary.opengl.gui.components;
import java.util.logging.Logger;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
/**
 * A text box menu component, designed to display a single line of text
 * Uses texture <code>/gui/textBox.png</code>
 * @author Bryan
 */
public class MenuComponentTextBox extends MenuComponent{
    private static final Logger LOG=Logger.getLogger(MenuComponentTextBox.class.getName());
    /**
     * whether or not the text in the text box can be edited by the user
     */
    public boolean editable;
    /**
     * The text that is displayed
     */
    public String text;
    /**
     * The text inset of the text box, in GL coordinates.  This is the minimum distance between text and the outside of the text box.
     */
    public double textInset = -1;
    /**
     * @param x The X location of the text box, in GL coordinates
     * @param y The Y location of the text box, in GL coordinates
     * @param width The width of the text box, in GL coordinates
     * @param height The height of the text box, in GL coordinates
     * @param text The initial text to go on the text box
     * @param editable The initial editable state; defines whether the user may modify the text.
     */
    public MenuComponentTextBox(double x, double y, double width, double height, String text, boolean editable){
        super(x, y, width, height);
        this.text = text;
        this.editable = editable;
    }
    @Override
    public void onCharTyped(char c) {
        if(!Character.isWhitespace(c)&&editable)text+=c;
    }
    @Override
    public void keyEvent(int key, int scancode, boolean isPress, boolean isRepeat, int modifiers) {
        if(!(isPress||isRepeat)||!editable) return;//Don't care about release, or while editing is off
        switch(key){
            case GLFW_KEY_BACKSPACE:
                if(!text.isEmpty()){
                    text = text.substring(0, text.length()-1);
                } break;
            case GLFW_KEY_TAB:
                parent.onTabPressed(this); break;
            case GLFW_KEY_ENTER:
                parent.onReturnPressed(this); break;
        }
    }
    @Override
    public void render(){
        drawRect(x, y, x+width, y+height, ImageStash.instance.getTexture("/gui/textBox.png"));
        GL11.glColor3f(0, 0, 0);
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
        if(editable){
            drawText(x+textInset, y+textInset, x+width-textInset, y+height-textInset, text+(((gui.tick&20)<10&&isSelected)?"_":""));
        }else{
            drawCenteredText(x+textInset, y+textInset, x+width-textInset, y+height-textInset, text);
        }
        GL11.glColor3f(1, 1, 1);
    }
}
