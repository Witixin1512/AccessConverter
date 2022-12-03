package witixin.accessconverter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AccessConverterPlugin implements Plugin<Project> {


    @Override
    public void apply(Project project) {
        project.getExtensions().create("accessConverter", AccessConverterExtension.class);
        project.task("convertAccessWideners").doFirst(new ConvertAccessModifiers());

    }
}