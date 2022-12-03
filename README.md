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
        fileToConvert project(':Common').file("/src/main/resources/${modid}.accesswidener")
        fileOutput project(":Forge").file("/src/main/resources/META-INF/accesstransformer.cfg")
    }
    //Does the opposite, converts an AT into AW.
    convertAT {
        
    }
}
```
