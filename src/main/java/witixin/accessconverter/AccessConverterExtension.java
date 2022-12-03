package witixin.accessconverter;

import org.gradle.api.Action;

import java.io.File;

public class AccessConverterExtension {
    private ConvertToATExtension atExtension = new ConvertToATExtension();
    private ConvertToAWExtension awExtension = new ConvertToAWExtension();
    private String mcVersion;

    public ConvertToATExtension getATExtension() {
        return atExtension;
    }

    public void atExtension(Action<? super ConvertToATExtension> action) {
        action.execute(atExtension);
    }

    public ConvertToAWExtension getAWExtension() {
        return awExtension;
    }

    public void awExtension(Action<? super ConvertToAWExtension> action) {
        action.execute(awExtension);
    }

    public String getMcVersion() {
        return mcVersion;
    }

    public void mcVersion(String mcVersion) {
        this.mcVersion = mcVersion;
    }

    public static class ConvertToATExtension {
        //Takes in a file as an input, converts to AT
        private File awLocation;

        public File getAwLocation() {
            return awLocation;
        }

        public void awLocation(File file) {
            this.awLocation = file;
        }
    }


    public static class ConvertToAWExtension {
        //Takes in an AT and saves it in the given location.
        private File awLocation;

        public File getAwLocation() {
            return awLocation;
        }

        public void awLocation(File file) {
            this.awLocation = file;
        }
    }
}
