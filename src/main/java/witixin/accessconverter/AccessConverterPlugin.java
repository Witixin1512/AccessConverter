package witixin.accessconverter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AccessConverterPlugin implements Plugin<Project> {

    public static final String EXCLUDE_FROM_TRANSFORMATIONS_COMMENT = "#transform_exclude";

    @Override
    public void apply(Project project) {
        project.getExtensions().create("accessConverter", AccessConverterExtension.class);
        project.task("convertAccessFiles").doFirst(new ConvertAccessModifiers());

    }
}