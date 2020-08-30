package simplelibrary.opengl;
import org.lwjgl.opengl.GL11;
import simplelibrary.font.FontManager;

public class Renderer3D{
    /**
     * Tests that the specified point is within the specified rectangle.
     * 
     * This method returns true if all of the following hold true:
     * 
     * <code>pointX&#62;=targetXMin</code>,
     * <code>pointY&#62;=targetYMin</code>,
     * <code>tartetXMax&#62;=pointX</code>,
     * <code>tartetYMax&#62;=pointY</code>
     * @param pointX The point X-coordinate to test
     * @param pointY The point Y-coordinate to test
     * @param targetXMin The minimum X-coordinate
     * @param targetYMin The minimum Y-coordinate
     * @param targetXMax The maximum X-coordinate
     * @param targetYMax The maximum Y-coordinate
     * @return if the specified point is within the specified rectangle
     * @since GUI module 3.3.0.0
     */
    public static boolean isPointWithinBounds(double pointX, double pointY, double targetXMin, double targetYMin, double targetXMax, double targetYMax){
        return pointX>=targetXMin&&pointY>=targetYMin&&pointX<=targetXMax&&pointY<=targetYMax;
    }
    /**
     * Draws text at the specified location.  The text may not occupy
     * the full desired rectangle.  The text will be scaled to match the
     * vertical size of the specified rectangle, then truncated (if necessary)
     * to fit within the horizontal specifications.
     * NOTE:  The top and bottom edge coordinates are multiplied by -1 to get
     * the render location.  <code>bottomEdge</code> cannot be less than <code>
     * topEdge</code>for anything to draw.
     * @param leftEdge The left edge of the text, in OpenGL coordinates
     * @param topEdge The top edge of the text, in OpenGL coordinates
     * @param rightPossibleEdge The right edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param bottomEdge The bottom edge of the text, in OpenGL coordinates
     * @param text The text to render
     * @return if any text was drawn (only returns false if space was too small for text with truncation marks, if any)
     * @since GUI module 3.3.0.0
     */
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
    /**
     * Draws text at the specified location and trims it to fit within
     * the specified coordinates.  The text will look like it slides behind the
     * rest of the screen.  The text may not occupy the full desired rectangle.
     * The text will be scaled to match the vertical size of the specified
     * rectangle, then truncated (if necessary) to fit within the horizontal
     * specifications.  The minX/minY/maxX/maxY specifications do not affect the
     * text scaling or truncation but merely trim the visible parts of the text.
     * NOTE:  The top and bottom edge coordinates are multiplied by -1 to get
     * the render location.  <code>bottomEdge</code> cannot be less than <code>
     * topEdge</code> for anything to draw.
     * @param leftEdge The left edge of the text, in OpenGL coordinates
     * @param topEdge The top edge of the text, in OpenGL coordinates
     * @param rightPossibleEdge The right edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param bottomEdge The bottom edge of the text, in OpenGL coordinates
     * @param minX The minimum X-coordinate to be rendered on, in OpenGL coordinates
     * @param minY The minimum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param maxX The maximum X-coordinate to be rendered on, in OpenGL coordinates
     * @param maxY The maximum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param text The text to render
     * @return if any text was drawn (only returns false if space was too small for text with truncation marks, if any; minX/minY/maxX/maxY specifications do not affect return value)
     * @since GUI module 3.3.0.0
     */
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
    /**
     * Draws text at the specified location.  The text may not occupy
     * the full desired rectangle.  The text will be scaled to match the
     * vertical size of the specified rectangle, then truncated (if necessary)
     * to fit within the horizontal specifications.  No truncation marks are
     * used, but the truncated section is returned.
     * NOTE:  The top and bottom edge coordinates are multiplied by -1 to get
     * the render location.  <code>bottomEdge</code> cannot be less than <code>
     * topEdge</code>, for anything to draw.
     * @param leftEdge The left edge of the text, in OpenGL coordinates
     * @param topEdge The top edge of the text, in OpenGL coordinates
     * @param rightPossibleEdge The right edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param bottomEdge The bottom edge of the text, in OpenGL coordinates
     * @param text The text to render
     * @return The truncated section of the input text, if any.  Empty string if the text was not truncated.
     * @since GUI module 3.3.0.0
     */
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
    /**
     * Draws text at the specified location and trims it to fit within
     * the specified coordinates.  The text will look like it slides behind the
     * rest of the screen.  The text may not occupy the full desired rectangle.
     * The text will be scaled to match the vertical size of the specified
     * rectangle, then truncated (if necessary) to fit within the horizontal
     * specifications.  No truncation marks are used, but the truncated section
     * is returned.  The minX/minY/maxX/maxY specifications do not affect the
     * text scaling or truncation but merely trim the visible parts of the text.
     * NOTE:  The top and bottom edge coordinates are multiplied by -1 to get
     * the render location.  <code>bottomEdge</code> cannot be less than <code>
     * topEdge</code>, for anything to draw.
     * @param leftEdge The left edge of the text, in OpenGL coordinates
     * @param topEdge The top edge of the text, in OpenGL coordinates
     * @param rightPossibleEdge The right edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param bottomEdge The bottom edge of the text, in OpenGL coordinates
     * @param minX The minimum X-coordinate to be rendered on, in OpenGL coordinates
     * @param minY The minimum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param maxX The maximum X-coordinate to be rendered on, in OpenGL coordinates
     * @param maxY The maximum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param text The text to render
     * @return The truncated section of the input text, if any.  Empty string if the text was not truncated.
     * @since GUI module 3.3.0.0
     */
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
    /**
     * Draws text at the specified location.  The text may not occupy
     * the full desired rectangle.  The text will be scaled to match the
     * vertical size of the specified rectangle, then truncated (if necessary)
     * to fit within the horizontal specifications.  The text will be centered
     * horizontally in the desired rectangle.
     * NOTE:  The top and bottom edge coordinates are multiplied by -1 to get
     * the render location.  <code>bottomEdge</code> cannot be less than <code>
     * topEdge</code>, for anything to draw.
     * @param leftPossibleEdge The left edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param topEdge The top edge of the text, in OpenGL coordinates
     * @param rightPossibleEdge The right edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param bottomEdge The bottom edge of the text, in OpenGL coordinates
     * @param text The text to render
     * @return If any text was drawn (only returns false if space was too small for text with truncation marks, if any)
     * @since GUI module 3.3.0.0
     */
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
    /**
     * Draws text at the specified location and trims it to fit within
     * the specified coordinates.  The text will look like it slides behind the
     * rest of the screen.  The text may not occupy the full desired rectangle.
     * The text will be scaled to match the vertical size of the specified
     * rectangle, then truncated (if necessary) to fit within the horizontal
     * specifications.  The text will be centered horizontally in the desired
     * rectangle.  The minX/minY/maxX/maxY specifications do not affect the text
     * scaling or truncation but merely trim the visible parts of the text.
     * NOTE:  The top and bottom edge coordinates are multiplied by -1 to get
     * the render location.  <code>bottomEdge</code> cannot be less than <code>
     * topEdge</code>, for anything to draw.
     * @param leftPossibleEdge The left edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param topEdge The top edge of the text, in OpenGL coordinates
     * @param rightPossibleEdge The right edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param bottomEdge The bottom edge of the text, in OpenGL coordinates
     * @param minX The minimum X-coordinate to be rendered on, in OpenGL coordinates
     * @param minY The minimum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param maxX The maximum X-coordinate to be rendered on, in OpenGL coordinates
     * @param maxY The maximum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param text The text to render
     * @return If any text was drawn (only returns false if space was too small for text with truncation marks, if any)
     * @since GUI module 3.3.0.0
     */
    public static boolean drawCenteredTextWithBounds(double leftPossibleEdge, double topEdge, double rightPossibleEdge, double bottomEdge, double minX, double minY, double maxX, double maxY, String text){
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
            drawRectWithBounds(leftPossibleEdge+skip, topEdge, leftPossibleEdge+skip+(FontManager.getCharLength(character)*scale), bottomEdge, minX, minY, maxX, maxY, FontManager.getFontImage(), FontManager.getTextureLocationForChar(character));
            skip+=(FontManager.getCharLength(character)*scale);
        }
        return true;
    }
    /**
     * Draws text at the specified location.  The text may not occupy
     * the full desired rectangle.  The text will be scaled to match the
     * vertical size of the specified rectangle, then truncated (if necessary)
     * to fit within the horizontal specifications.  The text will be centered
     * horizontally in the desired rectangle.  No truncation marks are used, but
     * the truncated section is returned.
     * NOTE:  The top and bottom edge coordinates are multiplied by -1 to get
     * the render location.  <code>bottomEdge</code> cannot be less than <code>
     * topEdge</code>, for anything to draw.
     * @param leftPossibleEdge The left edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param topEdge The top edge of the text, in OpenGL coordinates
     * @param rightPossibleEdge The right edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param bottomEdge The bottom edge of the text, in OpenGL coordinates
     * @param text The text to render
     * @return The truncated section of the input text, if any.  Empty string if the text was not truncated.
     * @since GUI module 3.3.0.0
     */
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
    /**
     * Draws text at the specified location and trims it to fit within
     * the specified coordinates.  The text will look like it slides behind the
     * rest of the screen.  The text may not occupy the full desired rectangle.
     * The text will be scaled to match the vertical size of the specified
     * rectangle, then truncated (if necessary) to fit within the horizontal
     * specifications.  The text will be centered horizontally in the desired
     * rectangle.  No truncation marks are used, but the truncated section is
     * returned.  The minX/minY/maxX/maxY specifications do not affect the text
     * scaling or truncation but merely trim the visible parts of the text.
     * NOTE:  The top and bottom edge coordinates are multiplied by -1 to get
     * the render location.  <code>bottomEdge</code> cannot be less than <code>
     * topEdge</code>, for anything to draw.
     * @param leftPossibleEdge The left edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param topEdge The top edge of the text, in OpenGL coordinates
     * @param rightPossibleEdge The right edge of the rectangle the text is to render inside, in OpenGL coordinates
     * @param bottomEdge The bottom edge of the text, in OpenGL coordinates
     * @param minX The minimum X-coordinate to be rendered on, in OpenGL coordinates
     * @param minY The minimum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param maxX The maximum X-coordinate to be rendered on, in OpenGL coordinates
     * @param maxY The maximum Y-coordinate to be rendered on, in OpenGL coordinates
     * @param text The text to render
     * @return The truncated section of the input text, if any.  Empty string if the text was not truncated.
     * @since GUI module 3.3.0.0
     */
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
     * @param texLocation The rectangle of the texture to draw (Left, top, right, down)
     * @since GUI module 3.3.0.0
     */
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
     * @param texLeft The left edge of the rectangle to draw from the texture
     * @param texTop The top edge of the rectangle to draw from the texture
     * @param texRight The right edge of the rectangle to draw from the texture
     * @param texDown The bottom edge of the rectangle to draw from the texture
     * @since GUI module 3.3.0.0
     */
    public static void drawRect(double left, double top, double right, double bottom, int texture, double texLeft, double texTop, double texRight, double texDown){
        top*=-1;
        bottom*=-1;
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
     * @param texLocation The rectangle of the texture to draw (Left, top, right, down)
     * @since GUI module 3.3.0.0
     */
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
     * @param texLeft The left edge of the rectangle to draw from the texture
     * @param texTop The top edge of the rectangle to draw from the texture
     * @param texRight The right edge of the rectangle to draw from the texture
     * @param texBottom The bottom edge of the rectangle to draw from the texture
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
        top*=-1;
        bottom*=-1;
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
}
