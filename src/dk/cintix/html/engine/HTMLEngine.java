package dk.cintix.html.engine;

import dk.cintix.html.engine.tags.base.HTMLTag;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public static String process(File file, Map<String, String> properties, Map<String, Object> resources) throws IOException {
        String filedata = readFile(file);
        String parseHTML = processHTML(filedata, properties, resources);
        return parseHTML.trim();
    }

    public static String process(File file) throws IOException {
        return process(file, new TreeMap<>(), new TreeMap<>());
    }

    private static String processHTML(String code) throws IOException {
        return processHTML(code, new TreeMap<>(), new TreeMap<>());
    }

    private static String processHTML(String code, Map<String, String> properties, Map<String, Object> resources) throws IOException {
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

            if (offset >= 0) {
                if (lastOffset != offset) {
                    parseHTML += code.substring(lastOffset, offset);
                }
                int tagEndIndex = code.indexOf(">", offset);
                String tagData = code.substring(offset, tagEndIndex + 1);
                if (tagData.contains("/")) {
                    System.out.println("SINGLE LINE!");
                    HTMLTag proccessTag = proccessTag(tagData, properties);
                    if (proccessTag != null) {
                        proccessTag.addResource(resources);
                        parseHTML += proccessTag.startTag() + proccessTag.endTag();
                    }
                    offset = tagEndIndex + 1;
                } else {
                    int endingTagOffset = code.indexOf(prefixEnd, tagEndIndex);
                    if (endingTagOffset != -1) {
                        int endTagEndIndex = code.indexOf(">", endingTagOffset);
                        String innerHTML = code.substring(tagEndIndex + 1, endingTagOffset);
                    System.out.println("SINGLE LINE!");

                        HTMLTag proccessTag = proccessTag(tagData, properties);
                        if (proccessTag != null) {
                            proccessTag.addResource(resources);
                            parseHTML += proccessTag.startTag();
                        }
                        parseHTML += processHTML(innerHTML, properties, resources);
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
        int classEndIndex = code.indexOf(" ", keyOffset);
        if (classEndIndex == -1) {
            classEndIndex = code.indexOf(">", keyOffset);
        }

        String clsKey = code.substring(1, classEndIndex);
        try {
            String propertiesString = code.substring(1 + clsKey.length(), code.length() - 1).trim();
            if (propertiesString.endsWith("/")) {
                propertiesString = propertiesString.substring(0, propertiesString.length() - 1);
            }

            Map<String, String> properties = new TreeMap<>();
            List<String> propertyKeys = readParamters(propertiesString);
            for (String property : propertyKeys) {
                if (!property.contains("=")) {
                    continue;
                }
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

    private static List<String> readParamters(String line) {
        List<String> parameters = new ArrayList<>();
        int offset = 0;

        while (offset != -1) {
            offset = line.indexOf("=", offset);
            if (offset == -1) {
                break;
            }

            int start = line.indexOf(" ");
            if (start == -1 || start > offset) {
                start = 0;
            }

            int firstMark = line.indexOf("\"", offset);
            int lastMark = line.indexOf("\"", firstMark + 1);

            if (firstMark == -1 || lastMark == -1) {
                break;
            }
            lastMark++;

            parameters.add(line.substring(start, lastMark));
            offset = lastMark;
        }

        return parameters;
    }

    private static String readFile(File file) {
        String filedata = "";
        try ( FileInputStream fileinput = new FileInputStream(file)) {
            int read = 0;
            byte[] buffer = new byte[2048 * 2];
            while (read != -1) {
                read = fileinput.read(buffer);
                if (read != -1) {
                    filedata += new String(buffer, 0, read);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filedata.isEmpty() ? null : filedata;
    }

}
