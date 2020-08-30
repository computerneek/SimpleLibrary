package simplelibrary.opengl.gui.components;
import org.lwjgl.opengl.GL11;
public class MenuComponentScrollable extends MenuComponent{
    public final double horizScrollbarHeight;
    public final double vertScrollbarWidth;
    public final boolean alwaysShowHorizScrollbar;
    public final boolean alwaysShowVertScrollbar;
    private double scrollX = 0;
    private double scrollY = 0;
    private double maxScrollX = 0;
    private double maxScrollY = 0;
    private double horizClickOff, vertClickOff;
    private boolean horizScrollbarPresent = true;
    private boolean vertScrollbarPresent = true;
    private boolean vertPressed, horizPressed;
    private boolean vertZooming, horizZooming;
    private boolean upPress, downPress, leftPress, rightPress;
    private double scrollMagnitude;
    private double horizWidth;
    private double vertHeight;
    private double vertCenter;
    private double horizCenter;
    private double myX;
    private double myY;
    private double scrollWheelMagnitude=1;
    public MenuComponentScrollable(double x, double y, double width, double height, double horizScrollbarHeight, double vertScrollbarWidth) {
        this(x, y, width, height, horizScrollbarHeight, vertScrollbarWidth, true, true);
    }
    public MenuComponentScrollable(double x, double y, double width, double height, double horizScrollbarHeight, double vertScrollbarWidth, boolean alwaysShowVertScrollbar, boolean alwaysShowHorizScrollbar){
        super(x, y, width, height);
        this.horizScrollbarHeight = horizScrollbarHeight;
        this.vertScrollbarWidth = vertScrollbarWidth;
        this.alwaysShowHorizScrollbar = alwaysShowHorizScrollbar;
        this.alwaysShowVertScrollbar = alwaysShowVertScrollbar;
        scrollMagnitude = Math.min(width, height)/50;
    }
    @Override
    public void render() {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, 0);
        drawScrollbars();
        GL11.glPopMatrix();
    }
    @Override
    public void draw() {
        double contentHeight = 0;
        double contentWidth = 0;
        for(MenuComponent c : components){
            contentHeight = Math.max(contentHeight, c.height+c.y);
            contentWidth = Math.max(contentWidth, c.width+c.x);
        }
        maxScrollX = contentWidth-width;
        maxScrollY = contentHeight-height+(maxScrollX>0||alwaysShowHorizScrollbar?horizScrollbarHeight:0);
        maxScrollX = contentWidth-width+(maxScrollY>0||alwaysShowVertScrollbar?vertScrollbarWidth:0);
        horizScrollbarPresent = alwaysShowHorizScrollbar||maxScrollX>0;
        vertScrollbarPresent = alwaysShowVertScrollbar||maxScrollY>0;
        scrollX = Math.max(0, Math.min(maxScrollX, scrollX));
        scrollY = Math.max(0, Math.min(maxScrollY, scrollY));
        render();
    }
    @Override
    public void render(int millisSinceLastTick) {
        renderBackground();
        draw();//renderBackground() and draw() functions can both be used to draw the component
        GL11.glColor4f(1, 1, 1, 1);//White
        double vertWidth = vertScrollbarPresent?vertScrollbarWidth:0;
        double horizHeight = horizScrollbarPresent?horizScrollbarHeight:0;
        if(addRenderBound(x, y, x+width-vertWidth, y+height-horizHeight)){
            GL11.glPushMatrix();
            GL11.glTranslated(x-scrollX, y-scrollY, 0);
            translate(x-scrollX, y-scrollY);
            for(MenuComponent c : components){
                c.render(millisSinceLastTick);
            }
            removeRenderBound();//Translate
            removeRenderBound();//Added bound
            GL11.glPopMatrix();
        }
    }
    @Override
    public void tick() {
        super.tick();
        if(vertZooming){
            if(myY<vertCenter-vertHeight/2&&myY>vertScrollbarWidth) zoomUp();
            if(myY>vertCenter+vertHeight/2&&myY<height-vertScrollbarWidth-(horizScrollbarPresent?horizScrollbarHeight:0)) zoomDown();
        }
        if(horizZooming){
            if(myX<horizCenter-horizWidth/2&&myX>horizScrollbarHeight) zoomLeft();
            if(myX>horizCenter+horizWidth/2&&myX<width-horizScrollbarHeight-(vertScrollbarPresent?vertScrollbarWidth:0)) zoomRight();
        }
    }
    @Override
    public boolean onTabPressed(MenuComponent component) {
        return parent.onTabPressed(component);
    }
    @Override
    public boolean onReturnPressed(MenuComponent component) {
        return parent.onReturnPressed(component);
    }
    @Override
    public void onMouseButton(double x, double y, int button, boolean pressed, int mods) {
        if(button==0&&!pressed){
            vertPressed = vertZooming = horizPressed = horizZooming = false;
            upPress = downPress = leftPress = rightPress = false;
        }else if(button==0&&pressed){
            double vertWidth = vertScrollbarPresent?vertScrollbarWidth:0;
            double horizHeight = horizScrollbarPresent?horizScrollbarHeight:0;
            if(horizScrollbarPresent&&y>=height-horizScrollbarHeight){
                if(isClickWithinBounds(x, y, 0, height-horizHeight, horizHeight, height)) scrollLeft();
                else if(isClickWithinBounds(x, y, width-vertWidth-horizHeight, height-horizHeight, width-vertWidth, height)) scrollRight();
                else if(x<horizCenter-horizWidth/2) horizZooming = true;
                else if(x>horizCenter+horizWidth/2) horizZooming = true;
                else{
                    horizClickOff = horizCenter-x;
                    horizPressed = true;
                }
            }else if(vertScrollbarPresent&&x>=width-vertScrollbarWidth){
                if(isClickWithinBounds(x, y, width-vertWidth, 0, width, vertWidth)) scrollUp();
                else if(isClickWithinBounds(x, y, width-vertWidth, height-vertWidth-horizHeight, width, height-horizHeight)) scrollDown();
                else if(y<vertCenter-vertHeight/2) vertZooming = true;
                else if(y>vertCenter+vertHeight/2) vertZooming = true;
                else{
                    vertClickOff = vertCenter-y;
                    vertPressed = true;
                }
            }
        }
        myX = x;
        myY = y;
        if(x>width-(vertScrollbarPresent?vertScrollbarWidth:0)||y>height-(horizScrollbarPresent?horizScrollbarHeight:0)){//Click events on the scrollbar
            x=y=Double.NaN;
        }else{
            x+=scrollX;
            y+=scrollY;
        }
        super.onMouseButton(x, y, button, pressed, mods);
    }
    @Override
    public void onMouseMove(double x, double y) {
        if(x<width-vertScrollbarWidth) vertZooming = false;
        if(y<height-horizScrollbarHeight) horizZooming = false;
        if(vertPressed) scrollVert(y-(horizScrollbarPresent?horizScrollbarHeight:0)+vertClickOff);
        if(horizPressed) scrollHoriz(x-(vertScrollbarPresent?vertScrollbarWidth:0)+horizClickOff);
        boolean elsewhere = false;
        if(vertScrollbarPresent&&x>=width-vertScrollbarWidth) elsewhere = true;
        if(horizScrollbarPresent&&y>=height-horizScrollbarHeight) elsewhere = true;
        myX = x;
        myY = y;
        if(!horizPressed&&!horizZooming&&!vertPressed&&!vertZooming){
            x+=scrollX;
            y+=scrollY;
            if(elsewhere) super.onMouseMovedElsewhere(x, y);
            else super.onMouseMove(x, y);
        }
    }
    @Override
    public void onMouseMovedElsewhere(double x, double y) {
        if(x<width-vertScrollbarWidth) vertZooming = false;
        if(y<height-horizScrollbarHeight) horizZooming = false;
        if(vertPressed) scrollVert(y-(horizScrollbarPresent?horizScrollbarHeight:0)+vertClickOff);
        if(horizPressed) scrollHoriz(x-(vertScrollbarPresent?vertScrollbarWidth:0)+horizClickOff);
        myX = x;
        myY = y;
        if(!horizPressed&&!horizZooming&&!vertPressed&&!vertZooming){
            x+=scrollX;
            y+=scrollY;
            super.onMouseMovedElsewhere(x, y);
        }
    }
    @Override
    public boolean onMouseScrolled(double x, double y, double dx, double dy) {
        if(super.onMouseScrolled(x+scrollX, y+scrollY, dx, dy)) return true;
        boolean scrolled = false;
        if(dx>0&&scrollX>0){
            scrollX = Math.max(0, scrollX-dx*scrollWheelMagnitude);//Scroll left
            scrolled = true;
        }
        if(dy>0&&scrollY>0){
            scrollY = Math.max(0, scrollY-dy*scrollWheelMagnitude);//Scroll up
            scrolled = true;
        }
        if(dx<0&&scrollX<maxScrollX){
            scrollX = Math.min(maxScrollX, scrollX-dx*scrollWheelMagnitude);//Scroll right
            scrolled = true;
        }
        if(dy<0&&scrollY<maxScrollY){
            scrollY = Math.min(maxScrollY, scrollY-dy*scrollWheelMagnitude);//Scroll down
            scrolled = true;
        }
        return scrolled;
    }
    private void drawScrollbars() {
        if(vertScrollbarPresent){
            double barTop = 0;
            double spaceHeight = height-(horizScrollbarPresent?horizScrollbarHeight:0);
            double barBottom = spaceHeight;
            double barLeft = width-vertScrollbarWidth;
            drawVerticalScrollbarBackground(barLeft, 0, vertScrollbarWidth, barBottom);
            drawUpwardScrollbarButton(barLeft, barTop, vertScrollbarWidth, vertScrollbarWidth);
            barTop+=vertScrollbarWidth;
            drawDownwardScrollbarButton(barLeft, barBottom-vertScrollbarWidth, vertScrollbarWidth, vertScrollbarWidth);
            barBottom-=vertScrollbarWidth;
            double percentY = maxScrollY<=1?0.99:spaceHeight/(maxScrollY+spaceHeight);
            double barSpace = barBottom-barTop;
            double barHeight = percentY*barSpace;
            double barShift = barSpace-barHeight;
            percentY = scrollY/(double)(maxScrollY>0?maxScrollY:1);
            double posY = percentY*barShift;
            vertHeight = barHeight;
            vertCenter = barTop+posY+barHeight/2;
            drawVerticalScrollbarForeground(barLeft, barTop+posY, vertScrollbarWidth, barHeight);
        }
        if(horizScrollbarPresent){
            double barLeft = 0;
            double spaceWidth = width-(vertScrollbarPresent?vertScrollbarWidth:0);
            double barRight = spaceWidth;
            double barTop = height-horizScrollbarHeight;
            drawHorizontalScrollbarBackground(0, barTop, barRight, horizScrollbarHeight);
            drawLeftwardScrollbarButton(barLeft, barTop, horizScrollbarHeight, horizScrollbarHeight);
            barLeft+=horizScrollbarHeight;
            drawRightwardScrollbarButton(barRight-horizScrollbarHeight, barTop, horizScrollbarHeight, horizScrollbarHeight);
            barRight-=horizScrollbarHeight;
            double percentX = maxScrollX<=1?0.99:spaceWidth/(maxScrollX+spaceWidth);
            double barSpace = barRight-barLeft;
            double barWidth = percentX*barSpace;
            double barShift = barSpace-barWidth;
            percentX = scrollX/(maxScrollX>0?maxScrollX:1);
            double posX = percentX*barShift;
            horizWidth = barWidth;
            horizCenter = barLeft+posX+barWidth/2;
            drawHorizontalScrollbarForeground(barLeft+posX, barTop, barWidth, horizScrollbarHeight);
        }
    }
    //Last 2 params:  Height & width of horz. and vert. scrollbars (0 means no scrollbar)
    public void drawUpwardScrollbarButton(double x, double y, double width, double height){
        drawButton(x, y, width, height);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2d(x+width/2, y+height/4);
        GL11.glVertex2d(x+width/4, y+3*height/4);
        GL11.glVertex2d(x+3*width/4, y+3*height/4);
        GL11.glEnd();
    }
    public void drawDownwardScrollbarButton(double x, double y, double width, double height){
        drawButton(x, y, width, height);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2d(x+width/4, y+height/4);
        GL11.glVertex2d(x+3*width/4, y+height/4);
        GL11.glVertex2d(x+width/2, y+3*height/4);
        GL11.glEnd();
    }
    public void drawRightwardScrollbarButton(double x, double y, double width, double height){
        drawButton(x, y, width, height);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2d(x+width/4, y+height/4);
        GL11.glVertex2d(x+width/4, y+3*height/4);
        GL11.glVertex2d(x+3*width/4, y+height/2);
        GL11.glEnd();
    }
    public void drawLeftwardScrollbarButton(double x, double y, double width, double height){
        drawButton(x, y, width, height);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2d(x+width/4, y+height/2);
        GL11.glVertex2d(x+3*width/4, y+height/4);
        GL11.glVertex2d(x+3*width/4, y+3*height/4);
        GL11.glEnd();
    }
    public void drawVerticalScrollbarBackground(double x, double y, double width, double height){
        GL11.glColor3f(0.7f, 0.7f, 0.7f);
        drawRect(x, y, x+width, y+height, 0);
    }
    public void drawVerticalScrollbarForeground(double x, double y, double width, double height){
        GL11.glColor3f(0.5f, 0.5f, 0.5f);
        drawRect(x, y, x+width, y+height, 0);
    }
    public void drawHorizontalScrollbarBackground(double x, double y, double width, double height){
        GL11.glColor3f(0.7f, 0.7f, 0.7f);
        drawRect(x, y, x+width, y+height, 0);
    }
    public void drawHorizontalScrollbarForeground(double x, double y, double width, double height){
        GL11.glColor3f(0.5f, 0.5f, 0.5f);
        drawRect(x, y, x+width, y+height, 0);
    }
    public void drawButton(double x, double y, double width, double height){
                GL11.glColor3f(0.5f, 0.5f, 0.5f);
                drawRect(x, y, x+width, y+height, 0);
                GL11.glColor3f(0.2f, 0.2f, 0.2f);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex2d(x, y);
                GL11.glVertex2d(x, y+height-1);
                GL11.glVertex2d(x, y+height-1);
                GL11.glVertex2d(x+width-1, y+height-1);
                GL11.glVertex2d(x+width-1, y+height-1);
                GL11.glVertex2d(x+width-1, y);
                GL11.glVertex2d(x+width-1, y);
                GL11.glVertex2d(x, y);
                GL11.glEnd();
            }
    public void scrollUp(){
        scrollY = Math.max(0, scrollY-scrollMagnitude);
    }
    public void scrollDown(){
        scrollY = Math.min(maxScrollY, scrollY+scrollMagnitude);
    }
    public void scrollLeft(){
        scrollX = Math.max(0, scrollX-scrollMagnitude);
    }
    public void scrollRight(){
        scrollX = Math.min(maxScrollX, scrollX+scrollMagnitude);
    }
    public void scrollVert(double y){
        if(maxScrollY<1) return;
        double spaceHeight = height-(horizScrollbarPresent?horizScrollbarHeight:0);
        double percentY = maxScrollY<=1?2:spaceHeight/(maxScrollY+spaceHeight);
        double barHeight = (percentY*(spaceHeight-vertScrollbarWidth*2));
        y-=barHeight/2;
        double maxEffective = Math.round(spaceHeight-vertScrollbarWidth*2-barHeight);
        if(maxEffective<=0) return;//Should never be the case, but we can't be certain; this will prevent any possible /0 errors
        if(y<0) scrollY = 0;
        else if(y>maxEffective) scrollY = maxScrollY;
        else scrollY = (Math.round(y/maxEffective*maxScrollY));
    }
    public void scrollHoriz(double x){
        if(maxScrollX<1) return;
        double spaceWidth = width-(vertScrollbarPresent?vertScrollbarWidth:0);
        double percentX = maxScrollX<1?2:spaceWidth/(maxScrollX+spaceWidth);
        double barWidth = (percentX*(spaceWidth-horizScrollbarHeight*2));
        x-=barWidth/2;
        double maxEffective = Math.round(spaceWidth-horizScrollbarHeight*2-barWidth);
        if(maxEffective<=0) return;
        if(x<0) scrollX = 0;
        else if(x>maxEffective) scrollX = maxScrollX;
        else scrollX = (Math.round(x/maxEffective*maxScrollX));
    }
    public void setScrollMagnitude(double scrollMagnitude){
        this.scrollMagnitude = scrollMagnitude;
    }
    public void setScrollWheelMagnitude(double scrollMagnitude){
        this.scrollWheelMagnitude = scrollMagnitude;
    }
    public boolean hasVertScrollbar(){
        return vertScrollbarPresent&&vertScrollbarWidth>0;
    }
    public boolean hasHorizScrollbar(){
        return horizScrollbarPresent&&horizScrollbarHeight>0;
    }
    public double getVertScroll(){ return scrollY; }
    public double getHorizScroll(){ return scrollX; }
    private void zoomUp(){
        scrollY = Math.max(0, scrollY-(height-(horizScrollbarPresent?horizScrollbarHeight:0))/2);
    }
    private void zoomDown(){
        scrollY = Math.min(maxScrollY, scrollY+(height-(horizScrollbarPresent?horizScrollbarHeight:0))/2);
    }
    private void zoomLeft(){
        scrollX = Math.min(0, scrollX-(width-(vertScrollbarPresent?vertScrollbarWidth:0))/2);
    }
    private void zoomRight(){
        scrollX = Math.max(maxScrollX, scrollX+(width-(vertScrollbarPresent?vertScrollbarWidth:0))/2);
    }
}
