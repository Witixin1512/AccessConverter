package witixin.accessconverter;

import org.gradle.api.Project;

import java.io.File;
import java.util.Map;


/**
 * Converts an AW into an AT
 */
public class ATConverter {

    static final Map<String, String> MC_VERSION_TO_MCP = Utils.makeMap(map -> {
        map.put("1.18.2", "20220404.173914");
        map.put("1.19.2", "20220805.130853");
        return map;
    });

    public static File getPathFromMinecraftVersion(Project project, String version) {
        File mcpConfigFolder = new File(project.getGradle().getGradleUserHomeDir(), "/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config");
        return new File(mcpConfigFolder, version + "-" + MC_VERSION_TO_MCP.get(version) + File.pathSeparator +  "srg_to_official_" + version + ".tsrg");
    }


}
