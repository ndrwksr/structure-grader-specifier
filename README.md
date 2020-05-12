# Structure Grader - Specifier

## Purpose
This application exists to serve as a more user-friendly way of creating and modifying specification stacks
for user with Structure Grader - Core.

## Installation
These instructions assume that you are using IntelliJ Idea. If that's not the case,
minor changes in procedure are required for providing JavaFX. There should be instructions on
how to add the JavaFX libraries for your specific IDE online.

#### Required Programs:
* [Maven](http://asdf.jkl.com)
* [OpenJFX 13.0.2](https://gluonhq.com/products/javafx/)


First, clone [Structure Grader - Core](https://github.com/ndrwksr/structure-grader-core).
Then, run the Gradle task called publishStructureGraderCorePublicationToMavenLocal.
As the name suggests, this will build Core and publish it to your local maven repository.
This step will be made unnecessary when publication to Maven Central is possible.

Now that Structure Grader - Core is built in your local Maven repository, we can build Structure Grader - Specifier.
Start by cloning [the project from GitHub](https://github.com/ndrwksr/structure-grader-specifier).
Then, assuming you're using IntelliJ Idea, go to
`File -> Project Structure -> Global Libraries` (under "Platform Settings").
Click the `Add` (+) button in the top left, select Java, then browse to wherever you downloaded
your JavaFX SDK to, and select the `/libs` folder.

Lastly, we need to add some VM arguments to the run configuration to ensure that the JDK can find the JFX libraries.
Assuming you're using IntelliJ Idea, go to the `Launcher` class and attempt to run it. This will create a 
new run configuration for you that will start the specifier. Note: This first attempt is expected to fail!
Now that we have generated an incomplete run configuration, edit the configuration by selecting the run config dropdown
and clicking "Edit Configurations...". Under "VM Options", add the following:
```
--module-path YOUR\PATH\TO\JAVAFX\javafx-sdk-13.0.2\lib --add-modules=javafx.controls,javafx.fxml
```
Now, start the Launcher run configuration to run the specifier. 
