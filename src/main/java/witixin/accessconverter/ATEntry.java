package witixin.accessconverter;

public class ATEntry {
    public String modifier;
    public final String clazz;
    public final String srgName;
    public String signature;

    ATEntry(String modifier, String clazz, String srgName, String signature) {
        this.modifier = modifier;
        this.clazz = clazz;
        this.srgName = srgName;
        this.signature = signature;
    }

    public String toString() {
        return modifier + " "  + clazz + " " + srgName + (srgName.startsWith("m") ? "" : " ") + (srgName.startsWith("f_") ? "" : signature);
    }

    void setModifier(String modifier){
        this.modifier = modifier;
    }
}
