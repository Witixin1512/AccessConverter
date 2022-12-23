package witixin.accessconverter;



import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import witixin.accessconverter.conversion.ATConverter;
import witixin.accessconverter.conversion.AWConverter;

import java.io.File;
import java.io.IOException;

import static org.gradle.internal.impldep.org.junit.Assert.assertEquals;

/**
 * Tests a couple of Access Widener to Access Transformer conversions.
 */


public class WidenerToTransformerTest {

    ClassLoader classLoader = getClass().getClassLoader();
    File emojifulAW = new File(classLoader.getResource("emojiful/original_emojiful.accesswidener").getFile());
    File emojifulIdeal = new File(classLoader.getResource("emojiful/result.perfect_widener").getFile());
    File emojifulSaveLocation = new File(emojifulAW.getParentFile(), "emojiful/result.perfect_transformer");

    File emojifulAT = new File(emojifulAW.getParentFile(), "original_accesstransformer.cfg");

    @Test
    public void testEverything() throws IOException {

        Logger logger = LoggerFactory.getLogger("AccessConverter-Test");

        File tsrg = Utils.getTSRGPath("1.19.3");
        String tsrgContents = Utils.getFileContents(tsrg);

        File clientMappings = Utils.getClientMappings("1.19.3");
        String mappingsContents = Utils.getFileContents(clientMappings);

        String atToAW = "accessWidener v1 named" + System.lineSeparator().concat(AWConverter.convertFile(logger, emojifulAT, tsrgContents, mappingsContents)).concat(System.lineSeparator());
        assertEmojiful(atToAW, "", new File(emojifulAW.getParentFile(), "result.perfect_widener"));

        String aWToAT = ATConverter.
    }



    private void assertEmojiful(String atToAW, String awToAT, File perfect) {
        String perfectAWString = Utils.getFileContents(perfect, true);
        assertEquals(perfectAWString, atToAW);
    }

}
