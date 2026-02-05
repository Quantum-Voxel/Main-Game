/*
 * Copyright 2025. Quinten 'Qubix' Jungblut
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.smushytaco.lwjgl_gradle.Module
import io.github.fourlastor.construo.Target
import io.github.fourlastor.construo.task.jvm.RoastTask
import io.github.fourlastor.construo.task.macos.GeneratePlist
import kotlinx.serialization.Serializable

plugins {
    id("java")
    id("io.github.fourlastor.construo") version "2.0.1"
    id("com.smushytaco.lwjgl3") version "1.0.0"
}
val fabricVersion by properties

val mixinVersion by properties
configurations {
    implementation.get().extendsFrom(api.get())
    runtimeClasspath.get().extendsFrom(api.get())
}

lwjgl {
    version = "3.4.0-SNAPSHOT"
    usePredefinedPlatforms = true
    api(Module.OPENGL, Module.OPENAL, Module.STB, Module.ASSIMP, Module.MESHOPTIMIZER)
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    api(project(":server"))
    runtimeOnly(project(":devutils"))

    api("net.java.dev.jna:jna:5.18.0")
    api("net.java.dev.jna:jna-platform:5.18.0")
    api("com.github.oshi:oshi-core:6.9.0")

    // ImGui
    api("io.github.spair:imgui-java-lwjgl3:1.90.0")
    api("io.github.spair:imgui-java-natives-linux:1.90.0")
    api("io.github.spair:imgui-java-natives-macos:1.90.0")
    api("io.github.spair:imgui-java-natives-windows:1.90.0")

    // Fabric Loader
    api("net.fabricmc:sponge-mixin:$mixinVersion")

    api("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    api("net.fabricmc:tiny-remapper:0.12.0")
    api("net.fabricmc:dev-launch-injector:0.2.1+build.8")
    api("net.fabricmc:access-widener:2.1.0")

    api("org.apache.commons:commons-lang3:3.19.0")
    api("org.apache.commons:commons-collections4:4.5.0")
    api("org.apache.commons:commons-math3:3.6.1")
    api("org.apache.commons:commons-text:1.10.0")
    api("org.apache.commons:commons-compress:1.26.0")
    api("commons-io:commons-io:2.15.1")
    api("commons-codec:commons-codec:1.16.0")

    api("net.fabricmc:fabric-loader:$fabricVersion")

    api("io.github.llamalad7:mixinextras-fabric:0.4.1")

    // Misc. dependencies
    api("org.slf4j:slf4j-api:2.0.17")
    api("org.joml:joml:1.10.8")
    api("it.unimi.dsi:fastutil:8.5.16")

    api("com.github.JnCrMx:discord-game-sdk4j:v1.0.0")

    api("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-windows-arm64")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-windows-x86")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-freebsd")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-linux")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-linux-arm32")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-linux-arm64")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-linux-ppc64le")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-linux-riscv64")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-macos")
    runtimeOnly("org.lwjgl:lwjgl-sdl:3.4.0-SNAPSHOT:natives-macos-arm64")

    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
}

tasks.test {
    useJUnitPlatform()
}

construo {
    mainClass = "net.fabricmc.loader.impl.launch.knot.KnotClient"
    name = "QuantumVoxel"
    version = rootProject.version.toString()
    humanName = "Quantum Voxel"
    outputDir = file("build/dist")
    roast.runOnFirstThread = true
    roast.useZgc = true
    roast.vmArgs.addAll("-Xmx6g", "-Xms6g")
    jlink.modules.addAll("java.base", "java.logging", "java.xml", "java.desktop", "java.sql", "java.management", "java.instrument", "java.compiler", "java.management.rmi", "java.rmi", "java.security.jgss", "java.security.sasl", "java.smartcardio", "jdk.unsupported", "jdk.zipfs", "jdk.jfr")
    jlink.guessModulesFromJar = false

    targets {
        register<Target.Windows>("windowsX64") {
            this.jdkUrl = "https://cdn.azul.com/zulu/bin/zulu25.28.85-ca-jdk25.0.0-win_x64.zip"
            this.architecture = Target.Architecture.X86_64
            this.useGpuHint = true
            this.icon = file("package/icon.ico")
        }
        register<Target.Windows>("windowsAarch64") {
            this.jdkUrl = "https://cdn.azul.com/zulu/bin/zulu25.28.85-ca-jdk25.0.0-win_aarch64.zip"
            this.architecture = Target.Architecture.AARCH64
            this.useGpuHint = true
            this.icon = file("package/icon.ico")
        }
        register<Target.Linux>("linuxX64") {
            this.jdkUrl = "https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25%2B36/OpenJDK25U-jdk_x64_linux_hotspot_25_36.tar.gz"
            this.architecture = Target.Architecture.X86_64
        }
        register<Target.Linux>("linuxAarch64") {
            this.jdkUrl = "https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25%2B36/OpenJDK25U-jdk_aarch64_linux_hotspot_25_36.tar.gz"
            this.architecture = Target.Architecture.AARCH64
        }
    }
}

tasks.register("package") {
    group = "native-build"
    description = "Packages the client for all Construo targets."

    dependsOn("packageWindowsX64", "packageWindowsAarch64", "packageLinuxX64", "packageLinuxAarch64")

    doLast {
        println("Client has been successfully built for all targets.")
    }
}

tasks.withType<RoastTask> roast@ {
    dependsOn(rootProject.subprojects.map { it.tasks.named("build") })

    doLast {
        val configDir = file(output).resolve("app").apply { mkdirs() }
        val jarsDir = file(output).resolve("jars").apply { mkdirs() }
        val files = sourceSets.main.get().runtimeClasspath.files
        for (file in files) {
            if (file.isDirectory) continue
            file.copyTo(jarsDir.resolve(file.name), true)
        }
        for (file in tasks.jar.get().outputs.files.files) {
            file.copyTo(file(output).resolve(file.name), true)
        }

        File(configDir, "${appName.get()}.json").writer().buffered().use {
            it.write(
                """
                    {
                        "mainClass": "${mainClassName.get()}",
                        "runOnFirstThread": ${runOnFirstThread.get()},
                        "useZgcIfSupportedOs": ${useZgc.get()},
                        "useMainAsContextClassLoader": ${useMainAsContextClassLoader.get()},
                        "vmArgs": [${vmArgs.get().joinToString(", ") { "\"${it.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")}\"" }}],
                        "classPath": [
                            ${jarsDir.listFiles().joinToString(", ") { "\"jars/${it.name}\"" }}, ${tasks.jar.get().outputs.files.files.joinToString(", ") { "\"${it.name}\"" }}                            
                        ]
                    }
                """.trimIndent()
            )
        }
    }
}

tasks.withType<GeneratePlist> {
    this.identifier = "dev.ultreon.qvoxel.QuantumVoxel"
}


@Serializable
data class PackConfig(
    val classPath: List<String>,
    val mainClass: String,
    val runOnFirstThread: Boolean,
    val useZgcIfSupportedOs: Boolean,
    val useMainAsContextClassLoader: Boolean,
    val vmArgs: List<String>
)

tasks.jar {
    manifest {
        attributes(
            "QuantumVoxel-Dist" to "client",
            "QuantumVoxel-Dists" to "client server"
        )
    }
}
