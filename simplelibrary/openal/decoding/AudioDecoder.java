package simplelibrary.openal.decoding;
import java.io.BufferedInputStream;
public interface AudioDecoder{
    /**
     * Gets an input stream with this decoding.
     * IF THE STREAM DOES NOT MATCH THIS DECODER, the input stream MUST BE RESET TO ITS ORIGINAL POSITION and this method should return null.
     * @param in The stream to read encoded audio data from
     * @return The stream from which decoded audio data will be read from (or null, if invalid).
     */
    public DecodedAudioInputStream getInputStream(BufferedInputStream in);
}
