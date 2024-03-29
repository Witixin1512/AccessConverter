package net.witixin.accessconverter;

import org.gradle.internal.installation.CurrentGradleInstallation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Utils {

    private static final File PATH_TO_GRADLE_HOME = CurrentGradleInstallation.locate().getInstallation().getGradleHome().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();

    public static final Map<String, String> MC_VERSION_TO_MCP = Utils.makeMap(map -> {
        map.put("1.18.2", "20220404.173914");
        map.put("1.19.2", "20220805.130853");
        map.put("1.19.3", "20221207.122022");
        map.put("1.19.4", "20230314.122934");
        map.put("1.20", "20230608.053357");
        map.put("1.20.1", "20230612.114412");
        map.put("1.20.2", "20230921.100330");
        return map;
    });

    public static File getTSRGPath(String mcVersion) {


        File mcpConfigFolder = new File(PATH_TO_GRADLE_HOME, "/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config");
        String mcpConfigVersion = MC_VERSION_TO_MCP.get(mcVersion);
        if (mcpConfigVersion != null) {
            final File returnFile = new File(mcpConfigFolder, mcVersion + "-" + mcpConfigVersion + "/srg_to_official_" + mcVersion + ".tsrg");
            if (returnFile.exists()) return returnFile;
        }
        return null;
        /*
        try {
            //Path file = Requests.INSTANCE.getFile("https://piston-data.mojang.com/v1/objects/42366909cc612e76208d34bf1356f05a88e08a1d/client.txt", Path.of(".", "client.txt"));
            //IMappingFile.load(file.toFile()).rename().write(Path.of(".", "output.txt"), IMappingFile.Format.TSRG2, true);

        }
        catch (IOException ioException) {

        }

         */
   }

    public static File getClientMappings(String mcVersion) {
        final File vanillaGradleCache = new File(PATH_TO_GRADLE_HOME, "VanillaGradle/v2/jars/net/minecraft/client_m-obf-" + mcVersion + "-mappings.txt");
        if (vanillaGradleCache.exists()) return vanillaGradleCache;
        String mcpConfigVersion = MC_VERSION_TO_MCP.get(mcVersion);
        if (mcpConfigVersion == null) throw new NullPointerException("Access Converter does not support that Minecraft Version!");
        File clientMappings = new File(PATH_TO_GRADLE_HOME, "/caches/forge_gradle/minecraft_user_repo/mcp/" + mcVersion + "-" + mcpConfigVersion + "/joined/downloadClientMappings");
        return new File(clientMappings, "client_mappings.txt");
    }

    public static String getFileContents(File fileToRead) {
        return getFileContents(fileToRead, false);
    }

    public static String getFileContents(File fileToRead, boolean appendEndLines) {
        try(BufferedReader br = new BufferedReader(new FileReader(fileToRead))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line.replace("\t", ""));
                if (appendEndLines) sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
        catch (IOException exception) {
            System.out.println((exception.getMessage()));
        }
        return "";
    }

    public static String reverseString(String toReverse) {
        StringBuilder sb = new StringBuilder(toReverse);
        sb.reverse();
        return sb.toString();
    }

    public static <T> T make(Supplier<T> supplier) {
        return supplier.get();
    }

    public static <K, V> Map<K, V> makeMap(Function<Map<K, V>, Map<K, V>> genericFunction) {
        Map<K, V> map = new HashMap<>();
        return genericFunction.apply(map);
    }
}
