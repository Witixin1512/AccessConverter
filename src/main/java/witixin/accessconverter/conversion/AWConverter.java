package witixin.accessconverter.conversion;

import org.slf4j.Logger;
import witixin.accessconverter.Utils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Converts an AT into an AW
 */
public class AWConverter {

    //This string does NOT contain the header (Version or namespace)
    public static String convertFile(Logger logger, File atFileToConvert, String tsrgContents, String mappingsContents) {
        List<String> accessWidenerOutput = new ArrayList<>();

        String lineCopy = "";

        try(BufferedReader br = new BufferedReader(new FileReader(atFileToConvert))) {
            String line = br.readLine();
            while (line != null && !line.isEmpty() && !line.startsWith("#")) {
                lineCopy = line;
                String parsedContent = convertLine(line, tsrgContents, mappingsContents);
                if (parsedContent.isEmpty()) throw new RuntimeException("Conversion of line: " + lineCopy + " did not finish correctly!");
                accessWidenerOutput.add(parsedContent);
                line = br.readLine();
            }

        } catch (Exception exception) {
            logger.error(exception.getMessage());
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.error(stacktrace);
            logger.error("[ERROR] Erroring line: " + lineCopy);
        }

        return accessWidenerOutput.stream().sorted(String::compareTo).collect(Collectors.joining(System.lineSeparator()));
    }

    public static boolean convertAT(Logger logger, String version, File awFileToOutputIn, File atFileToConvert, boolean sortInput) {

        if (atFileToConvert == null){
            logger.error("[ERROR] Null Access Transformer file provided, unable to convert");
            return false;
        }

        File tsrg = Utils.getTSRGPath(version);
        String contents = Utils.getFileContents(tsrg);

        File clientMappings = Utils.getClientMappings(version);
        String mappingsContents = Utils.getFileContents(clientMappings);

        if (mappingsContents.isEmpty()) {
            logger.error("[ERROR] Mappings File is empty! Verify that " + clientMappings.getAbsolutePath() + " is not empty!");
            return false;
        }

        if (contents.isEmpty()) {
            logger.error("[ERROR] Mappings File is empty! Verify that " + tsrg.getAbsolutePath() + " is not empty!");
            return false;
        }

        String toWrite = convertFile(logger, atFileToConvert, contents, mappingsContents);
        writeAWOutput(toWrite, awFileToOutputIn, logger);

        if (sortInput) {
            try {
                List<String> originalFile = Files.readAllLines(atFileToConvert.getAbsoluteFile().toPath());
                originalFile.sort(String::compareTo);
                try (FileWriter fileWriter = new FileWriter(atFileToConvert)) {
                    String originalContents = originalFile.stream().collect(Collectors.joining(System.lineSeparator()));
                    fileWriter.write(originalContents);
                    logger.error("Successfully sorted input Access Transformer File");
                    return true;
                }
                catch (IOException ioException) {
                    logger.error(ioException.getMessage());
                }
            }
            catch (IOException ignored) {}
        }
        else {
            return true;
        }
        return false;
    }

    public static String convertLine(String line, String tsrgContents, String mappingsContents) {
        String[] splitString = line.split(" ");
        String accessMod = splitString[0];
        String clazz = splitString[1].replace(".", "/");
        //In the case of a method, an srgName is appended by the signature.
        if (splitString.length > 2) {
            String srgName = splitString[2];
            if (srgName.startsWith("f")) {
                //Field handling

                String[] newString = tsrgContents.split(srgName);

                if (newString.length == 1) throw new IllegalArgumentException("The current line{" + line + "} contains an SRG Name that could not be found. Please double check it!");

                newString = newString[1].split(" ", 3);


                String resultString = newString[1];

                if (resultString.contains("<")) resultString = resultString.split("<")[0];

                if (resultString.contains("f_")){
                    resultString = resultString.substring(0, resultString.indexOf("f_"));
                }

                String[] classMapping = mappingsContents.split(splitString[1] + " ->");

                String tentativeSignature = Utils.reverseString(classMapping[1].split(resultString + " ->")[0]);
                String[] spacedOutString = tentativeSignature.split(" ", 3);
                String resultSignature = Utils.reverseString(spacedOutString[1]);

                String output = new AWEntry("accessible", "field", clazz, resultString, "L" +  resultSignature.replace(".", "/") + ";").toString();
                if (accessMod.equals("public-f"))  output = output.concat(System.lineSeparator()).concat(new AWEntry("mutable", "field", clazz, resultString, "L" +  resultSignature.replace(".", "/") + ";").toString());
                return output;
            }

            if (srgName.startsWith("m")) {
                //Method land
                String[] splitSrg = srgName.split("_", 3);
                int srgNumber = Integer.parseInt(splitSrg[1]);
                String nextSrg = String.valueOf(srgNumber + 1);
                String regex = "m_" + srgNumber + "_ " + splitSrg[2];
                regex = regex.replace("(", "\\(").replace("/", "\\/").replace(")", "\\)").replace("$", "\\$");
                String[] test = tsrgContents.split(regex);
                test = test[1].split(" ", 3);
                String reverseTest = test[2].substring(0, 40);
                test = test[1].split("m_" + nextSrg + "_");
                if (reverseTest.split("p_" + nextSrg + "_", 3).length >= 2) {
                    test[0] = test[0].substring(0, test[0].length() - 1);
                }
                if (test[0].endsWith("static"))test[0] = test[0].substring(0, test[0].length() - "static".length());

                String output = new AWEntry("accessible", "method", clazz, test[0], splitSrg[2]).toString();
                if (accessMod.equals("public-f")) output = output.concat(System.lineSeparator()).concat(new AWEntry("extendable", "method", clazz, test[0], splitSrg[2]).toString());
                return output;
            }
            if (srgName.startsWith("<init>")) {
                String result = "method " + clazz + " " + srgName.replace(">", "> ");
                String output = "accessible " + result;
                if (accessMod.equals("public-f")) output = output.concat(System.lineSeparator()).concat("extendable " + result);
                return output;
            }
        }
        else {
            String newAccess = "accessible ";
            if (accessMod.equals("public-f")){
                newAccess = "extendable ";
            }
            newAccess = newAccess.concat("class ");
            return newAccess + clazz;
        }
        return "";
    }

    private static void writeAWOutput(String output, File location, Logger logger) {
        try (FileWriter fileWriter = new FileWriter(location)) {
            fileWriter.write("accessWidener v1 named" + System.lineSeparator());
            fileWriter.write(output);
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }
    }

    private static class AWEntry {
        private final String accessKey;
        private final String type;
        private final String clazz;
        private final String mojmapName;
        private final String signature;

        public AWEntry(String accessKey, String type, String clazz, String mojmapName, String signature) {
            this.accessKey = accessKey;
            this.type = type;
            this.clazz = clazz;
            this.mojmapName = mojmapName;
            this.signature = signature;
        }

        @Override
        public String toString() {
            return String.format("%s %s %s %s %s", accessKey, type, clazz, mojmapName, signature);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AWEntry awEntry = (AWEntry) o;

            if (accessKey != null ? !accessKey.equals(awEntry.accessKey) : awEntry.accessKey != null) return false;
            if (type != null ? !type.equals(awEntry.type) : awEntry.type != null) return false;
            if (clazz != null ? !clazz.equals(awEntry.clazz) : awEntry.clazz != null) return false;
            if (mojmapName != null ? !mojmapName.equals(awEntry.mojmapName) : awEntry.mojmapName != null) return false;
            return signature != null ? signature.equals(awEntry.signature) : awEntry.signature == null;
        }

        @Override
        public int hashCode() {
            int result = accessKey != null ? accessKey.hashCode() : 0;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
            result = 31 * result + (mojmapName != null ? mojmapName.hashCode() : 0);
            result = 31 * result + (signature != null ? signature.hashCode() : 0);
            return result;
        }
    }
}
