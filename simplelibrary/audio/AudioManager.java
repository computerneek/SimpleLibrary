package simplelibrary.audio;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import simplelibrary.Sys;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
/**
 * Loads and plays sounds with standard Java syntax.
 * Each clip created is a thread and must be stopped after playback is complete.
 * @author Bryan Dolan
 * @version 1.0.0.1
 */
public class AudioManager{
    private static final HashMap<String, String> sounds = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(AudioManager.class.getName());
    /**
     * Gets the sound that has been bound by the specified name
     * @param name The name of the sound to be played
     * @return the <code>Clip</code> object that is used to create the sound
     */
    public static Clip getSound(String name){
        if(sounds.containsKey(name)){
            try{
                String path = sounds.get(name);
                InputStream in;
                if(path.startsWith("/")){
                    in = AudioManager.class.getResourceAsStream(path);
                }else{
                    in = new FileInputStream(new File(path));
                    in = new BufferedInputStream(in);
                }
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(in);
                clip.open(inputStream);
                return clip;
            }catch(LineUnavailableException | IOException | UnsupportedAudioFileException ex){
                Sys.error(ErrorLevel.moderate, "Could not load sound at "+sounds.get(name)+"!", ex, ErrorCategory.audio);
            }
        }else{
            Sys.error(ErrorLevel.warning, "Could not find sound "+name+"!", new IllegalArgumentException("Sound Not Found!"), ErrorCategory.audio);
        }
        return null;
    }
    /**
     * Plays the sound that has been bound to the specified name
     *
     * Note:  This method is functionally equivalent to <code>getSound(name).start()</code>!
     * @param name the name of the sound to be played
     * @return the <code>Clip</code> object that is used to create the sound, playing
     */
    public static Clip playSound(String name){
        Clip clip = getSound(name);
        if(clip!=null){
            clip.start();
        }
        return clip;
    }
    /**
     * Binds the sound found at <code>path</code> to the specified name
     * @param name the name to be bound to the sound
     * @param path the path to the sound.  For sounds inside the jarfile, the path MUST start with a '/'
     */
    public static void addSound(String name, String path){
        try{
            if(path.startsWith("/")){
                InputStream s = AudioManager.class.getResourceAsStream(path);
                if(s==null){
                    Sys.error(ErrorLevel.warning, "Could not find sound file "+path+"!", null, ErrorCategory.fileIO);
                    return;
                }else{
                    s.close();
                }
            }else if(!path.startsWith("/")&&!new File(path).exists()){
                Sys.error(ErrorLevel.warning, "Could not find sound file "+path+"!", null, ErrorCategory.fileIO);
                return;
            }
        }catch(Exception ex){
            Sys.error(ErrorLevel.moderate, "Could not find sound file "+path+"!", ex, ErrorCategory.fileIO);
            return;
        }
        sounds.put(name, path);
    }
    private AudioManager(){}
}
