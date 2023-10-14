package net.witixin.accessconverter;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import net.witixin.accessconverter.conversion.ATConverter;
import net.witixin.accessconverter.conversion.AWConverter;

public class AccessConverterPlugin implements Plugin<Project> {

    public static final String EXCLUDE_FROM_TRANSFORMATIONS_COMMENT = "#transform_exclude";

    @Override
    public void apply(Project oldProject) {
        oldProject.getExtensions().create("accessConverter", AccessConverterExtension.class);

        oldProject.afterEvaluate(project -> {
            final AccessConverterExtension extension = project.getExtensions().getByType(AccessConverterExtension.class);
            final AccessConverterExtension.ConvertToATExtension atExtension = extension.getATExtension();

            if (atExtension != null) {
                if (ATConverter.convertAW(project.getLogger(), extension.getMcVersion(), atExtension.getAwLocation(), atExtension.getOutputLocation(), atExtension.doSortInput())){
                    project.getLogger().error("Access Widener Conversion into Access Transformer Finished Succesfully.");
                }
                else project.getLogger().error("[ERROR] Access Widener Conversion Failed");

            }

            final AccessConverterExtension.ConvertToAWExtension awExtension = extension.getAWExtension();

            if (awExtension != null) {
                if (AWConverter.convertAT(project.getLogger(), extension.getMcVersion(), awExtension.getAwLocation(), awExtension.getAtLocation(), awExtension.doSortInput())) {
                    project.getLogger().error("Access Transformer Conversion into Access Widener Finished Succesfully.");
                }
                else project.getLogger().error("[ERROR] Access Transformer Conversion Failed");
            }
        });

    }
}