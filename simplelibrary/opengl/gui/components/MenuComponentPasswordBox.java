package simplelibrary.opengl.gui.components;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
public class MenuComponentPasswordBox extends MenuComponentTextBox{
    private static final Logger LOG = Logger.getLogger(MenuComponentPasswordBox.class.getName());
    public MenuComponentPasswordBox(double x, double y, double width, double height, String text, boolean editable){
        super(x, y, width, height, text, editable);
    }
    @Override
    public void render(){
        drawRect(x, y, x+width, y+height, ImageStash.instance.getTexture("/gui/textBox.png"));
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
        GL11.glColor3f(0, 0, 0);
        if(editable){
            drawText(x+textInset, y+textInset, x+width-textInset, y+height-textInset, genString(text)+(((gui.tick&20)<10&&isSelected)?"_":""));
        }else{
            drawCenteredText(x+textInset, y+textInset, x+width-textInset, y+height-textInset, genString(text));
        }
        GL11.glColor3f(1, 1, 1);
    }
    private String genString(String text){
        int length = text.length();
        String replacement = "";
        for(int i = 0; i<length; i++){
            replacement+="-";
        }
        return replacement;
    }
}
