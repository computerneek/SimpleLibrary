package simplelibrary.openal.decoding.mp3;
import javazoom.jl.decoder.Obuffer;
public class JLayerOBuffer extends Obuffer{
    private byte[] buffer;
    private short[] bufferPos;
    private int channels;
    private final JLayerInputStream stream;
    /**
     * Creates a new WareFileObuffer instance. 
     * 
     * @param number_of_channels	
     *				The number of channels of audio data
     *				this buffer will receive. 
     * 
     * @param freq	The sample frequency of the samples in the buffer.
     */
    public JLayerOBuffer(JLayerInputStream stream, int number_of_channels, int freq){
        this.stream = stream;
        buffer = new byte[OBUFFERSIZE*2];
        bufferPos = new short[MAXCHANNELS];
        channels = number_of_channels;
        for (int i = 0; i < number_of_channels; ++i) 
            bufferPos[i] = (short)(i*2);
        stream.setChannelCount(number_of_channels);
        stream.setFrequency(freq);
    }
    /**
     * Takes a 16 Bit PCM sample.
     */
    public void append(int channel, short value){
        buffer[bufferPos[channel]+1] = (byte)(value>>8);//Upper half
        buffer[bufferPos[channel]] = (byte)value;//Lower half
        bufferPos[channel] += channels*2;//Progress the counter
    }
    short[] myBuffer = new short[2];
    public void write_buffer(int val){
//        System.out.println("New data:  "+bufferPos[0]+", "+bufferPos[1]);
        stream.newData(buffer, bufferPos[0]);
        for (int i = 0; i < channels; ++i) bufferPos[i] = (short)(i*2);
    }
    public void close(){}
    public void clear_buffer(){}
    public void set_stop_flag(){}
}
