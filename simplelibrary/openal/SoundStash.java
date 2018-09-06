package simplelibrary.openal;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.lwjgl.openal.Util;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
import simplelibrary.texture.TexturePack;
import simplelibrary.texture.TexturePackManager;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO8;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO8;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
public class SoundStash{
    private static final HashMap<String, Integer> sounds = new HashMap<>();
    private static final HashMap<String, Integer> sources = new HashMap<>();
    public static String lastError;
    public static Exception lastException;
    private static ByteBuffer soundData = createDirectByteBuffer(67_108_864);
    private static int dataLength = 67_108_864;
    private static boolean expanding;
    public static ByteBuffer createDirectByteBuffer(int bufferSize){
        return ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
    }
    public static void expandAudioBuffer(int size){
        if(size<=dataLength) return;
        dataLength*=2;
        if(size>dataLength) dataLength = size;
        soundData = createDirectByteBuffer(dataLength);
    }
    public static boolean hasBuffer(String filepath){
        if(filepath==null||filepath.isEmpty()) return false;
        return sounds.containsKey(filepath);
    }
    public static int getBuffer(String filepath){
        if(filepath==null||filepath.isEmpty()){
            return 0;
        }
        TexturePack texture = TexturePackManager.instance.currentTexturePack;
        Integer bufferIndex = sounds.get(filepath);
        if (bufferIndex != null){
            return bufferIndex;
        }else{
            try{
                InputStream in = texture.getResourceAsStream(filepath);
                if(in==null){
                    lastError = "Could not find sound file "+filepath+"!";
                    lastException = null;
                    return 0;
                }else{
                    in = new BufferedInputStream(in);
                    return allocateNew(AudioSystem.getAudioInputStream(in), filepath);
                }
            }catch (Exception ex){
                lastError = "Could not load sound file "+filepath;
                lastException = ex;
                return 0;
            }
        }
    }
    public static void removeBuffer(String filepath){
        if(!sounds.containsKey(filepath)){
            return;
        }
        alDeleteBuffers(sounds.remove(filepath));
    }
    public static void clearBuffers(){
        for(Integer val : sounds.values()){
            alDeleteBuffers(val);
        }
        sounds.clear();
    }
    public static int getSource(String name){
        if(sources.containsKey(name)){
            return sources.get(name);
        }else{
            int source = alGenSources();
            sources.put(name, source);
            return source;
        }
    }
    public static void removeSource(String name){
        if(!sources.containsKey(name)){
            return;
        }
        alDeleteSources(sources.remove(name));
    }
    public static void clearSources(){
        for(Integer val : sources.values()){
            alDeleteSources(val);
        }
        sources.clear();
    }
    private SoundStash() {
    }
    private static int allocateNew(AudioInputStream in, String filepath) throws IOException{
        float sampleRate = in.getFormat().getSampleRate();
        int channels = in.getFormat().getChannels();
        int sampleSize = in.getFormat().getSampleSizeInBits();
        return allocateNew(in, filepath, channels, sampleSize, sampleRate);
    }
    /**
     * External use is intended for use with encodings not supported by the base Java API (ex. MP3).
     * Also used internally by getBuffer() for those that are supported by the aforementioned API (ex. WAV).
     * @param in An InputStream offering decompressed/decoded sound data.  Stream will be read in its entirety, and closed.
     * @param filepath The filepath by which this sound can be looked up later (through getBuffer())
     * @param channels How many channels are present in the data (Supported:  1 (mono) and 2 (stereo))
     * @param sampleSize The sample size in the data, in bits (Supported:  8 and 16)
     * @param sampleRate The sample rate in the data
     * @return The buffer 'name'; same as later returned by getBuffer(filepath).  IF RETURN IS 0, an error occured; check lastError variable.
     */
    public static int allocateNew(InputStream in, String filepath, int channels, int sampleSize, float sampleRate) throws IOException{
        int alFormat;
        try (InputStream ain = in) {
            if(channels==1&&sampleSize==8){
                alFormat = AL_FORMAT_MONO8;
            }else if(channels==1&&sampleSize==16){
                alFormat = AL_FORMAT_MONO16;
            }else if(channels==2&&sampleSize==8){
                alFormat = AL_FORMAT_STEREO8;
            }else if(channels==2&&sampleSize==16){
                alFormat = AL_FORMAT_STEREO16;
            }else{
                ain.close();
                lastError = "Invalid format of "+filepath+"- only supports mono & stereo, in 8 or 16 bits!";
                lastException = null;
                return 0;
            }
            soundData.clear();
            byte[] data = new byte[1_024];
            int read;
            int size = 0;
            while((read = ain.read(data))!=-1){
                soundData.put(data, 0, read);
                size+=read;
                if(soundData.remaining()<1_024){
                    Sys.error(ErrorLevel.warning, "File too large, expanding audio buffer...", null, ErrorCategory.audio);
                    expanding = true;
                    ByteBuffer orig = soundData;
                    expandAudioBuffer(dataLength*2);
                    orig.flip();
                    soundData.put(orig);
                    Sys.error(ErrorLevel.warning, "Audio buffer expansion complete.", null, ErrorCategory.audio);
                }
            }
            if(expanding){
                Sys.error(ErrorLevel.warning, "Read complete; data size "+(dataLength-soundData.remaining())+"B", null, ErrorCategory.audio);
                expanding = false;
            }
        }
        soundData.flip();
        int name = alGenBuffers();
        alBufferData(name, alFormat, soundData, (int)sampleRate);
        Util.checkALError();
        sounds.put(filepath, name);
        return name;
    }
}
