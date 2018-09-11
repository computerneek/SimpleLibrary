package simplelibrary.openal;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.Util;
import org.lwjgl.opengl.Display;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.openal.decoding.AudioDecoder;
import simplelibrary.openal.decoding.DecodedAudioInputStream;
import simplelibrary.openal.decoding.mp3.JLayerAudioDecoder;
import simplelibrary.texture.TexturePackManager;
public class SoundSystem{
    private int sfxChannel;
    private final int sfxChannels;
    private final String sfxPrefix;
    private final String sfxSuffix;
    private final HashMap<String, SoundChannel> channels = new HashMap<>();
    private static final ArrayList<AudioDecoder> decoders = new ArrayList<>();
    private final Thread loop;
    private boolean running = true;
    private float masterVolume = 1;
    private float sfxVolume = 1;
    /**
     * Initializes the soundsystem with prefix "/" and suffix ".wav" and NO predefined main sound channels (music, etc.)
     */
    public SoundSystem(int sfxChannels) throws LWJGLException{
        this(sfxChannels, "/", ".wav");
    }
    /**
     * Initializes the soundsystem with NO predefined main sound channels (music, etc.)
     */
    public SoundSystem(int sfxChannels, String sfxPrefix, String sfxSuffix) throws LWJGLException{
        this(sfxChannels, sfxPrefix, sfxSuffix, new String[0]);
    }
    public SoundSystem(int sfxChannels, String sfxPrefix, String sfxSuffix, String... mains) throws LWJGLException{
        this.sfxChannels=sfxChannels;
        this.sfxPrefix = sfxPrefix==null?"":sfxPrefix;
        this.sfxSuffix = sfxSuffix==null?"":sfxSuffix;
        AL.create();
        for(String s : mains){
            channels.put(s, new SoundChannel(this, s));
        }
        loop = new Thread(){
            public void run(){
                while(running){
                    Display.sync(60);
                    update();
                }
            }
        };
        loop.setName("SimpLib SoundSystem Thread");
        loop.start();
    }
    /**
     * Gets a live-updating unmodifiable list of all available channels
     */
    public Set<String> getChannels(){
        return Collections.unmodifiableSet(channels.keySet());
    }
    /**
     * Gets the requested sound channel; returns NULL if not found.
     */
    public synchronized SoundChannel getChannel(String name){
        return channels.get(name);
    }
    /**
     * Creates and gets the requested sound channel
     * @throws IllegalStateException if the channel already exists
     */
    public synchronized SoundChannel makeChannel(String name){
        if(channels.containsKey(name)) throw new IllegalStateException("Channel "+name+" already exists!");
        channels.put(name, new SoundChannel(this, name));
        return getChannel(name);
    }
    /**
     * Deletes the specified sound channel.  No-op if the channel already doesn't exist.
     */
    public synchronized void deleteChannel(String name){
        if(channels.containsKey(name)) channels.remove(name).destroy();
    }
    public synchronized void playSFX(String sound){
        playSFX(sound, 1);
    }
    public synchronized void playSFX(String sound, float volume){
        sfxChannel++;
        if(sfxChannel>sfxChannels){
            sfxChannel = 1;
        }
        int src = SoundStash.getSource("SFX source "+sfxChannel);
        AL10.alSourceStop(src);
        AL10.alSourcef(src, AL10.AL_GAIN, volume*masterVolume*sfxVolume);
        try{
            AL10.alSourceUnqueueBuffers(src);
            Util.checkALError();
        }catch(Exception ex){}
        AL10.alSourceQueueBuffers(src, getSound(sfxPrefix+sound+sfxSuffix));
        AL10.alSourcePlay(src);
    }
    public int getSound(String filepath){
        if(SoundStash.hasBuffer(filepath)) return SoundStash.getBuffer(filepath);
        return getSound_do(filepath);
    }
    private synchronized int getSound_do(String filepath){
        int name = SoundStash.getBuffer(filepath);
        if(name==0){
            if(SoundStash.lastException!=null&&SoundStash.lastException instanceof UnsupportedAudioFileException){
                name = tryDecodeSound(filepath);
            }else{
                Sys.error(ErrorLevel.moderate, SoundStash.lastError, SoundStash.lastException, ErrorCategory.audio);
            }
            Util.checkALError();
            SoundStash.lastError = null;
            SoundStash.lastException = null;
        }
        return name;
    }
    private int tryDecodeSound(String filepath){
        try(BufferedInputStream in = new BufferedInputStream(TexturePackManager.instance.currentTexturePack.getResourceAsStream(filepath))){
            for(AudioDecoder d : decoders){
                DecodedAudioInputStream din = d.getInputStream(in);
                if(din==null) continue;
                return decodeSound(filepath, din);
            }
        }catch(IOException|NullPointerException ex){
            Sys.error(ErrorLevel.moderate, "Could not access required audio file!", ex, ErrorCategory.audio);
            return 0;
        }
        Sys.error(ErrorLevel.severe, "Unknown audio format in "+filepath+"!", null, ErrorCategory.audio);
        return 0;
    }
    private int decodeSound(String filepath, DecodedAudioInputStream in) throws IOException{
        Util.checkALError();
        return SoundStash.allocateNew(in, filepath, in.getChannelCount(), in.getSampleSize(), in.getSampleRate());
    }
    public synchronized boolean isPaused(String source){
        return AL10.alGetSourcei(SoundStash.getSource(source), AL10.AL_PAUSED)==AL10.AL_TRUE;
    }
    public synchronized boolean isPlaying(String source){
        return AL10.alGetSourcei(SoundStash.getSource(source), AL10.AL_PLAYING)==AL10.AL_TRUE;
    }
    public void destroy(){
        running = false;
        for(int i = 0; i<sfxChannels; i++){
            SoundStash.removeSource("SFX source "+(i+1));
        }
        for(SoundChannel c : channels.values()){
            c.destroy();
        }
        try{
            loop.join();
        }catch(InterruptedException ex){}
        AL.destroy();
    }
    public static void addDecoder(AudioDecoder decoder){
        decoders.add(decoder);
    }
    public synchronized void update(){
        for(SoundChannel c : channels.values()) c.update();
    }
    public float getMasterVolume(){
        return masterVolume;
    }
    public synchronized void setMasterVolume(float vol){
        float last = masterVolume;
        masterVolume = vol;
        for(int i = 0; i<sfxChannels; i++){
            updateSFXVolume(i+1, last, vol);
        }
        for(SoundChannel c : channels.values()){
            c.updateMasterVolume();
        }
    }
    public float getSFXVolume(){
        return sfxVolume;
    }
    public synchronized void setSFXVolume(float vol){
        float last = sfxVolume;
        sfxVolume = last;
        for(int i = 0; i<sfxChannels; i++){
            updateSFXVolume(i+1, last, vol);
        }
    }
    private void updateSFXVolume(int channel, float prevMaster, float newMaster){
        int src = SoundStash.getSource("SFX source "+channel);
        float vol = AL10.alGetSourcef(src, AL10.AL_GAIN);
        AL10.alSourcef(src, AL10.AL_GAIN, vol/prevMaster*newMaster);
    }
    static{
        if(classExists("javazoom.jl.decoder.Decoder")) addDecoder(new JLayerAudioDecoder());
    }
    private static boolean classExists(String classname){
        try{
            Class.forName(classname);
            return true;
        }catch(ClassNotFoundException ex){
            return false;
        }
    }
}