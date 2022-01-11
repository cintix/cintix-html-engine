package dk.cintix.html.engine;

import dk.cintix.html.engine.tags.Value;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Michael Martinsen
 */
public class HTMLEngineTest {

    public static void main(String[] args) {
        HTMLEngineTest htmlEngineTest = new HTMLEngineTest();
    }

    public HTMLEngineTest() {
        setup();
        testProcess();
    }

    public void setup() {
        try {
            HTMLEngine.setNamespace("cintix-stream");
            HTMLEngine.addClass("value", Value.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testProcess() {
        try {
            System.out.println("process");

            Map<String, String> variables = new TreeMap<>();
            variables.put("@currentUsername", "Michael Martinsen");

            File file = new File("resources/index.html");
            String expResult = "<!DOCTYPE html>\n"
                    + "<!--\n"
                    + "-->\n"
                    + "<html>\n"
                    + "    <head>\n"
                    + "        <title>Tag Test</title>\n"
                    + "        <meta charset=\"UTF-8\">\n"
                    + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                    + "    </head>\n"
                    + "    <body>\n"
                    + "        <div>Some content to be shown</div>\n"
                    + "        \n"
                    + "            <h2>Michael Martinsen</h2>\n"
                    + "        \n"
                    + "    </body>\n"
                    + "</html>";

            String result = HTMLEngine.process(file, variables);
            if (expResult.equalsIgnoreCase(result)) {
                System.out.println("\tHTMLEngine.process:: Test pass");
            } else {
                System.err.println("\tHTMLEngine.process:: Test failed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
