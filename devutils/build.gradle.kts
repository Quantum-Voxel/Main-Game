import com.smushytaco.lwjgl_gradle.Module

plugins {
    java

    id("com.smushytaco.lwjgl3") version "1.0.0"
}

repositories {
    mavenCentral()
}

lwjgl {
    version = "3.4.0-SNAPSHOT"
    usePredefinedPlatforms = true
    api(Module.OPENGL, Module.OPENAL, Module.STB)
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly(project(":client"))
}

tasks.processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version
        )
    }
}

tasks.test {
    useJUnitPlatform()
}