package simplelibrary.encryption;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
public abstract class EncryptionOutputStream extends DataOutputStream{
    public EncryptionOutputStream(OutputStream out) {
        super(out);
    }
    @Override
    public abstract void write(int b) throws IOException;
    @Override
    public abstract void flush() throws IOException;
    public void destroy(){
        out = null;
    }
}
