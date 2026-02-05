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

import java.lang.System.getenv
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.1.0"
}

group = "dev.ultreon.qvoxel"
version = property("projectVersion").toString()

val temporal = LocalDateTime.now(ZoneOffset.UTC)
if (property("snapshot")?.toString()?.toBoolean() == true) {
    val format = DateTimeFormatter.ofPattern("YYYY.MM.dd")
        .format(temporal)
    version = "$version-alpha.$format"
}

println("Project Version: $version")

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

dependencies {
    subprojects.map { dokka(it) }
}

dokka {
    moduleName = "Quantum Voxel"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    project.group = rootProject.group
    project.version = rootProject.version

    base {
        archivesName = "${rootProject.name}-${project.name}"
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }

        withSourcesJar()
        withJavadocJar()

        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    dependencies {
        dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:2.1.0")
    }

    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    name = "Fabric"
                    url = uri("https://maven.fabricmc.net/")
                }
            }

            filter {
                includeGroup("net.fabricmc")
            }
        }

        maven {
            name = "luck-repo"
            url = uri("https://repo.lucko.me/")
            content {
                includeGroupAndSubgroups("me.lucko");
                includeGroupAndSubgroups("net.kyori");
            }
        }

        google();

        exclusiveContent {
            forRepository {
                maven {
                    name = "Ultreon Maven Releases"
                    url = uri("https://maven.ultreon.dev/releases")
                }
                maven {
                    name = "Ultreon Maven Snapshots"
                    url = uri("https://maven.ultreon.dev/snapshots")
                }
            }

            filter {
                includeGroup("dev.ultreon")
            }
        }

        mavenCentral()

        maven {
            name = "Sonatype Snapshots"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        maven {
            name = "Sonatype Releases"
            url = uri("https://oss.sonatype.org/content/repositories/releases/")
        }
        maven("https://jitpack.io/")
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        implementation(api("org.jetbrains:annotations:26.0.2-1")!!)
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])

                groupId = project.group.toString()
                artifactId = project.name

                versionMapping {
                    usage("java-api") {
                        fromResolutionOf("runtimeClasspath")
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }


        repositories {
            maven {
                name = "UltreonMavenReleases"
                url = uri("https://maven.ultreon.dev/releases")
                credentials {
                    username = findProperty("ultreonmvn.name") as? String ?: getenv("ULTREON_MVN_NAME")
                    password = findProperty("ultreonmvn.secret") as? String ?: getenv("ULTREON_MVN_SEC")
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }

            maven {
                name = "UltreonMavenSnapshots"
                url = uri("https://maven.ultreon.dev/snapshots")
                credentials {
                    username = findProperty("ultreonmvn.name") as? String ?: getenv("ULTREON_MVN_NAME")
                    password = findProperty("ultreonmvn.secret") as? String ?: getenv("ULTREON_MVN_SEC")
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = project.name

            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }


    repositories {
        maven {
            name = "UltreonMavenReleases"
            url = uri("https://maven.ultreon.dev/releases")
            credentials {
                username = findProperty("ultreonmvn.name") as? String ?: getenv("ULTREON_MVN_NAME")
                password = findProperty("ultreonmvn.secret") as? String ?: getenv("ULTREON_MVN_SEC")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }

        maven {
            name = "UltreonMavenSnapshots"
            url = uri("https://maven.ultreon.dev/snapshots")
            credentials {
                username = findProperty("ultreonmvn.name") as? String ?: getenv("ULTREON_MVN_NAME")
                password = findProperty("ultreonmvn.secret") as? String ?: getenv("ULTREON_MVN_SEC")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Runs the client."

    mainClass.set("net.fabricmc.loader.impl.launch.knot.KnotClient")
    classpath = project(":client").sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
    jvmArgs = listOf("-Xmx4g", "-Xms2g", "-XX:+UseZGC", "-Dfabric.development=true", "-Dfabric.skipMcProvider=true")
}

tasks.register<JavaExec>("runServer") {
    group = "application"
    description = "Runs the server."

    mainClass.set("net.fabricmc.loader.impl.launch.knot.KnotServer")
    classpath = project(":server").sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
    jvmArgs = listOf("-Xmx4g", "-Xms2g", "-XX:+UseZGC", "-Dfabric.development=true", "-Dfabric.skipMcProvider=true")
}
