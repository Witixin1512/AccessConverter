package witixin.accessconverter.conversion;

import org.gradle.api.Project;

import java.io.File;

public class AWConverter {
    public static boolean convertAW(Project project, String version, File awFileToOutputIn, File atFileToConvert) {
        if (atFileToConvert == null){
            project.getLogger().error("Null at file provided, unable to convert");
            return false;
        }
        return false;
    }
}
