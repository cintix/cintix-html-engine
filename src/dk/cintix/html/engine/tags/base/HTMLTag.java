package dk.cintix.html.engine.tags.base;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Michael Martinsen
 */
public abstract class HTMLTag {

    private final Map<String, String> properties = new TreeMap<>();

    public void addProperties(Map<String, String> map) {
        properties.putAll(map);
    }

    public abstract String startTag();

    public abstract String endTag();

    protected String getProperty(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        }
        return null;
    }
}
