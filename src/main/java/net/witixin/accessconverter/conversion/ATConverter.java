package net.witixin.accessconverter.conversion;

import net.witixin.accessconverter.AccessConverterPlugin;
import net.witixin.accessconverter.Utils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Converts an AW into an AT
 */
public class ATConverter {


    public static String convertFile(Logger logger, File awFileToConvert, String tsrgContents) {
        Map<String, ATEntry> map = new HashMap<>();

        String lineCopy = "";

        try(BufferedReader br = new BufferedReader(new FileReader(awFileToConvert))) {
            int counter = 0;
            String line = br.readLine();
            while (line != null) {
                lineCopy = line;
                if (!line.isEmpty() && counter != 0 && !line.contains(AccessConverterPlugin.EXCLUDE_FROM_TRANSFORMATIONS_COMMENT)) {
                    String[] splitString = line.split(" ");
                    String accessModifier = splitString[0].equals("accessible") ? "public" : "public-f";
                    String type = splitString[1];
                    String clazz = splitString[2];
                    if ("class".equals(type)) {
                        map.put(clazz.replace("/", "_"), new ATEntry(accessModifier, clazz.replace("/", "."), "", ""));
                    } else {
                        String mojmapName = splitString[3];
                        //target is either the method signature if it's a method or the target type if it's a field
                        String target = splitString[4];
                        String key = (clazz + mojmapName + target);
                        ATEntry atEntry = map.get(key);
                        if (atEntry == null) {
                            atEntry = convertMojmapToSrg(tsrgContents, mojmapName, target, accessModifier, clazz, type);
                            map.put(key, atEntry);
                        } else {
                            if (atEntry.modifier.equals("public")) {
                                atEntry.setModifier("public-f");
                                map.put(key, atEntry);
                            }
                        }
                    }
                }
                line = br.readLine();
                ++counter;
            }
            }catch (Exception exception) {
                logger.error(exception.getMessage());
                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                String stacktrace = sw.toString();
                logger.error(stacktrace);
                logger.error("[ERROR] Erroring line: " + lineCopy);
            }

            return map.values().stream().map(ATEntry::toString).sorted(String::compareToIgnoreCase).collect(Collectors.joining(System.lineSeparator()));
        }
    
    public static boolean convertAW(Logger logger, String version, File awFileToConvert, File toOutputIn, boolean sortInput) {

        if (awFileToConvert == null){
            logger.error("[ERROR] Supplied null Access Widener file path in 'accessConverter/atExtension' extension! Specify one using 'fileToConvert' followed by a valid 'fileOutput'");
            return false;
        }

        File tsrg = Utils.getTSRGPath(version);
        String tsrgContents = Utils.getFileContents(tsrg);

        if (tsrgContents.isEmpty()){
            logger.error("[ERROR] tsrg contents are empty");
            logger.error("[ERROR] Verify that " + tsrg.getAbsolutePath() + " contains a valid tsrg file");
            logger.error("[ERROR] If there is nothing there, you NEED to import a " + version + "workspace with OFFICIAL Mappings on Forge!");
            return false;
        }

        String toWrite = convertFile(logger, awFileToConvert, tsrgContents);

        try (FileWriter fileWriter = new FileWriter(toOutputIn)) {
            fileWriter.write(toWrite);
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }

        if (sortInput) {
            try {
                List<String> originalFile = Files.readAllLines(awFileToConvert.getAbsoluteFile().toPath());
                originalFile.sort(String::compareToIgnoreCase);
                originalFile.removeIf(it -> it.equals("accessWidener v1 named"));
                try (FileWriter fileWriter = new FileWriter(awFileToConvert)) {
                    String originalContents = originalFile.stream().collect(Collectors.joining(System.lineSeparator()));
                    fileWriter.write("accessWidener v1 named");
                    fileWriter.write(System.lineSeparator());
                    fileWriter.write(originalContents);
                    logger.error("Successfully sorted input Access Widener File");
                    return true;
                }
                catch (IOException ioException) {
                    logger.error(ioException.getMessage());
                }
            }
            catch (IOException exception) {
                logger.error(exception.getMessage());
                StringWriter sw = new StringWriter();
                exception.printStackTrace(new PrintWriter(sw));
                String stacktrace = sw.toString();
                logger.error(stacktrace);
            }

        }
        else {
            return true;
        }

        return false;
    }

    public static ATEntry convertMojmapToSrg(String tsrgContents, String mojmapName, String target, String acccesModifier, String clazz, String javaType) {
        if (javaType.equals("method")) {
            String toFind = " " + target + " " + mojmapName;
            if (mojmapName.equals("<init>")) {
                return new ATEntry(acccesModifier, clazz.replace("/", "."), "", mojmapName + target);
            }
            else {
                int index = tsrgContents.indexOf(toFind);
                String result = Utils.reverseString(tsrgContents.substring(0, index));
                String[] srgSplit = result.split("_", 3);
                String finalSrgNumber = "m_" + Utils.reverseString(srgSplit[1]) + "_";
                return new ATEntry(acccesModifier, clazz.replace("/", "."), finalSrgNumber, target + " #" + mojmapName);
            }
        }
        if (javaType.equals("field")) {
            int indexOfClass = tsrgContents.indexOf(clazz + " " + clazz);
            String stringToFind = tsrgContents.substring(indexOfClass);
            int accurateStringPosition = stringToFind.indexOf(mojmapName);
            stringToFind = Utils.reverseString(stringToFind.substring(0, accurateStringPosition));
            String[] srgSplit = stringToFind.split("_", 3);
            String finalSrg = "f_" + Utils.reverseString(srgSplit[1]) + "_";
            return new ATEntry(acccesModifier, clazz.replace("/", "."), finalSrg + " #" + mojmapName,"");
        }
        return new ATEntry(acccesModifier, clazz, "INVALID_TYPE_NAME_SUPPLIED", target);
    }

    public static class ATEntry {
        public String modifier;
        public final String clazz;
        public final String srgName;
        public String signature;

        public ATEntry(String modifier, String clazz, String srgName, String signature) {
            this.modifier = modifier;
            this.clazz = clazz;
            this.srgName = srgName;
            this.signature = signature;
        }

        @Override
        public String toString() {
            boolean appendSpace = srgName.isEmpty() && !signature.isEmpty() && signature.startsWith("<init>");
            return modifier + " "  + clazz + (srgName.isEmpty() ? "" : " ") + srgName + (appendSpace ? " " : "") + signature;
        }

        void setModifier(String modifier){
            this.modifier = modifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ATEntry atEntry = (ATEntry) o;

            if (modifier != null ? !modifier.equals(atEntry.modifier) : atEntry.modifier != null) return false;
            if (clazz != null ? !clazz.equals(atEntry.clazz) : atEntry.clazz != null) return false;
            if (srgName != null ? !srgName.equals(atEntry.srgName) : atEntry.srgName != null) return false;
            return signature != null ? signature.equals(atEntry.signature) : atEntry.signature == null;
        }

        @Override
        public int hashCode() {
            int result = modifier != null ? modifier.hashCode() : 0;
            result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
            result = 31 * result + (srgName != null ? srgName.hashCode() : 0);
            result = 31 * result + (signature != null ? signature.hashCode() : 0);
            return result;
        }
    }
}
