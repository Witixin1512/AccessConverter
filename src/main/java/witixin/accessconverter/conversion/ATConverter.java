package witixin.accessconverter.conversion;

import org.gradle.api.Project;
import witixin.accessconverter.AccessConverterPlugin;
import witixin.accessconverter.Utils;
import witixin.accessconverter.conversion.ATEntry;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Converts an AW into an AT
 */
public class ATConverter {

    public static boolean convertAW(Project project, String version, File awFileToConvert, File toOutputIn) {

        if (awFileToConvert == null){
            project.getLogger().error("Supplied null aw file path in 'accessConverter/atExtension' extension! Specify one using 'fileToConvert' followed by a valid File");
            return false;
        }

        File tsrg = getPathFromMinecraftVersion(project, version);
        String tsrgContents = getFileContents(tsrg, project);
        if (tsrgContents.isEmpty()){
            project.getLogger().error("tsrg contents are empty");
            project.getLogger().error("Verify that " + tsrg.getAbsolutePath() + " contains a valid tsrg file");
            return false;
        }
        Map<String, ATEntry> map = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(awFileToConvert))) {
            String line = br.readLine();
            int counter = 0;
            while (line != null) {
                if (counter != 0 && !line.contains(AccessConverterPlugin.EXCLUDE_FROM_TRANSFORMATIONS_COMMENT)) {
                    String[] splitString = line.split(" ");
                    String accessModifier = splitString[0].equals("accessible") ? "public" : "public-f";
                    String type = splitString[1];
                    String clazz = splitString[2];
                    String mojmapName = splitString[3];
                    //target is either the method signature if it's a method or the target type if it's a field
                    String target = splitString[4];
                    ATEntry atEntry = map.get(mojmapName);
                    if (atEntry == null) {
                        atEntry = convertMojmapToSrg(tsrgContents, mojmapName, target, accessModifier, clazz, type);
                        map.put(mojmapName, atEntry);
                    }
                    else {
                        if (!atEntry.modifier.equals("public-f")) atEntry.setModifier("public-f");
                        map.put(mojmapName, atEntry);
                    }
                }
                line = br.readLine();
                ++counter;
            }

            String toWrite = map.values().stream().map(ATEntry::toString).collect(Collectors.joining(System.lineSeparator()));

            try (FileWriter fileWriter = new FileWriter(toOutputIn)) {
                fileWriter.write(toWrite);
                project.getLogger().info("FML");
                project.getLogger().info(toWrite);
                return true;
            } catch (IOException exception) {
                project.getLogger().error(exception.getMessage());
            }
        }
        catch (IOException exception) {
            project.getLogger().error(exception.getMessage());
        }
        project.getLogger().error("Hmmm something went wrong");
        return false;
    }

    public static ATEntry convertMojmapToSrg(String tsrgContents, String mojmapName, String target, String acccesModifier, String clazz, String javaType) {
        if (javaType.equals("method")) {
            String toFind = " " + target + " " + mojmapName;
            int index = tsrgContents.indexOf(toFind);
            String result = reverse(tsrgContents.substring(0, index));
            String[] srgSplit = result.split("_", 3);
            String finalSrgNumber = "m_" + reverse(srgSplit[1]) + "_";
            return new ATEntry(acccesModifier, clazz.replace("/", "."), finalSrgNumber, target + " #" + mojmapName);
        }
        if (javaType.equals("field")) {
            int indexOfClass = tsrgContents.indexOf(clazz + " " + clazz);
            String stringToFind = tsrgContents.substring(indexOfClass);
            int accurateStringPosition = stringToFind.indexOf(mojmapName);
            stringToFind = reverse(stringToFind.substring(0, accurateStringPosition));
            String[] srgSplit = stringToFind.split("_", 3);
            String finalSrg = "f_" + reverse(srgSplit[1]) + "_";
            return new ATEntry(acccesModifier, clazz.replace("/", "."), finalSrg + " #" + mojmapName,target);
        }
        return new ATEntry(acccesModifier, clazz, "INVALID_TYPE_NAME_SUPPLIED", target);
    }

    static final Map<String, String> MC_VERSION_TO_MCP = Utils.makeMap(map -> {
        map.put("1.18.2", "20220404.173914");
        map.put("1.19.2", "20220805.130853");
        return map;
    });

    public static File getPathFromMinecraftVersion(Project project, String version) {
        File mcpConfigFolder = new File(project.getGradle().getGradleUserHomeDir(), "/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config");
        return new File(mcpConfigFolder, version + "-" + MC_VERSION_TO_MCP.get(version) + "/srg_to_official_" + version + ".tsrg");
    }

    private static String getFileContents(File tsrg, Project project) {
        try(BufferedReader br = new BufferedReader(new FileReader(tsrg))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line.replace("\t", "").replace(System.lineSeparator(), ""));
                line = br.readLine();
            }
           return sb.toString();
        }
        catch (IOException exception) {
            project.getLogger().error(exception.getMessage());
        }
        return "";
    }

    private static String reverse(String toReverse) {
        StringBuilder sb = new StringBuilder(toReverse);
        sb.reverse();
        return sb.toString();
    }
}
