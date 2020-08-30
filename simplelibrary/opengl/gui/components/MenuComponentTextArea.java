package simplelibrary.opengl.gui.components;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
public class MenuComponentTextArea extends MenuComponent{
    private static final Logger LOG = Logger.getLogger(MenuComponentTextArea.class.getName());
    public ArrayList<String> text;
    protected double textHeight;
    public boolean editable;
    public double textInset = -1;
    public MenuComponentTextArea(double x, double y, double width, double height, double textHeight, String[] display, boolean editable){
        super(x, y, width, height);
        text = new ArrayList<>(Arrays.asList(display));
        this.textHeight = textHeight;
    }
    @Override
    public void render(){
        int line = 0;
        if(textInset<0){
            switch(parent.gui.type){
                case GameHelper.MODE_2D:
                case GameHelper.MODE_HYBRID:
                case GameHelper.MODE_2D_CENTERED:
                    textInset = 5;
                    break;
                case GameHelper.MODE_3D:
                    textInset = textHeight/18;
            }
        }
        for (String str : text) {
            do{
                GL11.glColor4f(0, 0, 0, 1);
                str = drawTextWithWrapAndBounds(x+textInset, y+textInset+line*textHeight, x+width-textInset, y+textInset+line*textHeight+textHeight, x+textInset, y+textInset, x+width-textInset, y+height-textInset, str);
                GL11.glColor4f(1, 1, 1, 1);
                line++;
            }while(str!=null&&!str.isEmpty());
        }
        double bufferHeight = line*textHeight;
    }
    @Override
    public void keyEvent(int key, int scancode, boolean isPress, boolean isRepeat, int modifiers) {
        if(!(isPress||isRepeat)||!editable) return;
        switch(key){
            case GLFW_KEY_TAB:
                parent.onTabPressed(this); break;
            case GLFW_KEY_ENTER:
                text.add("");
            case GLFW_KEY_BACKSPACE:
                String txt = text.get(text.size()-1);
                if(!txt.isEmpty()) text.set(text.size()-1, txt.substring(0, txt.length()-1));
                else if(text.size()>1) text.remove(text.size()-1);
        }
    }
    @Override
    public void onCharTyped(char c) {
        if(!Character.isWhitespace(c)&&editable) text.set(text.size()-1, text.get(text.size()-1)+c);
    }
}
