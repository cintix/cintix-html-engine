package dk.cintix.html.engine;

import dk.cintix.html.engine.tags.base.HTMLTag;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Michael Martinsen
 */
public class HTMLEngine {

    private static final Map<String, Class> registeredClasses = new TreeMap<>();
    private static String namespace;

    public static void setNamespace(String name) {
        namespace = name;
    }

    public static void addClass(String name, Class<?> cls) throws IOException {
        if (namespace.isEmpty()) {
            throw new IOException("can't registere class without namespace");
        }
        String key = namespace + ":" + name;
        if (!registeredClasses.containsKey(key)) {
            registeredClasses.put(key, cls);
        }
    }

    public static String process(File file, Map<String, String> properties) throws IOException {
        String filedata = readFile(file);
        String parseHTML = processHTML(filedata, properties);
        return parseHTML.trim();
    }

    public static String process(File file) throws IOException {
        return process(file, new TreeMap<>());
    }

    private static String processHTML(String code) throws IOException {
        return processHTML(code, new TreeMap<>());
    }

    private static String processHTML(String code, Map<String, String> properties) throws IOException {
        int offset = 0;
        int lastOffset = 0;

        String prefix = "<" + namespace + ":";
        String prefixEnd = "</" + namespace + ":";
        String parseHTML = "";

        while (offset != -1) {
            lastOffset = offset;
            offset = code.indexOf(prefix, offset);

            if (offset == -1 && lastOffset > 0) {
                if (lastOffset < code.length()) {
                    parseHTML += code.substring(lastOffset);
                }
            }

            if (offset > 0) {
                if (lastOffset != offset) {
                    parseHTML += code.substring(lastOffset, offset);
                }
                int tagEndIndex = code.indexOf(">", offset);
                String tagData = code.substring(offset, tagEndIndex + 1);

                if (tagData.contains("/")) {
                    HTMLTag proccessTag = proccessTag(tagData, properties);
                    if (proccessTag != null) {
                        parseHTML += proccessTag.startTag() + proccessTag.endTag();
                    }
                    offset = tagEndIndex + 1;
                } else {
                    int endingTagOffset = code.indexOf(prefixEnd, tagEndIndex);
                    if (endingTagOffset != -1) {
                        int endTagEndIndex = code.indexOf(">", endingTagOffset);
                        String innerHTML = code.substring(tagEndIndex + 1, endingTagOffset);

                        HTMLTag proccessTag = proccessTag(tagData, properties);
                        if (proccessTag != null) {
                            parseHTML += proccessTag.startTag();
                        }
                        parseHTML += processHTML(innerHTML, properties);
                        if (proccessTag != null) {
                            parseHTML += proccessTag.endTag();
                        }

                        offset = endTagEndIndex + 1;
                    } else {
                        throw new IOException("Invalid tags missing end for " + prefix);
                    }
                }
            }
        }

        if (parseHTML.isEmpty()) {
            return code;
        }

        return parseHTML;
    }

    private static HTMLTag proccessTag(String code, Map<String, String> predefinedValues) {
        int keyOffset = code.indexOf(":");
        HTMLTag htmlTag = null;

        String clsKey = code.substring(1, code.indexOf(" ", keyOffset));

        try {
            String propertiesString = code.substring(1 + clsKey.length(), code.length() - 1).trim();
            if (propertiesString.endsWith("/")) {
                propertiesString = propertiesString.substring(0, propertiesString.length() - 1);
            }
            Map<String, String> properties = new TreeMap<>();
            String[] propertyKeys = propertiesString.split(" ");
            for (String property : propertyKeys) {
                String[] keyValue = property.split("=");
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                if (value.equalsIgnoreCase("\"\"")) {
                    value = "";
                }

                if (!value.isEmpty()) {
                    value = value.substring(1);
                    value = value.substring(0, value.length() - 1);
                }

                properties.put(key, value);
            }

            if (registeredClasses.containsKey(clsKey)) {
                htmlTag = (HTMLTag) registeredClasses.get(clsKey).getDeclaredConstructor().newInstance();
                htmlTag.addProperties(properties);
                htmlTag.addProperties(predefinedValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return htmlTag;
    }

    private static String readFile(File file) {
        String filedata = "";
        try ( FileInputStream fileinput = new FileInputStream(file)) {
            int read = 0;
            byte[] buffer = new byte[2048 * 2];
            while (read != -1) {
                read = fileinput.read(buffer);
                if (read != -1) {
                    filedata = new String(buffer, 0, read);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return filedata.isEmpty() ? null : filedata;
    }

}
