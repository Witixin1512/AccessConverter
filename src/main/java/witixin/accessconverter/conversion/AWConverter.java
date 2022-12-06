package witixin.accessconverter.conversion;

import org.gradle.api.Project;
import witixin.accessconverter.Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Converts an AT into an AW
 */
public class AWConverter {
    public static boolean convertAW(Project project, String version, File awFileToOutputIn, File atFileToConvert, boolean sortInput) {

        if (atFileToConvert == null){
            project.getLogger().error("[ERROR] Null Access Transformer file provided, unable to convert");
            return false;
        }

        File tsrg = Utils.getTSRGPath(project, version);
        String contents = Utils.getFileContents(tsrg, project);

        File clientMappings = Utils.getClientMappings(version, project);
        String mappingsContents = Utils.getFileContents(clientMappings, project);

        if (mappingsContents.isEmpty()) {
            project.getLogger().error("[ERROR] Mappings File is empty! Verify that " + clientMappings.getAbsolutePath() + " is not empty!");
            return false;
        }

        List<String> accessWidenerOutput = new ArrayList<>();

        accessWidenerOutput.add("accessWidener v1 named");

        String lineCopy = "";

        try(BufferedReader br = new BufferedReader(new FileReader(atFileToConvert))) {
            String line = br.readLine();
            while (line != null && !line.isEmpty()) {
                lineCopy = line;
                String[] splitString = line.split(" ");
                String accessMod = splitString[0];
                String clazz = splitString[1].replace(".", "/");
                //In the case of a method, an srgName is appended by the signature.

                if (splitString.length > 2) {
                    String srgName = splitString[2];
                    if (srgName.startsWith("f")) {
                        //Field handling
                        String[] newString = contents.split(srgName)[1].split(" ", 2);
                        String resultString = newString[1];
                        if (newString[1].contains("<")) resultString = newString[1].split("<")[0];

                        String[] classMapping = mappingsContents.split(splitString[1] + " ->");

                        String tentativeSignature = Utils.reverseString(classMapping[1].split(resultString + " ->")[0]);
                        String[] spacedOutString = tentativeSignature.split(" ", 3);
                        String resultSignature = Utils.reverseString(spacedOutString[1]);

                        accessWidenerOutput.add(new AWEntry("accessible", "field", clazz, resultString, "L" +  resultSignature.replace(".", "/") + ";").toString());
                        if (accessMod.equals("public-f"))  accessWidenerOutput.add(new AWEntry("mutable", "field", clazz, resultString, "L" +  resultSignature.replace(".", "/") + ";").toString());
                    }

                    if (srgName.startsWith("m")) {
                        //Method land
                        String[] splitSrg = srgName.split("_", 3);
                        int srgNumber = Integer.parseInt(splitSrg[1]);
                        String nextSrg = String.valueOf(srgNumber + 1);
                        String regex = "m_" + srgNumber + "_ " + splitSrg[2];
                        regex = regex.replace("(", "\\(").replace("/", "\\/").replace(")", "\\)").replace("$", "\\$");
                        String[] test = contents.split(regex);
                        test = test[1].split(" ", 3);
                        String reverseTest = test[2].substring(0, 40);
                        test = test[1].split("m_" + nextSrg + "_");
                        if (reverseTest.split("p_" + nextSrg + "_", 3).length >= 2) {
                            test[0] = test[0].substring(0, test[0].length() - 1);
                        }
                        if (test[0].endsWith("static"))test[0] = test[0].substring(0, test[0].length() - "static".length());

                        accessWidenerOutput.add(new AWEntry("accessible", "method", clazz, test[0], splitSrg[2]).toString());
                        if (accessMod.equals("public-f"))accessWidenerOutput.add(new AWEntry("extendable", "method", clazz, test[0], splitSrg[2]).toString());
                    }
                    if (srgName.startsWith("<init>")) {
                        String result = "method " + clazz + " " + srgName.replace(">", "> ");
                        accessWidenerOutput.add("accessible " + result);
                        if (accessMod.equals("public-f")) accessWidenerOutput.add("extendable " + result);
                    }
                }
                else {
                    String newAccess = "accessible ";
                    if (accessMod.equals("public-f")){
                        newAccess = "extendable ";
                    }
                    newAccess = newAccess.concat("class ");
                    accessWidenerOutput.add(newAccess + clazz);
                }

                line = br.readLine();
            }

        } catch (Exception exception) {
            project.getLogger().error(exception.getMessage());
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            project.getLogger().error(stacktrace);
            project.getLogger().error("[ERROR] Erroring line: " + lineCopy);
        }


        String toWrite = accessWidenerOutput.stream().sorted(String::compareTo).collect(Collectors.joining(System.lineSeparator()));
        try (FileWriter fileWriter = new FileWriter(awFileToOutputIn)) {
            fileWriter.write(toWrite);
            project.getLogger().info(toWrite);
        } catch (IOException exception) {
            project.getLogger().error(exception.getMessage());
        }

        if (sortInput) {
            try {
                List<String> originalFile = Files.readAllLines(atFileToConvert.getAbsoluteFile().toPath());
                try (FileWriter fileWriter = new FileWriter(atFileToConvert)) {
                    String originalContents = originalFile.stream().sorted(String::compareTo).collect(Collectors.joining(System.lineSeparator()));
                    fileWriter.write(originalContents);
                    project.getLogger().error("Successfully sorted input Access Transformer File");
                    return true;
                }
                catch (IOException ioException) {
                    project.getLogger().error(ioException.getMessage());
                }
            }
            catch (IOException ignored) {}

        }
        else {
            return true;
        }

        return false;
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
