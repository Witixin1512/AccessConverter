package witixin.accessconverter.conversion;

import org.gradle.api.Project;
import witixin.accessconverter.AccessConverterPlugin;
import witixin.accessconverter.Utils;

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
            project.getLogger().error("Supplied null Access Widener file path in 'accessConverter/atExtension' extension! Specify one using 'fileToConvert' followed by a valid File");
            return false;
        }

        File tsrg = Utils.getTSRGPath(project, version);
        String tsrgContents = Utils.getFileContents(tsrg, project);
        if (tsrgContents.isEmpty()){
            project.getLogger().error("tsrg contents are empty");
            project.getLogger().error("Verify that " + tsrg.getAbsolutePath() + " contains a valid tsrg file");
            return false;
        }
        Map<String, ATEntry> map = new HashMap<>();

        String lineCopy = "";

        try(BufferedReader br = new BufferedReader(new FileReader(awFileToConvert))) {
            int counter = 0;
            String line = br.readLine();
            while (line != null) {
                lineCopy = line;
                if (!line.isEmpty() && counter != 0 && !line.contains(AccessConverterPlugin.EXCLUDE_FROM_TRANSFORMATIONS_COMMENT)) {
                    String[] splitString = line.split(" ");
                    System.out.println(line + " " + counter);
                    String accessModifier = splitString[0].equals("accessible") ? "public" : "public-f";
                    String type = splitString[1];
                    String clazz = splitString[2];
                    if ("class".equals(type)) {
                       map.put(clazz.replace("/", "_"), new ATEntry("public", clazz.replace("/", "."), "", ""));
                    }
                    else {
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
                }
                line = br.readLine();
                ++counter;
            }

            String toWrite = map.values().stream().map(ATEntry::toString).collect(Collectors.joining(System.lineSeparator()));

            try (FileWriter fileWriter = new FileWriter(toOutputIn)) {
                fileWriter.write(toWrite);
                project.getLogger().info(toWrite);
                return true;
            } catch (IOException exception) {
                project.getLogger().error(exception.getMessage());
            }
        }
        catch (Exception exception) {
            project.getLogger().error(exception.getMessage());
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            project.getLogger().error(stacktrace);
            project.getLogger().error("Erroring line: " + lineCopy);
        }
        return false;
    }

    public static ATEntry convertMojmapToSrg(String tsrgContents, String mojmapName, String target, String acccesModifier, String clazz, String javaType) {
        if (javaType.equals("method")) {
            String toFind = " " + target + " " + mojmapName;
            int index = tsrgContents.indexOf(toFind);
            String result = Utils.reverseString(tsrgContents.substring(0, index));
            String[] srgSplit = result.split("_", 3);
            String finalSrgNumber = "m_" + Utils.reverseString(srgSplit[1]) + "_";
            return new ATEntry(acccesModifier, clazz.replace("/", "."), finalSrgNumber, target + " #" + mojmapName);
        }
        if (javaType.equals("field")) {
            int indexOfClass = tsrgContents.indexOf(clazz + " " + clazz);
            String stringToFind = tsrgContents.substring(indexOfClass);
            int accurateStringPosition = stringToFind.indexOf(mojmapName);
            stringToFind = Utils.reverseString(stringToFind.substring(0, accurateStringPosition));
            String[] srgSplit = stringToFind.split("_", 3);
            String finalSrg = "f_" + Utils.reverseString(srgSplit[1]) + "_";
            return new ATEntry(acccesModifier, clazz.replace("/", "."), finalSrg + " #" + mojmapName,target);
        }
        return new ATEntry(acccesModifier, clazz, "INVALID_TYPE_NAME_SUPPLIED", target);
    }

    private static class ATEntry {
        public String modifier;
        public final String clazz;
        public final String srgName;
        public String signature;

        ATEntry(String modifier, String clazz, String srgName, String signature) {
            this.modifier = modifier;
            this.clazz = clazz;
            this.srgName = srgName;
            this.signature = signature;
        }

        public String toString() {
            return modifier + " "  + clazz + " " + srgName + (srgName.startsWith("m") ? "" : " ") + (srgName.startsWith("f_") ? "" : signature);
        }

        void setModifier(String modifier){
            this.modifier = modifier;
        }
    }
}
