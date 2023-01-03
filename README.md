# AccessConverter
A Gradle Plugin for automatically converting from Access Wideners to Access Transformers and viceversa.

Apply the plugin the following way, ideally in your rootProject buildscript, outside the `subprojects` block:

```groovy
plugins {
    id 'witixin.accessconverter' version '1.0.+'
}

accessConverter {
    mcVersion "1.19.2"
    //Converts the AccessWidener found at the fileToConvert location into an accesstransformer dropped into the fileToOutput location.
    convertAW {
        //optional, sorts the input file
        it.sortInput(true)
        it.fileToConvert(project(':Common').file("/src/main/resources/${modid}.accesswidener"))
        it.fileOutput(project(":Forge").file("/src/main/resources/META-INF/accesstransformer.cfg"))
    }
    //Does the opposite, converts an AT into AW.
    convertAT {
        //optional sorts the input file
        it.convertFrom(rootProject.file("testtransformer.cfg"))
        it.saveConversionTo(rootProject.file("outputwidener.accesswidener"))
        it.sortInput(true)
    }
    //It is not recommended to have both configurations enabled nor to have one of them depend on the files of the other one.
    //Keep it mind convertAW is fired first and then convertAT
}
```

The plugin requires java 17 and only supports MC 1.18.2 and above!

As of 1.0.0.+, you NEED to import a Forge Workspace (so it caches required files) with official mappings (parchment won't work) BEFORE refreshing your MultiLoader workspace!