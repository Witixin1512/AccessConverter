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

    public void convertAW(Action<? super ConvertToATExtension> action) {
        action.execute(atExtension);
    }

    public ConvertToAWExtension getAWExtension() {
        return awExtension;
    }

    public void convertAT(Action<? super ConvertToAWExtension> action) {
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
        private File outputLocation;

        public File getAwLocation() {
            return awLocation;
        }

        public void fileToConvert(File file) {
            this.awLocation = file;
        }

        public File getOutputLocation() {
            return outputLocation;
        }

        public void fileOutput(File file) {
            this.outputLocation = file;
        }
    }


    public static class ConvertToAWExtension {
        //Takes in an AT and saves it in the given location.
        private File awLocation;
        private File atLocation;

        public File getAwLocation() {
            return awLocation;
        }

        public void saveConversionTo(File file) {
            this.awLocation = file;
        }

        public File getAtLocation() {
            return atLocation;
        }

        public void convertFrom(File file) {
            this.atLocation = file;
        }
    }
}
