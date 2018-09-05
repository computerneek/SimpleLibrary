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
            fadeProgress = -1;
            if(autoplay!=null) tryAutoplay();
        }
        if(state==State.PLAYING){
            if(fadeProgress>=0){
                fadeProgress++;
                if(fadeProgress>=fadeSteps){
                    pause();
                    fadeProgress = -1;
                    AL10.alSourcef(src, AL10.AL_GAIN, volume*sys.getMasterVolume());//Reset playback volume after fade
                }else{
                    AL10.alSourcef(src, AL10.AL_GAIN, volume*sys.getMasterVolume()*(fadeProgress/(float)fadeSteps));
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
        return play();
    }
    public synchronized boolean loop(String sound){
        return play(sound, true);
    }
    public synchronized void autoplay(Autoplayer source){
        this.autoplay = source;
    }
    /**
     * Set the volume, ranging from 0 (0%) to 1 (100%).
     */
    public synchronized void setVolume(float volume){
        this.volume = Math.min(0, Math.max(1, volume));
        AL10.alSourcef(src, AL10.AL_GAIN, volume*sys.getMasterVolume());
    }
    private void tryAutoplay(){
        String sound = autoplay.next();
        if(sound==null) return;//Block null; if we allow a null filepath to propogate down to SoundSystem.getSound(), an error will be thrown.
        setVolume(autoplay.getVolume());
        doPlay(sound, false, autoplay);
    }
    void updateMasterVolume(){
        if(state==State.PLAYING&&fadeProgress==0){
            setVolume(volume);
        }
    }
    private enum State{PLAYING, PAUSED, STOPPED}
    public void skip(){
        if(autoplay==null) return;//Skip only works in autoplay mode
        tryAutoplay();
    }
}
