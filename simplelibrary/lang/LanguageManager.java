package simplelibrary.lang;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import simplelibrary.Sys;
import simplelibrary.config.Config;
import simplelibrary.error.ErrorCategory;
import simplelibrary.error.ErrorLevel;
/**
 * A class designed to translate generic keys into strings
 * @author Bryan Dolan
 */
public class LanguageManager{
    private static final ArrayList<Config> languages = new ArrayList<>();
    private static Config currentLanguage;
    private static final Logger LOG = Logger.getLogger(LanguageManager.class.getName());
    public static void addLanguage(File language){
        addLanguage(Config.loadConfig(language));
    };
    public static void addLanguage(InputStream language){
        addLanguage(Config.loadConfig(language));
    }
    public static void addLanguage(String language){
        Config config = Config.loadConfig(language);
        String lang = Sys.splitString(language, '/')[Sys.splitString(language, '/').length-1];
        lang = Sys.splitString(lang, '.')[0];
        config.checkProperty("LanguageInfo.name", lang);
        addLanguage(config);
    }
    public static void addLanguage(Config language){
        if(!language.hasProperty("LanguageInfo.name")&&language.getFile()==null){
            throw new IllegalArgumentException("Config must iether have the key 'LanguageInfo.name' defined in it or have a file attached!");
        }
        boolean has = language.hasProperty("LanguageInfo.name");
        if(language.getFile()!=null){
            language.checkProperty("LanguageInfo.name", Sys.splitString(language.getFile().getName(), '.')[0]);
        }
        String languageName = language.str("LanguageInfo.name");
        if(!has){
            language.save();
        }
        if(!hasLanguage(languageName)){
            languages.add(language);
        }else if(!language.isSameConfig(getLanguage(languageName))){
            Sys.error(ErrorLevel.warning, "Language "+languageName+" cannot be overwritten without first being deleted!", new IllegalArgumentException(), ErrorCategory.config);
        }
    }
    public static void removeLanguage(String name){
        languages.remove(getLanguage(name));
    }
    public static void setCurrentLanguage(Config language){
        if(language==null){
            return;
        }
        if(!languages.contains(language)&&getLanguage(language.str("LanguageInfo.name"))==null){
            languages.add(language);
        }else if(getLanguage(language.str("LanguageInfo.name"))!=null){
            language = getLanguage(language.str("LanguageInfo.name"));
        }
        currentLanguage = language;
    }
    public static void setCurrentLanguage(String name){
        setCurrentLanguage(getLanguage(name));
    }
    public static boolean hasTranslation(String key){
        return currentLanguage.hasProperty(key);
    }
    public static String tryTranslate(String key, String theDefault){
        if(!isInUse()){
            return theDefault;
        }
        String translation = translate(key);
        if(translation==null){
            return theDefault;
        }
        return translation;
    }
    private static Config getLanguage(String name){
        Config[] langs = languages.toArray(new Config[languages.size()]);
        for(Config lang : langs){
            if(lang.str("LanguageInfo.name").equals(name)){
                return lang;
            }
        }
        return null;
    }
    public static String translate(String key){
        if(currentLanguage.hasProperty(key)){
            return currentLanguage.str(key);
        }else{
            return key;
        }
    }
    public static boolean isInUse(){
        return currentLanguage!=null;
    }
    public static String[] translate(String[] keys){
        String[] value = new String[keys.length];
        for(int i = 0; i<value.length; i++){
            value[i] = translate(keys[i]);
        }
        return value;
    }
    public static boolean hasLanguage(String name){
        Config[] langs = languages.toArray(new Config[languages.size()]);
        for(Config lang : langs){
            if(lang.str("LanguageInfo.name").equals(name)){
                return true;
            }
        }
        return false;
    }
    public static void loadLanguages(String[] languages){
        for (String language : languages) {
            addLanguage(language);
        }
    }
    public static void setCurrentLanguageFile(File file){
        currentLanguage.setFile(file);
    }
    private LanguageManager(){
    }
}
