package dk.cintix.html.engine.tags.base;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Michael Martinsen
 */
public abstract class HTMLTag {

    private final Map<String, String> properties = new TreeMap<>();
    private final Map<String, Object> resources = new TreeMap<>();

    public void addProperties(Map<String, String> map) {
        properties.putAll(map);
    }

    public void addResource(Map<String, Object> map) {
        resources.putAll(map);
    }

    public abstract String startTag();

    public abstract String endTag();

    protected <T> T getResource(Class<?> cls) {
        if (resources.containsKey(cls.getName())) {
            return (T) resources.get(cls.getName());
        }
        return null;
    }

    protected String getProperty(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        }
        return null;
    }
}
