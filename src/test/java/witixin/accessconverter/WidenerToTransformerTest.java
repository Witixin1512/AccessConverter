package witixin.accessconverter;



import net.witixin.accessconverter.Utils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.witixin.accessconverter.conversion.ATConverter;
import net.witixin.accessconverter.conversion.AWConverter;

import java.io.File;

import static org.gradle.internal.impldep.org.junit.Assert.assertEquals;

/**
 * Tests a couple of Access Widener to Access Transformer conversions.
 */


public class WidenerToTransformerTest {

    public static final Logger LOGGER = LoggerFactory.getLogger("AccessConverter-Test");

    @Test
    public void assertClass() {
        assertEquals("public net.minecraft.server.MinecraftServer$ReloadableResources", new ATConverter.ATEntry("public", "net.minecraft.server.MinecraftServer$ReloadableResources", "", "").toString());
    }

    @Test
    public void testEmojiful() {
        assertMod(new TestMetadata("emojiful", "1.19.3"), LOGGER);
    }

    @Test
    public void testCraftTweaker() {
        assertMod(new TestMetadata("crafttweaker", "1.19.2"), LOGGER);
    }

    @Test
    public void assertConstructor() {
        File tsrg = Utils.getTSRGPath("1.19.2");
        String tsrgContents = Utils.getFileContents(tsrg);
        assertEquals("public net.minecraft.world.damagesource.DamageSource <init>(Ljava/lang/String;)V",
                testLine("accessible method net/minecraft/world/damagesource/DamageSource <init> (Ljava/lang/String;)V", tsrgContents));

    }

    private void assertMod(TestMetadata metadata, Logger logger) {

        ClassLoader classLoader = getClass().getClassLoader();
        File rootFile = new File(classLoader.getResource("dummy.txt").getFile()).getParentFile();
        File modidFolder = new File(rootFile, metadata.modid());

        for (String gameVersion : metadata.versions()) {
            File tsrg = Utils.getTSRGPath(gameVersion);
            String tsrgContents = Utils.getFileContents(tsrg);
            File rootModFolder = new File(modidFolder, gameVersion);
            File clientMappings = Utils.getClientMappings(gameVersion);
            String mappingsContents = Utils.getFileContents(clientMappings);

            String modid = metadata.modid();
            File originalAW = new File(rootModFolder, "original_" + modid + ".accesswidener");
            File originalAT = new File(rootModFolder, "original_accesstransformer.cfg");
            File idealAT = new File(rootModFolder, "result.perfect_transformer");
            File idealAW = new File(rootModFolder, "result.perfect_widener");

            String atToAW = "accessWidener v1 named" + System.lineSeparator().concat(AWConverter.convertFile(logger, originalAT, tsrgContents, mappingsContents)).concat(System.lineSeparator());
            String awToAT = ATConverter.convertFile(logger, originalAW, tsrgContents).concat(System.lineSeparator());

            String perfectAWString = Utils.getFileContents(idealAW, true);
            assertEquals("AT to AW Conversion Failed: " + System.lineSeparator() + atToAW + System.lineSeparator(), perfectAWString, atToAW);
            String perfectATString = Utils.getFileContents(idealAT, true);
            assertEquals("AW to AT Conversion Failed: " + System.lineSeparator() + awToAT + System.lineSeparator(), perfectATString, awToAT);
        }
    }

    private static String testLine(String input, String tsrg) {
        String[] splitString = input.split(" ");
        String accessModifier = splitString[0].equals("accessible") ? "public" : "public-f";
        String type = splitString[1];
        String clazz = splitString[2];
        if ("class".equals(type)) {
            return new ATConverter.ATEntry(accessModifier, clazz.replace("/", "."), "", "").toString();
        } else {
            String mojmapName = splitString[3];
            //target is either the method signature if it's a method or the target type if it's a field
            String target = splitString[4];
            return ATConverter.convertMojmapToSrg(tsrg, mojmapName, target, accessModifier, clazz, type).toString();
        }
    }

}
