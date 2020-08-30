package simplelibrary.opengl;
import org.lwjgl.opengl.GL11;
import simplelibrary.Stack;
import simplelibrary.font.FontManager;
public abstract class Renderer2D{
    private static Stack<double[]> bound = new Stack<>();
    private static Stack<Stack<double[]>> bounds = new Stack<>();
    /**
     * Translates the render bound, in order to account for an OpenGL translate call of the SAME dimensions.
     */
    public static void translate(double x, double y){
        double[] current = bound.peek();
        if(current==null) return;
        bound.push(new double[]{current[0]-x, current[1]-y, current[2]-x, current[3]-y});
    }
    public static void untranslate(){
        if(bound.isEmpty()) return;//No changes were made.
        double[] current = bound.pop();
        double[] last = bound.peek();
        if(current[0]>=last[0]&&current[1]>=last[1]&&current[2]<=last[2]&&current[3]<=last[3]) bound.push(current);//If it wasn't a translation...
    }
    public static boolean isClickWithinBounds(double clickX, double clickY, double targetXMin, double targetYMin, double targetXMax, double targetYMax){
        return clickX>=targetXMin&&clickY>=targetYMin&&clickX<=targetXMax&&clickY<=targetYMax;
    }
    public static boolean drawText(double leftEdge, double topEdge, double rightPossibleEdge, double bottomEdge, String text){
        boolean trimmed = false;
        double distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        while(distanceForText>rightPossibleEdge-leftEdge&&!text.isEmpty()){
            trimmed = true;
            text = text.substring(0, text.length()-1);
            distanceForText = FontManager.getLengthForStringWithHeight(text+"...", bottomEdge-topEdge);
        }
        if(text.isEmpty()){
            return false;
        }
        if(trimmed){
            text+="...";
        }
        double scale = FontManager.getLengthForStringWithHeight("M", bottomEdge-topEdge)/FontManager.getCharLength('M');
        double skip = 0;
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        for(char character : chars){
            drawRect(leftEdge+skip, topEdge, leftEdge+skip+(FontManager.getCharLength(character)*scale), bottomEdge, FontManager.getFontImage(), FontManager.getTextureLocationForChar(character));
            skip+=(FontManager.getCharLength(character)*scale);
        }
        return true;
    }
    public static boolean drawTextWithBounds(double leftEdge, double topEdge, double rightPossibleEdge, double bottomEdge, double minX, double minY, double maxX, double maxY, String text){
        boolean trimmed = false;
        double distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        while(distanceForText>rightPossibleEdge-leftEdge&&!text.isEmpty()){
            trimmed = true;
            text = text.substring(0, text.length()-1);
            distanceForText = FontManager.getLengthForStringWithHeight(text+"...", bottomEdge-topEdge);
        }
        if(text.isEmpty()){
            return false;
        }
        if(trimmed){
            text+="...";
        }
        double scale = FontManager.getLengthForStringWithHeight("M", bottomEdge-topEdge)/FontManager.getCharLength('M');
        double skip = 0;
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        for(char character : chars){
            drawRectWithBounds(leftEdge+skip, topEdge, leftEdge+skip+(FontManager.getCharLength(character)*scale), bottomEdge, minX, minY, maxX, maxY, FontManager.getFontImage(), FontManager.getTextureLocationForChar(character));
            skip+=(FontManager.getCharLength(character)*scale);
        }
        return true;
    }
    public static String drawTextWithWrap(double leftEdge, double topEdge, double rightPossibleEdge, double bottomEdge, String text){
        String originalText = text;
        double distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        while(distanceForText>rightPossibleEdge-leftEdge&&!text.isEmpty()){
            text = text.substring(0, text.length()-1);
            distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        }
        if(text.isEmpty()){
            return originalText;
        }
        double scale = FontManager.getLengthForStringWithHeight("M", bottomEdge-topEdge)/FontManager.getCharLength('M');
        double skip = 0;
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        for(char character : chars){
            drawRect(leftEdge+skip, topEdge, leftEdge+skip+(FontManager.getCharLength(character)*scale), bottomEdge, FontManager.getFontImage(), FontManager.getTextureLocationForChar(character));
            skip+=(FontManager.getCharLength(character)*scale);
        }
        return originalText.substring(text.length());
    }
    public static String drawTextWithWrapAndBounds(double leftEdge, double topEdge, double rightPossibleEdge, double bottomEdge, double minX, double minY, double maxX, double maxY, String text){
        String originalText = text;
        double distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        while(distanceForText>rightPossibleEdge-leftEdge&&!text.isEmpty()){
            text = text.substring(0, text.length()-1);
            distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        }
        if(text.isEmpty()){
            return originalText;
        }
        double scale = FontManager.getLengthForStringWithHeight("M", bottomEdge-topEdge)/FontManager.getCharLength('M');
        double skip = 0;
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        for(char character : chars){
            drawRectWithBounds(leftEdge+skip, topEdge, leftEdge+skip+(FontManager.getCharLength(character)*scale), bottomEdge, minX, minY, maxX, maxY, FontManager.getFontImage(), FontManager.getTextureLocationForChar(character));
            skip+=(FontManager.getCharLength(character)*scale);
        }
        return originalText.substring(text.length());
    }
    public static boolean drawCenteredText(double leftPossibleEdge, double topEdge, double rightPossibleEdge, double bottomEdge, String text){
        boolean trimmed = false;
        double distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        while(distanceForText>Math.abs(rightPossibleEdge-leftPossibleEdge)&&!text.isEmpty()){
            trimmed = true;
            text = text.substring(0, text.length()-1);
            distanceForText = FontManager.getLengthForStringWithHeight(text+"...", bottomEdge-topEdge);
        }
        if(text.isEmpty()){
            return false;
        }
        if(trimmed){
            text+="...";
        }
        double scale = FontManager.getLengthForStringWithHeight("M", bottomEdge-topEdge)/FontManager.getCharLength('M');
        double skip = (Math.abs(rightPossibleEdge-leftPossibleEdge)-distanceForText)/2;
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        for(char character : chars){
            drawRect(leftPossibleEdge+skip, topEdge, leftPossibleEdge+skip+(FontManager.getCharLength(character)*scale), bottomEdge, FontManager.getFontImage(), FontManager.getTextureLocationForChar(character));
            skip+=(FontManager.getCharLength(character)*scale);
        }
        return true;
    }
    public static boolean drawCenteredTextWithBounds(double leftPossibleEdge, double topEdge, double rightPossibleEdge, double bottomEdge, double minX, double minY, double maxX, double maxY, String text){
        boolean trimmed = false;
        double distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        while(distanceForText>rightPossibleEdge-leftPossibleEdge&&!text.isEmpty()){
            trimmed = true;
            text = text.substring(0, text.length()-1);
            distanceForText = FontManager.getLengthForStringWithHeight(text+"...", bottomEdge-topEdge);
        }
        if(text.isEmpty()){
            return false;
        }
        if(trimmed){
            text+="...";
        }
        double scale = FontManager.getLengthForStringWithHeight("M", bottomEdge-topEdge)/FontManager.getCharLength('M');
        double skip = (Math.abs(rightPossibleEdge-leftPossibleEdge)-distanceForText)/2;
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        for(char character : chars){
            drawRectWithBounds(leftPossibleEdge+skip, topEdge, leftPossibleEdge+skip+(FontManager.getCharLength(character)*scale), bottomEdge, minX, minY, maxX, maxY, FontManager.getFontImage(), FontManager.getTextureLocationForChar(character));
            skip+=(FontManager.getCharLength(character)*scale);
        }
        return true;
    }
    public static String drawCenteredTextWithWrap(double leftPossibleEdge, double topEdge, double rightPossibleEdge, double bottomEdge, String text){
        String originalText = text;
        double distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        while(distanceForText>rightPossibleEdge-leftPossibleEdge&&!text.isEmpty()){
            text = text.substring(0, text.length()-1);
            distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        }
        if(text.isEmpty()){
            return originalText;
        }
        double scale = FontManager.getLengthForStringWithHeight("M", bottomEdge-topEdge)/FontManager.getCharLength('M');
        double skip = (Math.abs(rightPossibleEdge-leftPossibleEdge)-distanceForText)/2;
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        for(char character : chars){
            drawRect(leftPossibleEdge+skip, topEdge, leftPossibleEdge+skip+(FontManager.getCharLength(character)*scale), bottomEdge, FontManager.getFontImage(), FontManager.getTextureLocationForChar(character));
            skip+=(FontManager.getCharLength(character)*scale);
        }
        return originalText.substring(text.length());
    }
    public static String drawCenteredTextWithWrapAndBounds(double leftPossibleEdge, double topEdge, double rightPossibleEdge, double bottomEdge, double minX, double minY, double maxX, double maxY, String text){
        String originalText = text;
        double distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        while(distanceForText>rightPossibleEdge-leftPossibleEdge&&!text.isEmpty()){
            text = text.substring(0, text.length()-1);
            distanceForText = FontManager.getLengthForStringWithHeight(text, bottomEdge-topEdge);
        }
        if(text.isEmpty()){
            return originalText;
        }
        double scale = FontManager.getLengthForStringWithHeight("M", bottomEdge-topEdge)/FontManager.getCharLength('M');
        double skip = (Math.abs(rightPossibleEdge-leftPossibleEdge)-distanceForText)/2;
        char[] chars = new char[text.length()];
        text.getChars(0, chars.length, chars, 0);
        for(char character : chars){
            drawRectWithBounds(leftPossibleEdge+skip, topEdge, leftPossibleEdge+skip+(FontManager.getCharLength(character)*scale), bottomEdge, minX, minY, maxX, maxY, FontManager.getFontImage(), FontManager.getTextureLocationForChar(character));
            skip+=(FontManager.getCharLength(character)*scale);
        }
        return originalText.substring(text.length());
    }
    /**
     * Draws a rectangle at the specified location
     * NOTE:  The top and bottom coordinates are multiplied by -1 to get
     * the render location.  If <code>bottom</code> is less than <code>topEdge
     * </code>, it will draw upside-down.
     * @param left The left edge of the rectangle, in OpenGL coordinates
     * @param top The top edge of the rectangle, in OpenGL coordinates
     * @param right The right edge of the rectangle, in OpenGL coordinates
     * @param bottom The bottom edge of the rectangle, in OpenGL coordinates
     * @param texture The texture (image) for the rectangle (0 means no texture; solid color)
     * @since GUI module 3.3.0.0
     */
    public static void drawRect(double left, double top, double right, double bottom, int texture){
        drawRect(left, top, right, bottom, texture, 0, 0, 1, 1);
    }
    public static void drawRect(double left, double top, double right, double bottom, int texture, double[] texLocation){
        drawRect(left, top, right, bottom, texture, texLocation[0], texLocation[1], texLocation[2], texLocation[3]);
    }
    /**
     * Draws a rectangle at the specified location
     * NOTE:  The top and bottom coordinates are multiplied by -1 to get
     * the render location.  If <code>bottom</code> is less than <code>topEdge
     * </code>, it will draw upside-down.
     * @param left The left edge of the rectangle, in OpenGL coordinates
     * @param top The top edge of the rectangle, in OpenGL coordinates
     * @param right The right edge of the rectangle, in OpenGL coordinates
     * @param bottom The bottom edge of the rectangle, in OpenGL coordinates
     * @param texture The texture (image) for the rectangle (0 means no texture; solid color)
     * @since GUI module 3.3.0.0
     */
    public static void drawRect(double left, double top, double right, double bottom, int texture, double texLeft, double texTop, double texRight, double texDown){
        if(!bound.isEmpty()){
            double[] bound = Renderer2D.bound.peek();
            if(bound==null) return;
            drawRectWithBounds(left, top, right, bottom, bound[0], bound[1], bound[2], bound[3], texture, texLeft, texTop, texRight, texDown);
            return;
        }
        ImageStash.instance.bindTexture(texture);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(texLeft, texTop);
        GL11.glVertex2d(left, top);
        GL11.glTexCoord2d(texRight, texTop);
        GL11.glVertex2d(right, top);
        GL11.glTexCoord2d(texRight, texDown);
        GL11.glVertex2d(right, bottom);
        GL11.glTexCoord2d(texLeft, texDown);
        GL11.glVertex2d(left, bottom);
        GL11.glEnd();
    }
    /**
     * Draws a rectangle at the specified location and trims it to fit within
     * the specified coordinates.  The texture will look like it slides behind
     * the rest of the screen.
     * NOTE:  The top and bottom coordinates are multiplied by -1 to get
     * the render location.  If <code>bottom</code> is less than <code>topEdge
     * </code>, it will draw upside-down.
     * @param left The left edge of the rectangle, in OpenGL coordinates
     * @param top The top edge of the rectangle, in OpenGL coordinates
     * @param right The right edge of the rectangle, in OpenGL coordinates
     * @param bottom The bottom edge of the rectangle, in OpenGL coordinates
     * @param minX The minimum X-coordinate to be rendered on, in OpenGL coordinates
     * @param minY The minimum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param maxX The maximum X-coordinate to be rendered on, in OpenGL coordinates
     * @param maxY The maximum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param texture The texture (image) for the rectangle (0 means no texture; solid color)
     * @since GUI module 3.3.0.0
     */
    public static void drawRectWithBounds(double left, double top, double right, double bottom, double minX, double minY, double maxX, double maxY, int texture){
        drawRectWithBounds(left, top, right, bottom, minX, minY, maxX, maxY, texture, 0, 0, 1, 1);
    }
    public static void drawRectWithBounds(double left, double top, double right, double bottom, double minX, double minY, double maxX, double maxY, int texture, double[] texLocation){
        drawRectWithBounds(left, top, right, bottom, minX, minY, maxX, maxY, texture, texLocation[0], texLocation[1], texLocation[2], texLocation[3]);
    }
    /**
     * Draws a rectangle at the specified location and trims it to fit within
     * the specified coordinates.  The texture will look like it slides behind
     * the rest of the screen.
     * NOTE:  The top and bottom coordinates are multiplied by -1 to get
     * the render location.  If <code>bottom</code> is less than <code>topEdge
     * </code>, it will draw upside-down.
     * @param left The left edge of the rectangle, in OpenGL coordinates
     * @param top The top edge of the rectangle, in OpenGL coordinates
     * @param right The right edge of the rectangle, in OpenGL coordinates
     * @param bottom The bottom edge of the rectangle, in OpenGL coordinates
     * @param minX The minimum X-coordinate to be rendered on, in OpenGL coordinates
     * @param minY The minimum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param maxX The maximum X-coordinate to be rendered on, in OpenGL coordinates
     * @param maxY The maximum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param texture The texture (image) for the rectangle (0 means no texture; solid color)
     * @since GUI module 3.3.0.0
     */
    public static void drawRectWithBounds(double left, double top, double right, double bottom, double minX, double minY, double maxX, double maxY, int texture, double texLeft, double texTop, double texRight, double texBottom){
        if((right<minX&&left<minX)||(right>maxX&&left>maxX)||(top<minY&&bottom<minY)||(bottom>maxY&&top>maxY)){
            return;
        }
        double texLeft_, texTop_, texRight_, texBottom_;
        double width=right-left;
        double height=bottom-top;
        if(width==0||height==0){
            return;
        }
        if(left>=minX){
            texLeft_ = 0;
        }else{
            double diff=minX-left;
            texLeft_ = diff/width;
            left=minX;
        }
        if(top>=minY){
            texTop_ = 0;
        }else{
            double diff=minY-top;
            texTop_ = diff/height;
            top=minY;
        }
        if(right<=maxX){
            texRight_ = 1;
        }else{
            double diff=right-maxX;
            texRight_ = 1F-diff/width;
            right=maxX;
        }
        if(bottom<=maxY){
            texBottom_ = 1;
        }else{
            double diff=bottom-maxY;
            texBottom_ = 1F-diff/height;
            bottom=maxY;
        }
        texLeft_ = texLeft+(texRight-texLeft)*texLeft_;
        texRight_ = texLeft+(texRight-texLeft)*texRight_;
        texTop_ = texTop+(texBottom-texTop)*texTop_;
        texBottom_ = texTop+(texBottom-texTop)*texBottom_;
        ImageStash.instance.bindTexture(texture);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2d(texLeft_, texTop_);
        GL11.glVertex2d(left, top);
        GL11.glTexCoord2d(texRight_, texTop_);
        GL11.glVertex2d(right, top);
        GL11.glTexCoord2d(texRight_, texBottom_);
        GL11.glVertex2d(right, bottom);
        GL11.glTexCoord2d(texLeft_, texBottom_);
        GL11.glVertex2d(left, bottom);
        GL11.glEnd();
    }
    public static void drawBorder(double left, double top, double right, double bottom){
        drawLine(left, top, left, bottom);
        drawLine(left, top, right, top);
        drawLine(left, bottom, right, bottom);
        drawLine(right, top, right, bottom);
    }
    public static void drawLine(double x1, double y1, double x2, double y2){
        if(!bound.isEmpty()){
            double[] bound = Renderer2D.bound.peek();
            if(bound==null) return;
            drawLineWithBounds(x1, y1, x2, y2, bound[0], bound[1], bound[2], bound[3]);
            return;
        }
        ImageStash.instance.bindTexture(0);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
    }
    public static void drawLineWithBounds(double x1, double y1, double x2, double y2, double minX, double minY, double maxX, double maxY){
        if(minX>maxX||minY>maxY) return;
        if((x1<minX&&x2<minX)||(x1>maxX&&x2>maxX)||(y1<minY&&y2<minY)||(y1>maxY&&y2>maxY)) return;
        if(x1<minX){
            double f = (x2-minX)/(x2-x1);
            x1 = minX;
            y1 = y2+f*(y1-y2);
        }
        if(x2<minX){
            double f = (x1-minX)/(x1-x2);
            x2 = minX;
            y2 = y1+f*(y2-y1);
        }
        if(x1>maxX){
            double f = (maxX-x2)/(x1-x2);
            x1 = maxX;
            y1 = y2+f*(y1-y2);
        }
        if(x2>maxX){
            double f = (maxX-x1)/(x2-x1);
            x2 = maxX;
            y2 = y1+f*(y2-y1);
        }
        if((x1<minX&&x2<minX)||(x1>maxX&&x2>maxX)||(y1<minY&&y2<minY)||(y1>maxY&&y2>maxY)) return;
        if(y1<minY){
            double f = (y2-minY)/(y2-y1);
            y1 = minY;
            x1 = x2+f*(x1-x2);
        }
        if(y2<minY){
            double f = (y1-minY)/(y1-y2);
            y2 = minY;
            x2 = x1+f*(x2-x1);
        }
        if(y1>maxY){
            double f = (maxY-y2)/(y1-y2);
            y1 = maxY;
            x1 = x2+f*(x1-x2);
        }
        if(y2>maxY){
            double f = (maxY-y1)/(y2-y1);
            y2 = maxY;
            x2 = x1+f*(x2-x1);
        }
        if((x1<minX&&x2<minX)||(x1>maxX&&x2>maxX)||(y1<minY&&y2<minY)||(y1>maxY&&y2>maxY)) return;
        ImageStash.instance.bindTexture(0);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
    }
    public static void applyRenderBound(double minX, double minY, double maxX, double maxY){
        if(!addRenderBound(minX, minY, maxX, maxY)){
            bound.push(null);
        }
    }
    @Deprecated
    public static void cancelRenderBound(){
        removeRenderBound();
    }
    public static boolean addRenderBound(double minX, double minY, double maxX, double maxY){
        if(!bound.isEmpty()){
            double[] current = bound.peek();
            if(current!=null){
                minX = Math.max(minX, current[0]);
                minY = Math.max(minY, current[1]);
                maxX = Math.min(maxX, current[2]);
                maxY = Math.min(maxY, current[3]);
            }
        }
        return addAbsoluteRenderBound(minX, minY, maxX, maxY);
    }
    public static void removeRenderBound(){
        bound.pop();
    }
    public static boolean addAbsoluteRenderBound(double minX, double minY, double maxX, double maxY){
        if(maxX<=minX||maxY<=minY) return false;//Invalid coordinates- nothing would be drawn.
        bound.push(new double[]{minX, minY, maxX, maxY});
        return true;
    }
    public static void bound(double minX, double minY, double maxX, double maxY, Runnable render){
        if(!addRenderBound(minX, minY, maxX, maxY)) return;
        render.run();
        removeRenderBound();
    }
    public static void clearBoundStack(){
        bound.clear();
    }
    public static void pushBoundStack(){
        bounds.push(bound);
        bound = bound.copy();
    }
    public static void pushAndClearBoundStack(){
        bounds.push(bound);
        bound = new Stack<>();
    }
    public static void popBoundStack(){
        if(bounds.isEmpty()) return;
        bound = bounds.pop();
    }
    public static int getBoundStackDepth(){
        return bounds.size();
    }
    public static int getBoundDepth(){
        return bound.size();
    }
}
