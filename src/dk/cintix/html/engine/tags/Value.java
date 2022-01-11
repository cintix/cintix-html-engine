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
        if (propertyName.startsWith("@")) {
            return getProperty(propertyName);
        }
        return getProperty("name");
    }

    @Override
    public String endTag() {
        return "";
    }

}
