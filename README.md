# AccessConverter
A Gradle Plugin for automatically converting from Access Wideners to Access Transformers and viceversa.

Apply the plugin the following way:

```groovy
plugins {
    id 'witixin.accessconverter' version '1.0.+'
}

accessConverter {
    mcVersion "1.19.2"
    //Converts the AccessWidener found at the fileToConvert location into an accesstransformer dropped into the fileToOutput location.
    convertAW {
        //optional, sorts the input file
        sortInput true
        fileToConvert project(':Common').file("/src/main/resources/${modid}.accesswidener")
        fileOutput project(":Forge").file("/src/main/resources/META-INF/accesstransformer.cfg")
    }
    //Does the opposite, converts an AT into AW.
    convertAT {
        //optional sorts the input file
        convertFrom rootProject.file("testtransformer.cfg")
        saveConversionTo rootProject.file("outputwidener.accesswidener")
        sortInput true
    }
    //It is not recommended to have both configurations enabled nor to have one of them depend on the files of the other one.
    //Keep it mind convertAW is fired first and then convertAT
}
```

Run the `convertAccessFiles` task after configuring the plugin and you're set to go!
