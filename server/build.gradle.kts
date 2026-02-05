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

plugins {
    id("java")
}

val fabricVersion by properties
val mixinVersion by properties
val mixinExtrasVersion by properties

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    api(project(":gameprovider"))

    api("me.lucko:spark-api:0.1-SNAPSHOT")
    api("me.lucko:spark-common:1.10.142-SNAPSHOT")

    api("dev.ultreon.corelibs:resources-v0:0.3.0")
    api("dev.ultreon.corelibs:crash-v0:0.3.0")
    api("dev.ultreon.corelibs:commons-v0:0.3.0")
    api("dev.ultreon.corelibs:collections-v0:0.3.0")
    api("dev.ultreon.corelibs:registries-v0:0.3.0")
    api("dev.ultreon.corelibs:translations-v1:0.3.0")
    api("dev.ultreon.corelibs:events-v0:0.3.0")
    api("dev.ultreon:ubo:1.6.0-SNAPSHOT")
    api("de.articdive:jnoise-pipeline:4.1.0")

    api("org.joml:joml:1.10.8")

    api("org.slf4j:slf4j-api:2.0.17")

    api("com.github.oshi:oshi-core:6.9.0")

    api("net.fabricmc:sponge-mixin:$mixinVersion") {
        exclude(null, "launchwrapper")
        exclude(null, "guava")
    }

    api("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    api("net.fabricmc:tiny-remapper:0.12.0")
    api("net.fabricmc:dev-launch-injector:0.2.1+build.8")
    api("net.fabricmc:access-widener:2.1.0")

    api("net.fabricmc:fabric-loader:$fabricVersion")
    
    api("org.apache.commons:commons-collections4:4.5.0")
    api("org.apache.commons:commons-lang3:3.19.0")
    api("org.apache.commons:commons-math3:3.6.1")
    api("org.apache.commons:commons-text:1.14.0")

    api("io.github.llamalad7:mixinextras-fabric:$mixinExtrasVersion")

    api("it.unimi.dsi:fastutil:8.5.16")

    // Netty Network
    api("io.netty:netty-all:4.2.7.Final")
    
    api("io.github.spair:imgui-java-binding:1.88.0")

    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes(
            "QuantumVoxel-Dist" to "server",
            "QuantumVoxel-Dists" to "client server"
        )
    }
}
