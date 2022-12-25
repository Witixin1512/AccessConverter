package witixin.accessconverter;

import java.util.Arrays;

public record TestMetadata(String modid, String... versions) {

    @Override
    public String toString() {
        return "TestMetadata{" +
                "modid='" + modid + '\'' +
                ", versions=" + Arrays.toString(versions) +
                '}';
    }
}
