package dk.cintix.html.engine;

import dk.cintix.html.engine.tags.base.HTMLTag;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Michael Martinsen
 */
public class HTMLEngine {

    private static final Map<String, Class<?>> registeredClasses = new TreeMap<>();
    private static String namespace;

    public static void setNamespace(String name) {
        namespace = name;
    }

    public static void addClass(String name, Class<?> cls) throws IOException {
        if (namespace == null || namespace.isEmpty()) {
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
        if (code == null || code.isEmpty() || namespace == null || namespace.isEmpty()) {
            return code == null ? "" : code;
        }

        String prefix = "<" + namespace + ":";
        String prefixEnd = "</" + namespace + ":";
        int cursor = 0;
        StringBuilder parsed = new StringBuilder(code.length());

        while (cursor < code.length()) {
            int tagStart = code.indexOf(prefix, cursor);
            if (tagStart < 0) {
                parsed.append(code, cursor, code.length());
                break;
            }

            parsed.append(code, cursor, tagStart);

            int tagEnd = code.indexOf('>', tagStart);
            if (tagEnd < 0) {
                throw new IOException("Invalid tag missing '>' near index " + tagStart);
            }

            String tagData = code.substring(tagStart, tagEnd + 1);
            boolean selfClosing = isSelfClosing(tagData);

            if (selfClosing) {
                HTMLTag processTag = proccessTag(tagData, properties);
                if (processTag != null) {
                    processTag.addResource(resources);
                    parsed.append(processTag.startTag());
                    parsed.append(processTag.endTag());
                }
                cursor = tagEnd + 1;
                continue;
            }

            int closingStart = findMatchingClosingTag(code, tagStart, tagEnd + 1, prefix, prefixEnd);
            if (closingStart < 0) {
                throw new IOException("Invalid tags missing end for " + prefix);
            }

            int closingEnd = code.indexOf('>', closingStart);
            if (closingEnd < 0) {
                throw new IOException("Invalid end tag missing '>' near index " + closingStart);
            }

            String innerHTML = code.substring(tagEnd + 1, closingStart);
            HTMLTag processTag = proccessTag(tagData, properties);
            if (processTag != null) {
                processTag.addResource(resources);
                parsed.append(processTag.startTag());
            }

            parsed.append(processHTML(innerHTML, properties, resources));

            if (processTag != null) {
                parsed.append(processTag.endTag());
            }

            cursor = closingEnd + 1;
        }

        return parsed.toString();
    }

    private static int findMatchingClosingTag(String code, int openTagStart, int searchFrom, String prefix, String prefixEnd) throws IOException {
        int depth = 1;
        int cursor = searchFrom;

        while (cursor < code.length()) {
            int nextOpen = code.indexOf(prefix, cursor);
            int nextClose = code.indexOf(prefixEnd, cursor);

            if (nextClose < 0) {
                return -1;
            }

            if (nextOpen >= 0 && nextOpen < nextClose) {
                int openEnd = code.indexOf('>', nextOpen);
                if (openEnd < 0) {
                    throw new IOException("Invalid tag missing '>' near index " + nextOpen);
                }

                String nestedTag = code.substring(nextOpen, openEnd + 1);
                if (!isSelfClosing(nestedTag)) {
                    depth++;
                }
                cursor = openEnd + 1;
                continue;
            }

            depth--;
            if (depth == 0) {
                return nextClose;
            }

            int closeEnd = code.indexOf('>', nextClose);
            if (closeEnd < 0) {
                throw new IOException("Invalid end tag missing '>' near index " + nextClose);
            }
            cursor = closeEnd + 1;
        }

        return -1;
    }

    private static boolean isSelfClosing(String tagData) {
        int idx = tagData.length() - 2;
        while (idx >= 0 && Character.isWhitespace(tagData.charAt(idx))) {
            idx--;
        }
        return idx >= 0 && tagData.charAt(idx) == '/';
    }

    private static HTMLTag proccessTag(String code, Map<String, String> predefinedValues) {
        int keyOffset = code.indexOf(':');
        if (keyOffset < 0) {
            return null;
        }

        int classEndIndex = code.indexOf(' ', keyOffset);
        if (classEndIndex == -1) {
            classEndIndex = code.indexOf('>', keyOffset);
        }

        if (classEndIndex < 0) {
            return null;
        }

        String clsKey = code.substring(1, classEndIndex);
        try {
            String propertiesString = code.substring(1 + clsKey.length(), code.length() - 1).trim();
            if (propertiesString.endsWith("/")) {
                propertiesString = propertiesString.substring(0, propertiesString.length() - 1).trim();
            }

            Map<String, String> properties = readParameters(propertiesString);

            if (registeredClasses.containsKey(clsKey)) {
                Class<? extends HTMLTag> tagClass = registeredClasses.get(clsKey).asSubclass(HTMLTag.class);
                HTMLTag htmlTag = tagClass.getDeclaredConstructor().newInstance();
                htmlTag.addProperties(properties);
                htmlTag.addProperties(predefinedValues);
                return htmlTag;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Map<String, String> readParameters(String line) {
        Map<String, String> parameters = new HashMap<>();
        int index = 0;

        while (index < line.length()) {
            while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
                index++;
            }
            if (index >= line.length()) {
                break;
            }

            int keyStart = index;
            while (index < line.length() && !Character.isWhitespace(line.charAt(index)) && line.charAt(index) != '=') {
                index++;
            }
            String key = line.substring(keyStart, index);

            while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
                index++;
            }
            if (index >= line.length() || line.charAt(index) != '=') {
                break;
            }
            index++; // skip '='

            while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
                index++;
            }
            if (index >= line.length()) {
                parameters.put(key, "");
                break;
            }

            char quote = line.charAt(index);
            String value;
            if (quote == '"' || quote == '\'') {
                index++;
                int valueStart = index;
                while (index < line.length() && line.charAt(index) != quote) {
                    index++;
                }
                value = line.substring(valueStart, Math.min(index, line.length()));
                if (index < line.length()) {
                    index++;
                }
            } else {
                int valueStart = index;
                while (index < line.length() && !Character.isWhitespace(line.charAt(index))) {
                    index++;
                }
                value = line.substring(valueStart, index);
            }

            parameters.put(key, value);
        }

        return parameters;
    }

    private static String readFile(File file) {
        StringBuilder filedata = new StringBuilder();
        try (FileInputStream fileinput = new FileInputStream(file)) {
            int read = 0;
            byte[] buffer = new byte[4096];
            while (read != -1) {
                read = fileinput.read(buffer);
                if (read != -1) {
                    filedata.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filedata.toString();
    }

}
