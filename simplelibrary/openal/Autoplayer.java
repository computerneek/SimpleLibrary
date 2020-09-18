package simplelibrary.openal;
public interface Autoplayer{
    /**
     * Gets the next song in line for this source to play.  NULL means no song; may be called many times when returning null.
     * Note:  This method will not be called until the previous song has finished.
     * @return a String path to the song, a Song object, or null
     */
    public Object next();
    /**
     * Gets the volume that the current song should be played at, in a range from 0-1.  Outlying values will be clamped; 0 is absolute silence.
     * Note:  This song is called once, as soon as a song is selected.
     * No further calls will be made until <code>next()</code> returns another non-null value (selects the next song).
     */
    public float getVolume();
}
