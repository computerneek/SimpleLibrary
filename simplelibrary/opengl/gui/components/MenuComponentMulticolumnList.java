package simplelibrary.opengl.gui.components;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.lwjgl.opengl.GL11;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.game.GameHelper;
import simplelibrary.opengl.ImageStash;
public class MenuComponentMulticolumnList extends MenuComponentScrollable{
    private static final Logger LOG = Logger.getLogger(MenuComponentMulticolumnList.class.getName());
    public double columnWidth;
    public double rowHeight;
    public int columnCount;
    private MenuComponent selectedComponent;
    public MenuComponentMulticolumnList(double x, double y, double width, double height, double columnWidth, double rowHeight){
        this(x, y, width, height, columnWidth, rowHeight, width/20);
    }
    public MenuComponentMulticolumnList(double x, double y, double width, double height, double columnWidth, double rowHeight, double scrollbarWidth){
        this(x, y, width, height, columnWidth, rowHeight, scrollbarWidth, false);
    }
    public MenuComponentMulticolumnList(double x, double y, double width, double height, double columnWidth, double rowHeight, double scrollbarWidth, boolean alwaysShowScrollbar){
        super(x, y, width, height, 0, scrollbarWidth, alwaysShowScrollbar, false);
        this.columnWidth=columnWidth;
        this.rowHeight=rowHeight;
        columnCount = Math.max(1, (int)((width-(width%columnWidth))/columnWidth));
    }
    @Override
    public void renderBackground() {
        if(selected!=null) selectedComponent = selected;
        double width = this.width-(hasVertScrollbar()?vertScrollbarWidth:0);
        columnCount = Math.max(1, (int)((width-(width%columnWidth))/columnWidth));
        int column = 0;
        double y = 0;
        for(MenuComponent c : components){
            c.x = column*columnWidth;
            c.y = y;
            c.width = columnWidth;
            c.height = rowHeight;
            column = (column+1)%columnCount;
            if(column==0) y+=rowHeight;
        }
        super.renderBackground();
    }
    public boolean scrollbarPresent(){
        return hasVertScrollbar();
    }
    public int getSelectedIndex(){
        return components.indexOf(selectedComponent);
    }
    public void setSelectedIndex(int index){
        if(index<0||index>=components.size()) selectedComponent = null;
        else selectedComponent = components.get(index);
    }
}
