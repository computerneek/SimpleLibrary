package simplelibrary.opengl.gui.components;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
public class MenuComponentList extends MenuComponentScrollable{
    private static final Logger LOG = Logger.getLogger(MenuComponentList.class.getName());
    private MenuComponent selectedComponent;
    public MenuComponentList(double x, double y, double width, double height){
        this(x, y, width, height, height/20);
    }
    public MenuComponentList(double x, double y, double width, double height, double scrollbarWidth){
        this(x, y, width, height, scrollbarWidth, false);
    }
    public MenuComponentList(double x, double y, double width, double height, double scrollbarWidth, boolean alwaysShowScrollbar){
        super(x, y, width, height, 0, scrollbarWidth, alwaysShowScrollbar, false);
    }
    public boolean hasScrollbar(){
        return hasVertScrollbar();
    }
    @Override
    public void renderBackground() {
        if(selected!=null) selectedComponent = selected;
        double y = 0;
        for (MenuComponent c : components) {
            c.x = 0;
            c.y = y;
            y+=c.height;
            c.width = width-(hasScrollbar()?vertScrollbarWidth:0);
        }
        super.renderBackground();
    }
    public int getSelectedIndex(){
        return components.indexOf(selectedComponent);
    }
    public void setSelectedIndex(int index){
        if(index<0||index>=components.size()) selectedComponent = null;
        else selectedComponent = components.get(index);
    }
}
