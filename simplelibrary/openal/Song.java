package simplelibrary.openal;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.lwjgl.openal.AL10;
import simplelibrary.openal.decoding.AudioDecoder;
import simplelibrary.openal.decoding.DecodedAudioInputStream;
import simplelibrary.texture.TexturePackManager;
public class Song{
    private int overallSongLength = -1;//In buffers
    private int totalSongLength = -1;//In milliseconds
    private final ArrayList<String> buffers = new ArrayList<>();
    private final HashSet<SoundChannel> channels = new HashSet<>();
    private final String path;
    private final int channelCount;
    private final int sampleSize;
    private final int sampleRate;
    private int autoUnload = 2;//0=disabled, 1=enabled, 2=default.  Binary AND with default read being 1 for no and 3 for yes.
    private InputStream in;
    private final SoundSystem ss;
    private int autoUnloadTick;
    Song(String path, SoundSystem ss) throws IOException{
        this.path = path;
        this.ss = ss;
        try(InputStream in = getSoundInputStream(path)){
            if(in instanceof AudioInputStream){
                AudioFormat f = ((AudioInputStream)in).getFormat();
                channelCount = f.getChannels();
                sampleSize = f.getSampleSizeInBits();
                sampleRate = (int)f.getFrameRate();
            }else{
                DecodedAudioInputStream din = (DecodedAudioInputStream)in;
                channelCount = din.getChannelCount();
                sampleSize = din.getSampleSize();
                sampleRate = din.getSampleRate();
            }
            byte[] data = new byte[sampleRate*5*(sampleSize/8)*channelCount];//Pull 5 seconds of data for starter piece
            int pos = 0;
            while(pos<data.length){
                int val = in.read(data, pos, data.length-pos);
                if(val<1) throw new IllegalArgumentException("Songs must be over 5 seconds long!");
                pos+=val;
            }
            SoundStash.allocateNew(data, path, channelCount, sampleSize, sampleRate);
            buffers.add(path);
        }
    }
    private static InputStream getSoundInputStream(String filepath) throws IOException{
        BufferedInputStream in = new BufferedInputStream(TexturePackManager.instance.currentTexturePack.getResourceAsStream(filepath));
        in.mark(0);
        try{
            return AudioSystem.getAudioInputStream(in);
        }catch(Exception ex){
            if(ex instanceof UnsupportedAudioFileException){
            }else{
                in.close();
                if(ex instanceof IOException) throw (IOException)ex;
                else if(ex instanceof RuntimeException) throw (RuntimeException)ex;
            }
        }
        for(AudioDecoder d : SoundSystem.decoders){
            DecodedAudioInputStream din = d.getInputStream(in);
            if(din!=null) return din;
        }
        in.close();
        return null;
    }
    void onDestroy(){
        buffers.clear();
        channels.clear();
        if(in==null) return;
        try{
            in.close();
        }catch(IOException ex){}
    }
    synchronized void update(){
        if(channels.isEmpty()){
            if(buffers.size()>1){
                boolean autoUnload = (this.autoUnload&(ss.getAutoUnloadDefault()?3:1))>0;
                if(autoUnload){
                    autoUnloadTick++;
                    if(autoUnloadTick>60){
                        if(autoUnload){
                            doUnload();
                        }
                    }
                }
            }
            return;//No channels, no update.  Except autounload.
        }
        autoUnloadTick = 0;
        boolean loadNext = false;
        for(Iterator<SoundChannel> it = channels.iterator(); it.hasNext(); ){
            SoundChannel c = it.next();
            if(c.isStopped()){ c.dequeue(); it.remove(); continue; }
            String current = c.getCurrentSound();
            if(current==null){ c.dequeue(); it.remove(); continue; }
            if(!path.equals(current)){ dequeue(c); it.remove(); continue; }
            if(c.getPlayheadPosition()>=getLoadedSongLength()-10_000) loadNext = true;//Within 10 seconds of the end of the piece, load the next section
        }
        if(loadNext){
            if(overallSongLength>0&&buffers.size()>=overallSongLength) return;//Song is fully loaded, can't load more
            try{
                loadNextSegment();//Load next segment
            }catch(IOException ex){}//Or fail, I suppose.  But do it silently.
        }
    }
    private void doUnload(){
        try{
            in.close();
            in = null;
        }catch(IOException|NullPointerException ex){}
        //Unload the song, but leave the first sample
        while(buffers.size()>1) SoundStash.removeBuffer(buffers.remove(buffers.size()-1));
    }
    private void doCompleteUnload(){
        try{
            in.close();
            in = null;
        }catch(IOException|NullPointerException ex){}
        //Unload the song, including the first sample
        buffers.stream().forEach((e)->SoundStash.removeBuffer(e));
        buffers.clear();
    }
    private int getLoadedSongLength(){
        int size = buffers.size();
        if(size==0) return 0;
        if(size==1) return 5000;//5s
        if(size==2) return 30000;//30s
        return (size-2)*60000+30000;//30s+60s/seg
    }
    private synchronized void loadNextSegment() throws IOException{
        if(in==null){
            in=getSoundInputStream(path);
            int length = getLoadedSongLength()/1000;//Amount of time we need to skip
            byte[] data = new byte[sampleRate*5*(sampleSize/8)*channelCount];//5 seconds at a time
            int toSkip = (data.length/5)*length;
            while(toSkip>0){
                int val = in.read(data, 0, Math.min(toSkip, data.length));
                if(val<1){
                    in.close();
                    overallSongLength = buffers.size();
                    computeTotalSongLength();
                    in = null;
                }
                toSkip-=val;
            }
        }
        //Read segment
        byte[] data = new byte[sampleRate*(sampleSize/8)*channelCount*(buffers.size()==1?25:60)];//Read 25s for second sample, 60s for the rest
        int pos = 0;
        while(pos<data.length){
            int val = in.read(data, pos, data.length-pos);
            if(val<1) break;
            pos+=val;
        }
        if(pos==0){
            //EOF already occured
            in.close();
            in = null;
            overallSongLength = buffers.size();
            computeTotalSongLength();
            return;
        }
        if(pos<data.length){
            //EOF was encountered
            byte[] newdata = new byte[pos];
            System.arraycopy(data, 0, newdata, 0, newdata.length);
            data = newdata;
            //Now, the only reason this would have happened is EOF, so...
            in.close();
            in = null;
            overallSongLength = buffers.size()+1;
        }
        int buff = SoundStash.allocateNew(data, path+"_"+buffers.size(), channelCount, sampleSize, sampleRate);
        buffers.add(path+"_"+buffers.size());
        if(totalSongLength<0&&overallSongLength>0) computeTotalSongLength();
        //Segment loaded.  Now...
        channels.stream().filter((e)->!e.isStopped()).filter((e)->e.getCurrentSound()!=null&&e.getCurrentSound().equals(path))
                .forEach((e)->AL10.alSourceQueueBuffers(SoundStash.getSource(e.getName()), buff));
        //We append the newly-loaded segment to all channels playing this song!
    }
    private void computeTotalSongLength(){
        totalSongLength = 0;
        for(String b : buffers){
            totalSongLength+=SoundStash.getMillisecondDuration(b);
        }
    }
    private synchronized void loadFirstSegment() throws IOException{
        if(!buffers.isEmpty()) return;//First segment already loaded
        try(InputStream in = getSoundInputStream(path)){
            byte[] data = new byte[sampleRate*5*(sampleSize/8)*channelCount];//Pull 5 seconds of data for starter piece
            int pos = 0;
            while(pos<data.length){
                int val = in.read(data, pos, data.length-pos);
                if(val<1) throw new IllegalArgumentException("Songs must be over 5 seconds long!");
                pos+=val;
            }
            SoundStash.allocateNew(data, path, channelCount, sampleSize, sampleRate);
            buffers.add(path);
        }
    }
    public void setAutoUnload(boolean autoUnload){
        this.autoUnload = autoUnload?1:0;
    }
    public void resetAutoUnload(){
        this.autoUnload = 2;
    }
    synchronized void addPlayer(SoundChannel channel){
        try{
            loadFirstSegment();//First, make sure our first segment is loaded
        }catch(IOException ex){ return; }//If we can't, do nothing (fail silently)
        channel.doPlay(path, channel.isLooping(), channel.autoplay);
        channel.lastSong = this;
        channel.lastSound = null;
        channels.add(channel);
        int src = SoundStash.getSource(channel.getName());
        for(int i = 1; i<buffers.size(); i++){
            AL10.alSourceQueueBuffers(src, SoundStash.getBuffer(buffers.get(i)));
        }
    }
    private void dequeue(SoundChannel c){
        int src = SoundStash.getSource(c.getName());
        int buff = AL10.alGetSourcei(src, AL10.AL_BUFFER);
        if(buffers.stream().map((e)->SoundStash.getBuffer(e)).anyMatch((e)->e==buff)){
            AL10.alSourcei(src, AL10.AL_BUFFER, 0);
        }
    }
    /**
     * Unloads the song from memory.  The first 5 seconds remain loaded to allow instant playback.
     * @throws IllegalStateException if any SoundChannel is using the song.
     */
    public synchronized void unload(){
        if(channels.stream().filter((e)->e.isPlaying()||e.isPaused()).anyMatch((e)->e.getCurrentSound()!=null&&e.getCurrentSound().equals(path))){
            throw new IllegalStateException("Song is in use!");
        }
        doUnload();
    }
    /**
     * Unloads the song from memory.  No data is retained; instant playback will be impossible.
     * @throws IllegalStateException if any SoundChannel is using the song.
     */
    public synchronized void completeUnload(){
        if(channels.stream().filter((e)->e.isPlaying()||e.isPaused()).anyMatch((e)->e.getCurrentSound()!=null&&e.getCurrentSound().equals(path))){
            throw new IllegalStateException("Song is in use!");
        }
        doCompleteUnload();
    }
    /**
     * Push-unloads the song from memory.  The first 5 seconds remain loaded to allow instant playback.
     * @throws IllegalStateException if any SoundChannel is actively playing the song (paused channels will be stopped)
     */
    public synchronized void pushUnload(){
        if(channels.stream().filter((e)->e.isPlaying()).anyMatch((e)->e.getCurrentSound()!=null&&e.getCurrentSound().equals(path))){
            throw new IllegalStateException("Song is in use!");
        }
        forceUnload();
    }
    /**
     * Push-unloads the song from memory.  No data is retained; instant playback will be impossible.
     * @throws IllegalStateException if any SoundChannel is actively playing the song (paused channels will be stopped)
     */
    public synchronized void pushCompleteUnload(){
        if(channels.stream().filter((e)->e.isPlaying()).anyMatch((e)->e.getCurrentSound()!=null&&e.getCurrentSound().equals(path))){
            throw new IllegalStateException("Song is in use!");
        }
        forceCompleteUnload();
    }
    /**
     * Force-unloads the song from memory.  The first 5 seconds remain loaded to allow instant playback.
     * Does not throw any exceptions.  Any SoundChannel actively playing the song will be <code>stop()</code>ed.
     */
    public synchronized void forceUnload(){
        channels.stream().filter((e)->e.getCurrentSound()==null||e.getCurrentSound().equals(path))
                .forEach((e)->e.dequeue());//Force the song inactive
        doUnload();
    }
    /**
     * Force-unloads the song from memory.  No data is retained; instant playback will be impossible.
     * Does not throw any exceptions.  Any SoundChannel actively playing the song will be <code>stop()</code>ed.
     */
    public synchronized void forceCompleteUnload(){
        channels.stream().filter((e)->e.getCurrentSound()==null||e.getCurrentSound().equals(path))
                .forEach((e)->e.dequeue());//Force the song inactive
        doCompleteUnload();
    }
    /**
     * Forces the track to load completely.  If autounload is on, the track may instantly unload itself.
     * WARNING:  Operation may take a few seconds.
     */
    public synchronized void loadCompletely(){
        try{
            loadFirstSegment();
            while(overallSongLength<0||buffers.size()<overallSongLength) loadNextSegment();
        }catch(IOException ex){}
        if(channels.isEmpty()&&(this.autoUnload&(ss.getAutoUnloadDefault()?3:1))>0) doUnload();
    }
    /**
     * Gets the total song length on a best-effort basis.
     * If the song has not been loaded fully, this function will return an inaccurate value.
     * Once the song is loaded fully, even if it is subsequently unloaded, this function will still return an accurate value.
     * To tell which it will return, see <code>wasLoadedCompletely()</code>
     */
    public int getTotalLength(){
        return totalSongLength<0?getLoadedSongLength():totalSongLength;
    }
    /**
     * Tells if this song was, at any given point, loaded completely.
     * This can be used to tell whether or not getTotalLength() will return an accurate value.
     */
    public boolean wasLoadedCompletely(){
        return overallSongLength>0;
    }
    public String getPath(){
        return path;
    }
}
