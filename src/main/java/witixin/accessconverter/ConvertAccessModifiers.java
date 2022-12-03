package witixin.accessconverter;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;

public class ConvertAccessModifiers implements Action<Task> {
    @Override
    public void execute(Task task) {
        Project project = task.getProject();
        AccessConverterExtension extension = project.getExtensions().getByType(AccessConverterExtension.class);

        if (extension.getATExtension() != null) {
            System.out.println("Extension isn't null!");
            ATConverter.getPathFromMinecraftVersion(project, extension.getMcVersion());
        }

        if (extension.getAWExtension() != null) {

        }
    }
}
