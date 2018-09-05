package simplelibrary.openal.decoding;
import java.io.InputStream;
public abstract class DecodedAudioInputStream extends InputStream{
    public abstract int getChannelCount();
    public abstract int getSampleSize();
    public abstract int getSampleRate();
}
