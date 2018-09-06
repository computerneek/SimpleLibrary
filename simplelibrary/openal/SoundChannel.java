package simplelibrary.openal;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.Util;
public class SoundChannel{
    private final SoundSystem sys;
    private final String name;
    private final int src;//Our OpenAL source
    private float volume = 1;
    private int fadeProgress = -1;
    private int fadeSteps = 60;
    private State state;
    private Autoplayer autoplay;
    private boolean skipOnFadeComplete;
    SoundChannel(SoundSystem sys, String channelName){
        this.sys = sys;
        this.name = channelName;
        src = SoundStash.getSource(name);
    }
    void destroy(){
        stop();
        SoundStash.removeSource(name);
    }
    synchronized void update(){
        int s = AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE);
        switch(s){
            case AL10.AL_PLAYING:
                state=State.PLAYING;
                break;
            case AL10.AL_PAUSED:
                state=State.PAUSED;
                break;
            default:
                state=State.STOPPED;
                break;
        }
        if(state==State.STOPPED){
            if(fadeProgress>=0&&!skipOnFadeComplete) autoplay = null;//Music is fading out; song has ended.  Prevent the next song from starting.
            fadeProgress = -1;
            if(autoplay!=null) tryAutoplay();
        }
        if(state==State.PLAYING){
            if(fadeProgress>=0){
                fadeProgress++;
                if(fadeProgress>=fadeSteps){
                    if(skipOnFadeComplete){
                        skipOnFadeComplete = false;
                        Autoplayer auto = autoplay;
                        stop();
                        autoplay(auto);//Don't reset playback volume- that will be auto-reset by the autoplay.
                    }else{
                        pause();
                        AL10.alSourcef(src, AL10.AL_GAIN, volume*sys.getMasterVolume());//Reset playback volume after fade
                    }
                    fadeProgress = -1;
                }else{
                    AL10.alSourcef(src, AL10.AL_GAIN, volume*sys.getMasterVolume()*(float)Math.pow(2, -6f*fadeProgress/fadeSteps));
                }
            }
        }
    }
    /**
     * Resumes a paused channel, or restarts a playing/stopped channel.
     */
    public synchronized boolean play(){
        AL10.alSourcePlay(src);
        return AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE)==AL10.AL_PLAYING;
    }
    public synchronized boolean pause(){
        AL10.alSourcePause(src);
        return AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE)==AL10.AL_PAUSED;
    }
    /**
     * Stops the music, also cancels autoplay
     */
    public synchronized void stop(){
        AL10.alSourceStop(src);
        autoplay = null;
        fadeProgress = -1;
    }
    /**
     * Fades the music.  Channel will be PAUSED when fade is complete.
     */
    public synchronized void fade(int sixtiethsOfASecond){
        fadeProgress = 0;
        fadeSteps = sixtiethsOfASecond;
    }
    public synchronized boolean isFading(){
        return fadeProgress>=0&&state!=State.STOPPED;
    }
    public synchronized boolean isPlaying(){ return state==State.PLAYING; }
    public synchronized boolean isPaused(){ return state==State.PAUSED; }
    public synchronized boolean isStopped(){ return state==State.STOPPED; }
    public synchronized boolean isLooping(){ return AL10.alGetSourcei(src, AL10.AL_LOOPING)==AL10.AL_TRUE; }
    public synchronized void setLooping(boolean loop){ AL10.alSourcei(src, AL10.AL_LOOPING, loop?AL10.AL_TRUE:AL10.AL_FALSE); }
    public synchronized boolean play(String sound){
        return play(sound, false);
    }
    public synchronized boolean play(String sound, boolean loop){
        return doPlay(sound, loop, null);
    }
    private synchronized boolean doPlay(String sound, boolean loop, Autoplayer auto){
        int buffer = sys.getSound(sound);
        if(buffer==0) return false;//Error loading the sound
        stop();
        setLooping(loop);
        autoplay = auto;
        try{
            AL10.alSourceUnqueueBuffers(src);
            Util.checkALError();
        }catch(Exception ex){}
        AL10.alSourceQueueBuffers(src, buffer);
        AL10.alSourcef(src, AL10.AL_GAIN, volume*sys.getMasterVolume());
        return play();
    }
    public synchronized boolean loop(String sound){
        return play(sound, true);
    }
    /**
     * Sets the autoplay.
     * NOTE:  This will NOT effect a playing channel until the current song completes.
     * If your channel is looping, this will NOT take effect unless setLooping(false) is called; a call to stop() clears autoplay status as well.
     * Any channel that reports a STOPPED status (song end) while autoplay is set will automatically invoke the autoplay for the next song.
     * Note that a PAUSED channel will stay paused; current song will not be changed until STOPPED status acquires through the end of the song.
     * The skip() and fadeSkip() functions can be used to force an early end to a track on an autoplaying source.
     */
    public synchronized void autoplay(Autoplayer source){
        this.autoplay = source;
    }
    /**
     * Set the volume, ranging from 0 (0%) to 1 (100%).
     */
    public synchronized void setVolume(float volume){
        this.volume = Math.max(0, Math.min(1, volume));
        AL10.alSourcef(src, AL10.AL_GAIN, volume*sys.getMasterVolume());
    }
    private void tryAutoplay(){
        String sound = autoplay.next();
        if(sound==null) return;//Block null; if we allow a null filepath to propogate down to SoundSystem.getSound(), an error will be thrown.
        setVolume(autoplay.getVolume());
        doPlay(sound, false, autoplay);
    }
    void updateMasterVolume(){
        if(state==State.PLAYING&&fadeProgress==-1){
            setVolume(volume);
        }
    }
    private enum State{PLAYING, PAUSED, STOPPED}
    /**
     * NOTE:  Does nothing if not autoplaying.
     */
    public void skip(){
        if(autoplay==null) return;//Skip only works in autoplay mode
        tryAutoplay();
    }
    /**
     * Fades the music.  Channel will be STOPPED when fade is complete, but autoplay status WILL NOT be cleared.
     * If this channel is in autoplay, the autoplayer will be invoked from this status and the next song will be started.
     */
    public void fadeSkip(int sixtiethsOfASecond){
        fade(sixtiethsOfASecond);
        skipOnFadeComplete = true;
    }
}
