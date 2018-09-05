package simplelibrary.openal.decoding.mp3;
import java.io.BufferedInputStream;
import java.io.IOException;
import simplelibrary.openal.decoding.AudioDecoder;
import simplelibrary.openal.decoding.DecodedAudioInputStream;
public class JLayerAudioDecoder implements AudioDecoder{
    /**
     * Gets an input stream with this decoding.
     * IF THE STREAM DOES NOT MATCH THIS DECODER, the input stream MUST BE RESET TO ITS ORIGINAL POSITION and this method should return null.
     * @param stream The stream to read encoded audio data from
     * @return The stream from which decoded audio data will be read from (or null, if invalid).
     */
    @Override
    public DecodedAudioInputStream getInputStream(BufferedInputStream stream){
        stream.mark(-1);
        try{
            BufferedInputStream s = new BufferedInputStream(stream);//Layer the buffered stream so we can't fail
            JLayerInputStream in = new JLayerInputStream(s);
            if(!in.isValid()){
                throw new IOException();//Trigger the catch block to reset the stream & return null
            }
            return in;
        }catch(IOException ex){
            try{
                stream.reset();
            }catch(IOException ex2){}
            return null;
        }
    }
}
