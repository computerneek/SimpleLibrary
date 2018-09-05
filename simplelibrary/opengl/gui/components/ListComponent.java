package simplelibrary.opengl.gui.components;
import simplelibrary.opengl.Renderer2D;
@Deprecated
public abstract class ListComponent extends MenuComponent{
    private boolean isPressed;
    public ListComponent(){
        super(0, 0, 0, 0);
        width = getWidthD();
        height = getHeightD();
    }
    /**
     * Gets the desired width of the ListComponent.  This value is never honored by the default lists.
     * Use <code>MenuComponent.width</code> to determine actual width.
     * Note:  In the default single-column list, the actual width MAY VARY depending on scrollbar presence status.
     */
    public abstract double getWidthD();
    /**
     * Gets the desired height of the ListComponent.  This value is not honored by the default multicolumn list.
     * Use <code>MenuComponent.height</code> to determine actual height.
     */
    public abstract double getHeightD();
    public abstract void render(double x, double y, double minX, double minY, double maxX, double maxY);
    @Override
    public void mouseEvent(double x, double y, int button, boolean isDown) {
        if(button==0&&isDown==true){
            isPressed = true;
        }else if(button==0&&isDown==false&&isPressed){
            isPressed = false;
            onClicked(x, y, button);
        }
    }
    @Override
    public void mouseover(double x, double y, boolean isMouseOver) {
        super.mouseover(x, y, isMouseOver);
        if(!isMouseOver){
            isPressed = false;
        }
    }
    public abstract void onClicked(double x, double y, int button);
    @Override
    public void render() {
        render(x, y, x, y, x+width, y+height);//assume component bounds are accurate
    }
}
