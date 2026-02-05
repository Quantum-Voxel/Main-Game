import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.+"
}

group = "dev.ultreon.qvoxel"

base {
    archivesName.set("qvoxel-api-kotlin")
}

repositories {
    mavenCentral()
}

sourceSets {
    create("client") {
        compileClasspath += sourceSets.main.get().compileClasspath
    }
    main {
        runtimeClasspath += sourceSets["client"].output
    }
}

dependencies {
    testImplementation(kotlin("test"))

    api(project(":client"))
    api("net.fabricmc:fabric-language-kotlin:1.13.7+kotlin.2.2.21")
}

kotlin {
    jvmToolchain(25)
}

tasks.compileKotlin {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
}

tasks.test {
    useJUnitPlatform()
}