package battleship;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {

    private static ResourceBundle bundle;
    private static String language = "pt";

    public static void setLanguage(String lang) {
        language = lang.equalsIgnoreCase("en") ? "en" : "pt";
        Locale locale = language.equals("en") ? Locale.ENGLISH : Locale.forLanguageTag("pt");
        bundle = ResourceBundle.getBundle("messages", locale);
    }

    public static String getLanguage() {
        return language;
    }

    public static String get(String key) {
        if (bundle == null) {
            setLanguage("pt");
        }

        return bundle.getString(key);
    }

    public static String get(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }
}