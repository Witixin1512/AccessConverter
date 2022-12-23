package witixin.accessconverter;



import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import witixin.accessconverter.conversion.ATConverter;
import witixin.accessconverter.conversion.AWConverter;

import java.io.File;

import static org.gradle.internal.impldep.org.junit.Assert.assertEquals;

/**
 * Tests a couple of Access Widener to Access Transformer conversions.
 */


public class WidenerToTransformerTest {

    public static final String[] MC_VERSIONS = {"1.19.3"};

    @Test
    public void testEverything() {

        Logger logger = LoggerFactory.getLogger("AccessConverter-Test");

        for (String gameVersion : MC_VERSIONS) {
            File tsrg = Utils.getTSRGPath(gameVersion);
            String tsrgContents = Utils.getFileContents(tsrg);

            File clientMappings = Utils.getClientMappings(gameVersion);
            String mappingsContents = Utils.getFileContents(clientMappings);



            assertMod("emojiful", tsrgContents, mappingsContents, gameVersion, logger);
        }
    }

    private void assertMod(String modid, String tsrgContents, String mappingsContents, String mcVersion, Logger logger) {

        ClassLoader classLoader = getClass().getClassLoader();

        File originalAW = new File(classLoader.getResource(String.format("%s/%s/original_%s.accesswidener", mcVersion, modid, modid)).getFile());
        File originalAT = new File(classLoader.getResource(String.format("%s/%s/original_accesstransformer.cfg", mcVersion, modid, modid)).getFile());
        File idealAT = new File(classLoader.getResource(String.format("%s/%s/result.perfect_transformer", mcVersion, modid, modid)).getFile());
        File idealAW = new File(classLoader.getResource(String.format("%s/%s/result.perfect_widener", mcVersion, modid, modid)).getFile());

        String atToAW = "accessWidener v1 named" + System.lineSeparator().concat(AWConverter.convertFile(logger, originalAT, tsrgContents, mappingsContents)).concat(System.lineSeparator());
        String awToAT = ATConverter.convertFile(logger, originalAW, tsrgContents).concat(System.lineSeparator());

        String perfectAWString = Utils.getFileContents(idealAW, true);
        assertEquals(perfectAWString, atToAW);
        String perfectATString = Utils.getFileContents(idealAT, true);
        assertEquals(perfectATString, awToAT);

        System.out.println(awToAT   );
    }


}
