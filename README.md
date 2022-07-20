cASpER was originally an Intellij plugin used to detect and automatically refactor certain code smells. The proposed version is 
a reengineered version of the original that works as Maven plugin. At the current stage, the cASpER Maven plugin version only holds the ability to 
detect the four original code smells it set out to detect and automatically refactor:
- `Blob`
- `Misplaced class` 
- `Promiscuos Package`
- `Feature Envy`

## How to install cASpER Maven plugin

To use cASpER it is essential to have on your device Maven 3.8.3 and insert the following plugin configuration 
in your project pom.xml:
   

```
	<plugin>
            <groupId>com.iges.project</groupId>
            <artifactId>casper-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
                <dump></dump>
                <textContent>true</textContent>
                <dependency>
                    <dipPromiscuous>50</dipPromiscuous>
                    <dipPromiscuous2>50</dipPromiscuous2>
                    <dipFeature>0</dipFeature>
                    <dipMisplaced>0</dipMisplaced>
                    <dipBlob>350</dipBlob>
                    <dipBlob2>20</dipBlob2>
                    <dipBlob3>500</dipBlob3>
                </dependency>
                <cosine>
                    <cosenoPromiscuous>0.5</cosenoPromiscuous>
                    <cosenoFeature>0.0</cosenoFeature>
                    <cosenoMisplaced>0.0</cosenoMisplaced>
                    <cosenoBlob>0.5</cosenoBlob>
                </cosine>
            </configuration>
        </plugin>
```

The dump variable accepts a filename and when specified allows to dump what would normally be printed on the sout in a file with the filename specified.
textContent variable is a boolean flag that when set to true tells the plugin to dump also the text content of a smelly component.
the dependency and cosine maps hold the thresholds the detection algorithms have to match (or surpass) to dictate that a component is affected by a certain smell. At present, it is necessary to specify them all.
## How to see the source code

To view the open source code, download the .zip file from this repository. Open the project by accessing the "casper-maven-plugin" subfolder as a Maven Project and selecting "java" as the source foulder.
To try and execute in a local environment you must again install the plugin via the command "mvn install -DskipTests"