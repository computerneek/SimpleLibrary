package simplelibrary.game;
import simplelibrary.Sys;
import simplelibrary.opengl.ImageStash;
public class Framebuffer {
    public final GameHelper helper;
    public final String name;
    public final int width;
    public final int height;
    public final ImageStash stash;
    public Framebuffer(GameHelper helper, String name, int width, int height){
        this(helper, ImageStash.instance, name, width, height);
    }
    public Framebuffer(GameHelper helper, ImageStash stash, String name, int width, int height){
        if(name==null) name = Sys.generateRandomString(20);
        this.helper = helper;
        this.name = name;
        this.width = width;
        this.height = height;
        this.stash = stash;
        if(stash.hasBuffer(name)) throw new IllegalStateException("Framebuffer \""+name+"\" already exists in ImageStash!");
        stash.configureBuffer(stash.getBuffer(name), width, height);//Configured.
    }
    public void bindTexture(){
        stash.bindTexture(getTexture());
    }
    public void bindRenderTarget2D(){
        bindRenderTarget(GameHelper.MODE_2D, 1);
    }
    public void bindRenderTargetCentered(){
        bindRenderTarget(GameHelper.MODE_2D_CENTERED, 1);
    }
    public void bindRenderTarget3D(){
        bindRenderTarget(GameHelper.MODE_3D, 1);
    }
    public void bindRenderTarget(int mode, float guiScale){
        helper.renderTargetFramebuffer(stash.getBuffer(name), mode, width, height, guiScale);
    }
    public void releaseRenderTarget(){
        helper.renderTargetFramebuffer(0, 0, 0, 0, 0);//values of last four params are ignored.
    }
    @Override
    protected void finalize() throws Throwable {
        stash.deleteBuffer(stash.getBuffer(name));
        super.finalize();
    }
    public int getTexture(){
        return stash.getTextureForBuffer(stash.getBuffer(name));
    }
}
