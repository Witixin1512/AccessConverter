package witixin.accessconverter;

import org.gradle.api.Project;


/**
 * Converts an AW into an AT
 */
public class ATConverter {

    public static String getPathFromMinecraftVersion(Project project, String version) {
        System.out.println(project.getGradle().getGradleHomeDir());
        System.out.println(project.getGradle().getGradleUserHomeDir());
        //def mcpConfigPath = new File(homePath, "/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config")
        //if (version == "1.18.2") return new File(new File(mcpConfigPath, "1.18.2-20220404.173914"), "srg_to_official_1.18.2.tsrg")
        //if (version == "1.19.2") return new File(new File(mcpConfigPath, "1.19.2-20220805.130853"), "srg_to_official_1.19.2.tsrg")
        //return mcpConfigPath
        return "";
    }
}
