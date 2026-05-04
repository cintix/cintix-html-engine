package dk.cintix.html.engine.tags;

import dk.cintix.html.engine.tags.base.HTMLTag;

/**
 *
 * @author Michael Martinsen
 */
public class Value extends HTMLTag {

    @Override
    public String startTag() {
        String propertyName = getProperty("name");
        if (propertyName == null) {
            return "";
        }

        if (propertyName.startsWith("@")) {
            String value = getProperty(propertyName);
            return value == null ? "" : value;
        }
        return propertyName;
    }

    @Override
    public String endTag() {
        return "";
    }

}
