# 🎮️ Game Controller Extension for libGDX, Version 2

Use game controllers with ease in your libGDX games.

[📖️ Documentation](https://github.com/libgdx/gdx-controllers/wiki) - [🎁️ Feature overview](https://github.com/libgdx/gdx-controllers/wiki/Features)

[🚀️ Migration guide from v1](https://github.com/libgdx/gdx-controllers/wiki/Migrate-from-v1)

## 💾️ Installation

The recommended way to use gdx-pay is via dependency management with Gradle or Maven. Artifacts are available in
[Snapshot Repository](https://oss.sonatype.org/content/repositories/snapshots/com/badlogicgames/gdx-controllers/) and Maven Central.

[![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/com.badlogicgames.gdx-controllers/gdx-controllers-core?nexusVersion=2&server=https%3A%2F%2Foss.sonatype.org&label=release)](https://search.maven.org/artifact/com.badlogicgames.gdx-controllers/gdx-controllers-core)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.badlogicgames.gdx-controllers/gdx-controllers-core?server=https%3A%2F%2Foss.sonatype.org&label=snapshot)](https://oss.sonatype.org/#nexus-search;gav~com.badlogicgames.gdx-controllers~gdx-controllers-core)

*project-root/build.gradle:*

    ext {
        gdxControllersVersion = '2.0.0'
    }

Add the following dependencies:

#### core:
```
implementation "com.badlogicgames.gdx-controllers:gdx-controllers-core:$gdxControllersVersion"
```
#### desktop:
```
implementation "com.badlogicgames.gdx-controllers:gdx-controllers-desktop:$gdxControllersVersion"
```
#### android:
```
implementation "com.badlogicgames.gdx-controllers:gdx-controllers-android:$gdxControllersVersion"
```
Proguard setting:
```
-keep class com.badlogic.gdx.controllers.android.AndroidControllers { *; }
```

#### ios:
```
implementation "com.badlogicgames.gdx-controllers:gdx-controllers-ios:$gdxControllersVersion"
```
`robovml.xml` needs the following lines added to `forceLinkClasses` and `frameworks`:
```
<pattern>com.badlogic.gdx.controllers.IosControllerManager</pattern> 
....
	<framework>GameKit</framework>
```
If you forget to explicitly link GameKit framework, no game controller will show up.

#### html:
```
implementation "com.badlogicgames.gdx-controllers:gdx-controllers-core:$gdxControllersVersion:sources"
implementation "com.badlogicgames.gdx-controllers:gdx-controllers-gwt:$gdxControllersVersion:sources"
```
You also need to add the following file to your GdxDefinition.gwt.xml in your html project:
```
<inherits name="com.badlogic.gdx.controllers" />
<inherits name="com.badlogic.gdx.controllers.controllers-gwt"/>
```

### Building from source
To build from source, clone or download this repository, then open it in Android Studio. Perform the following command to compile and upload the library in your local repository:

    gradlew clean uploadArchives -PLOCAL=true
    
See `build.gradle` file for current version to use in your dependencies.

## 🤝️ News & Community

You can get help on the [libgdx discord](https://discord.gg/6pgDK9F).

## License

The project is licensed under the Apache 2 License, meaning you can use it free of charge, without strings attached in commercial and non-commercial projects. We love to get (non-mandatory) credit in case you release a game or app using this project!
