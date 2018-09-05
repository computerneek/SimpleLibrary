package simplelibrary.encryption;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
public abstract class DecryptionInputStream extends FilterInputStream{
    public DecryptionInputStream(InputStream in) {
        super(in);
    }
    @Override
    public abstract int read() throws IOException;
    public void destroy(){
        in = null;
    }
}
