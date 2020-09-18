package simplelibrary.openal.decoding.mp3;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.Obuffer;
import simplelibrary.CircularStream;
import simplelibrary.openal.decoding.DecodedAudioInputStream;
public class JLayerInputStream extends DecodedAudioInputStream{
    private Obuffer output;
    private Decoder decoder;
    private Bitstream stream;
    private CircularStream circle = new CircularStream(Obuffer.OBUFFERSIZE*20);
    private CircularStream.CircularStreamInput back = circle.getInput();
    private Thread readThread;
    private boolean readComplete;
    public JLayerInputStream(BufferedInputStream in) throws IOException{
        decoder = new Decoder();
        stream = new Bitstream(in);
        readNextFrame();
        if(output!=null){
            readThread=new Thread(){
                public void run(){
                    try{
                        while(readNextFrame());
                    }catch(IOException ex){
                    }finally{
                        readComplete = true;
                    }
                }
            };
            readThread.setName("MP3 Decoder Thread");
            readThread.start();
        }
    }
    private boolean readNextFrame() throws IOException{
        try{
            Header header = stream.readFrame();//Read frame header
            if (header==null) return false;//No header?  Probably EOF.  Stop reading.
            if (output==null){//No output set- read header data & set the output.
                // REVIEW: Incorrect functionality.
                // the decoder should provide decoded
                // frequency and channels output as it may differ from
                // the source (e.g. when downmixing stereo to mono.)
                int channels = (header.mode()==Header.SINGLE_CHANNEL) ? 1 : 2;
                int freq = header.frequency();
                output = new JLayerOBuffer(this, channels, freq);
                decoder.setOutputBuffer(output);
            }
            Obuffer decoderOutput = decoder.decodeFrame(header, stream);
            // REVIEW: the way the output buffer is set
            // on the decoder is a bit dodgy. Even though
            // this exception should never happen, we test to be sure.
            if (decoderOutput!=output)
                throw new InternalError("Output buffers are different.");
            stream.closeFrame();
            return true;
        }catch(BitstreamException ex){
            return false;//Error occured, stop
        }catch(DecoderException ex){
            return false;//Error occured, stop
        }
    }
    private int channelCount, sampleRate;
    public boolean isValid(){
        return output!=null;
    }
    @Override
    public int getChannelCount(){
        return channelCount;
    }
    void setChannelCount(int count){
        channelCount = count;
    }
    @Override
    public int getSampleSize(){
        return channelCount*8;
    }
    void setFrequency(int freq){
        sampleRate = freq;
    }
    @Override
    public int getSampleRate(){
        return sampleRate;
    }
    @Override
    public int read() throws IOException{
        while(back.available()==0&&!readComplete){//Wait for data to be available
            synchronized(""){
                try{
                    "".wait(1);
                }catch(InterruptedException ex){}
            }
        }
        if(back.available()==0) return -1;
        return back.read();
    }
    @Override
    public int read(byte[] b, int off, int len) throws IOException{
        int read = 0;
        while(back.available()==0&&!readComplete){
            synchronized(""){
                try{
                    "".wait(1);
                }catch(InterruptedException ex){}
            }
        }
        if(back.available()==0) return -1;
        return back.read(b, off, Math.min(len, back.available()));
    }
    void newData(byte[] buffer, short len){
        circle.write(buffer, 0, len);
    }
    @Override
    public void close() throws IOException{
        try{
            stream.close();
            back.close();
        }catch(BitstreamException ex){
            throw new IOException(ex);
        }
    }
}
