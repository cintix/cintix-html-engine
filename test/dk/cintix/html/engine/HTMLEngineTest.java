package dk.cintix.html.engine;

import dk.cintix.html.engine.tags.base.HTMLTag;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class HTMLEngineTest {

    public static void main(String[] args) throws Exception {
        HTMLEngineTest test = new HTMLEngineTest();
        test.shouldInjectVariableWhenUsingValueTag();
        test.shouldHandleSelfClosingTagWithSlashInAttributeValue();
        test.shouldHandleNestedTagsWithSameName();
        test.shouldSupportSingleQuotedAttributes();
        test.shouldThrowOnMissingClosingTag();
        System.out.println("All HTMLEngine tests passed");
    }

    public void shouldInjectVariableWhenUsingValueTag() throws Exception {
        // Arrange
        setupNamespace();
        HTMLEngine.addClass("value", EchoNameTag.class);
        File template = createTempTemplate("<x:test><x:value name=\"@user\" /></x:test>");
        Map<String, String> variables = new TreeMap<>();
        variables.put("@user", "Alice");

        // Act
        String result = HTMLEngine.process(template, variables, new TreeMap<>());

        // Assert
        assertEquals("Alice", result, "variable injection failed");
    }

    public void shouldHandleSelfClosingTagWithSlashInAttributeValue() throws Exception {
        // Arrange
        setupNamespace();
        HTMLEngine.addClass("wrap", WrapTag.class);
        File template = createTempTemplate("<x:wrap name=\"a/b\" />");

        // Act
        String result = HTMLEngine.process(template, new TreeMap<>(), new TreeMap<>());

        // Assert
        assertEquals("[a/b]", result, "self-closing detection broke when attribute contains slash");
    }

    public void shouldHandleNestedTagsWithSameName() throws Exception {
        // Arrange
        setupNamespace();
        HTMLEngine.addClass("box", BoxTag.class);
        File template = createTempTemplate("<x:box><x:box>core</x:box></x:box>");

        // Act
        String result = HTMLEngine.process(template, new TreeMap<>(), new TreeMap<>());

        // Assert
        assertEquals("((core))", result, "nested matching failed for same tag name");
    }

    public void shouldSupportSingleQuotedAttributes() throws Exception {
        // Arrange
        setupNamespace();
        HTMLEngine.addClass("wrap", WrapTag.class);
        File template = createTempTemplate("<x:wrap name='single-quoted' />");

        // Act
        String result = HTMLEngine.process(template, new TreeMap<>(), new TreeMap<>());

        // Assert
        assertEquals("[single-quoted]", result, "single-quoted attributes were not parsed");
    }

    public void shouldThrowOnMissingClosingTag() throws Exception {
        // Arrange
        setupNamespace();
        HTMLEngine.addClass("box", BoxTag.class);
        File template = createTempTemplate("<x:box>broken");

        // Act + Assert
        boolean failedAsExpected = false;
        try {
            HTMLEngine.process(template, new TreeMap<>(), new TreeMap<>());
        } catch (IOException e) {
            failedAsExpected = true;
        }
        assertTrue(failedAsExpected, "missing end tag should throw IOException");
    }

    private void setupNamespace() {
        HTMLEngine.setNamespace("x");
    }

    private File createTempTemplate(String content) throws IOException {
        File temp = File.createTempFile("engine-test", ".html");
        temp.deleteOnExit();
        try (FileWriter writer = new FileWriter(temp)) {
            writer.write(content);
        }
        return temp;
    }

    private void assertEquals(String expected, String actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected=[" + expected + "] actual=[" + actual + "]");
        }
    }

    private void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    public static class EchoNameTag extends HTMLTag {
        @Override
        public String startTag() {
            String propertyName = getProperty("name");
            if (propertyName != null && propertyName.startsWith("@")) {
                String resolved = getProperty(propertyName);
                return resolved == null ? "" : resolved;
            }
            return propertyName == null ? "" : propertyName;
        }

        @Override
        public String endTag() {
            return "";
        }
    }

    public static class WrapTag extends HTMLTag {
        @Override
        public String startTag() {
            return "[" + getProperty("name") + "]";
        }

        @Override
        public String endTag() {
            return "";
        }
    }

    public static class BoxTag extends HTMLTag {
        @Override
        public String startTag() {
            return "(";
        }

        @Override
        public String endTag() {
            return ")";
        }
    }
}
