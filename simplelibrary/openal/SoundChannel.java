package simplelibrary.openal;
import org.lwjgl.openal.AL10;
public class SoundChannel{
    private final SoundSystem sys;
    private final String name;
    private final int src;//Our OpenAL source
    private float volume = 1;
    private int fadeProgress = -1;
    private int fadeSteps = 60;
    private State state;
    Autoplayer autoplay;
    private boolean skipOnFadeComplete;
    String lastSound;
    Song lastSong;
    private long nanoPlayTime;
    private long nanoPauseTime;
    private String nextSound;
    private Song nextSong;
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
            if(fadeProgress>=0&&!skipOnFadeComplete){//Music is fading out; song has ended.  Prevent the next song from starting.
                //However, do NOT cancel autoplay- and we must allow for the possibility of a fadeTo()!
                //To do this, we:
                //If in autoplay, run the autoplay and immediately pause and reset fade status.  Channel will be paused at the beginning of the next track.
                //IF the autoplayer pops a null, we have no choice but to disable autoplay to prevent continued play.
                //If in fadeTo(), play the next song.
                fadeProgress = -1;//Stop the fade
                if(nextSound!=null){
                    doPlay(nextSound, false, autoplay);//We're in fadeTo(); play next song without cancelling autoplay.
                    nextSound = null;//Clear next song.
                }else if(nextSong!=null){
                    doPlay(nextSong, false, autoplay);//We're in fadeTo(); play next song without cancelling autoplay.
                    nextSong = null;//Clear next song.
                }else if(autoplay!=null){
                    tryAutoplay();//We're in standard autoplay, fading out.  Skip to next song.
                    if(AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE)==AL10.AL_PLAYING) pause();//If next song started successfully, pause the music.
                    else autoplay = null;//Autoplay has one chance.  No value came back, cancel autoplay.
                }
            }
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
                    }else if(nextSound!=null){
                        doPlay(nextSound, false, autoplay);
                        nextSound = null;
                    }else if(nextSong!=null){
                        doPlay(nextSong, false, autoplay);
                        nextSong = null;
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
        if(AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE)==AL10.AL_PLAYING) return true;//Already playing, nothing to do
        AL10.alSourcePlay(src);
        boolean val = AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE)==AL10.AL_PLAYING;
        if(val) nanoPlayTime+=System.nanoTime()-nanoPauseTime;//For accurate playhead positioning
        if(val) state = State.PLAYING;
        return val;
    }
    public synchronized boolean pause(){
        if(AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE)==AL10.AL_PAUSED) return true;//Already paused, nothing to do
        AL10.alSourcePause(src);
        boolean val = AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE)==AL10.AL_PAUSED;
        if(val) nanoPauseTime = System.nanoTime();//Remember when we pause, for accurate playhead positioning
        if(val) state = State.PAUSED;
        return val;
    }
    /**
     * Stops the music, also cancels autoplay
     */
    public synchronized void stop(){
        AL10.alSourceStop(src);
        autoplay = null;
        fadeProgress = -1;
        nextSound = null;
        nextSong = null;
        state = State.STOPPED;
    }
    void dequeue(){
        AL10.alSourceStop(src);
        state = State.STOPPED;
        try{
            AL10.alSourceUnqueueBuffers(src);
            AL10.alSourcei(src, AL10.AL_BUFFER, 0);
        }catch(Exception ex){}
    }
    /**
     * Fades the music.  Channel will be PAUSED when fade is complete.
     */
    public synchronized void fade(int sixtiethsOfASecond){
        fadeProgress = Math.max(0, fadeProgress);
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
    public synchronized boolean play(Song sound){
        return play(sound, false);
    }
    public synchronized boolean play(String sound, boolean loop){
        return doPlay(sound, loop, null);
    }
    public synchronized boolean play(Song sound, boolean loop){
        return doPlay(sound, loop, null);
    }
    synchronized boolean doPlay(String sound, boolean loop, Autoplayer auto){
        int buffer = sys.getSound(sound);
        if(buffer==0) return false;//Error loading the sound
        stop();
        setLooping(loop);
        autoplay = auto;
        try{
            AL10.alSourceUnqueueBuffers(src);
            AL10.alSourcei(src, AL10.AL_BUFFER, 0);
        }catch(Exception ex){}
        AL10.alSourceQueueBuffers(src, buffer);
        AL10.alSourcef(src, AL10.AL_GAIN, volume*sys.getMasterVolume());
        lastSound = sound;
        lastSong = null;
        nanoPlayTime = nanoPauseTime = System.nanoTime();
        return play();
    }
    synchronized boolean doPlay(Song sound, boolean loop, Autoplayer auto){
        setLooping(loop);
        autoplay = auto;
        sound.addPlayer(this);//This will take care of the rest
        play();
        return true;
//        return play();
    }
    public synchronized boolean loop(String sound){
        return play(sound, true);
    }
    public synchronized boolean loop(Song sound){
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
        Object sound = autoplay.next();
        if(sound==null) return;//Block null; if we allow a null filepath to propogate down to SoundSystem.getSound(), an error will be thrown.
        setVolume(autoplay.getVolume());
        if(sound instanceof String) doPlay((String)sound, false, autoplay);
        else doPlay((Song)sound, false, autoplay);
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
        nextSound = null;
        nextSong = null;
    }
    /**
     * Fades the music.  When fade is complete, SoundChannel will behave as if <code>play(String)</code> were called, except that autoplay status will NOT be cleared.
     */
    public void fadeTo(int sixtiethsOfASecond, String nextSong){
        fade(sixtiethsOfASecond);
        this.nextSound = nextSong;
    }
    public void fadeTo(int sixtiethsOfASecond, Song nextSong){
        fade(sixtiethsOfASecond);
        this.nextSong = nextSong;
    }
    public int getLastSoundLength(){
        if(lastSound!=null) return SoundStash.getMillisecondDuration(lastSound);
        if(lastSong!=null) return lastSong.getTotalLength();
        return -1;
    }
    public int getPlayheadPosition(){
        int state = AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE);
        if(state==AL10.AL_PLAYING) return (int)((System.nanoTime()-nanoPlayTime)/1000000);
        if(state==AL10.AL_PAUSED) return (int)((nanoPauseTime-nanoPlayTime)/1000000);
        return -1;
    }
    public String getCurrentSound(){
        if(getPlayheadPosition()>=0) return lastSound==null?lastSong.getPath():lastSound;
        else return null;
    }
    public String getName(){
        return name;
    }
}
