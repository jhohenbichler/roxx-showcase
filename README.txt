
ROXX - Getting started



UI (Adobe Flex)
--------------------

Basic needs
o Flash player (if you want to debug the UI you better install the debugging version of the flash player). Download: http://get.adobe.com/de/flashplayer/

Note: If you don't want to play with the UI-code you could also use the compiled swf out of the folder roxx-flash/build-tools/build and skip the steps above.

Needed for Development
o Java jdk installed
o An Eclispe Distribution (for editing xml and for running ant) (tested with 3.x Versions). http://eclipse.org/
o Flex SDK 4.1. http://opensource.adobe.com/wiki/display/flexsdk/Flex+SDK

1. Download and unpack Flex SDK 4.1 to some location and change the path pointing to it in the file roxx-flash/build-tools/build.xml
2. Start Eclipse and create a new empty workspace by pointing to an empty folder when you are prompted for it at the startup
3. Import the roxx-flash folder as project into a new workpsace. Do not copy files, just link to them.
4. Run build-tools/build.xml by clicking it right and chose "run as ant build"
5. You should get the compiled roxx.swf together with an embedding index.html file in the output folder: roxx-flash/build-tools/build
6. Try to start the app with index.html you should get some error because you dont have mongo-db and the server running


Server (Scala)
---------------------

Needed for Development
o Java jdk installed and JAVA_HOME correctly set
o sbt version 0.7.6.RC0 or higher. http://code.google.com/p/simple-build-tool/
o Mongo-DB. http://www.mongodb.org/
o Optional: IntelliJ http://www.jetbrains.com/idea/

1. Download, install and start mongo-DB
2. Download/install sbt. Help could be found here: http://code.google.com/p/simple-build-tool/wiki/Setup
3. Run sbt for the project roxx and execute: update
4. In sbt execute: "compile" to check if everything is working
5. Optional: In sbt execute: "idea" after that you should be able to open the workspace with IntelliJ
6. Start another sbt instance and execute: jetty-run
7. Start another sbt instance and execute: ~prepare-webapp
8. After that you have at least two sbt-consoles online that will continously compile and deploy your code changes as soon as you save the file
9. Open the UI by clicking index.html (or if you have Flash-Builder installed by running the Main.mxml via right mouse click and run as...)
10. Now everything should work - Have fun!