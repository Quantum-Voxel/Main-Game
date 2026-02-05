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

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    api(project(":logging"))

    api("net.fabricmc:sponge-mixin:$mixinVersion") {
        exclude(null, "launchwrapper")
        exclude(null, "guava")
    }

    api("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    api("net.fabricmc:tiny-remapper:0.12.0")
    api("net.fabricmc:dev-launch-injector:0.2.1+build.8")
    api("net.fabricmc:access-widener:2.1.0")

    api("net.fabricmc:fabric-loader:$fabricVersion")

    api("org.slf4j:slf4j-api:2.0.17")

    api("org.joml:joml:1.10.8")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<ProcessResources> {
    inputs.property("quantum_version", rootProject.version)
    
    filesMatching("versions.properties") {
        expand(
            "quantum_version" to rootProject.version
        )
    }
}
