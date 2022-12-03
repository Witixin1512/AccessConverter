package witixin.accessconverter;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import witixin.accessconverter.conversion.ATConverter;
import witixin.accessconverter.conversion.AWConverter;

public class ConvertAccessModifiers implements Action<Task> {
    @Override
    public void execute(Task task) {
        Project project = task.getProject();
        AccessConverterExtension extension = project.getExtensions().getByType(AccessConverterExtension.class);

        if (extension.getATExtension() != null) {
            if (!ATConverter.convertAW(project, extension.getMcVersion(), extension.getATExtension().getAwLocation(), extension.getATExtension().getOutputLocation())){
                project.getLogger().error("AW Conversion Failed");
            }
        }

        if (extension.getAWExtension() != null) {
            if (!AWConverter.convertAW(project, extension.getMcVersion(), extension.getAWExtension().getAwLocation(), extension.getAWExtension().getAtLocation())) {
                project.getLogger().error("AT Conversion Failed");
            }
        }
    }
}
