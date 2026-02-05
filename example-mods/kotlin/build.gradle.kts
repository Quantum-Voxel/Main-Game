plugins {
    kotlin("jvm") version "2.2.20"
}

group = "com.example.mod"
version = "0.1.0-alpha.2025.11.03"

dependencies {
    testImplementation(kotlin("test"))

    api(project(":client"))
    api(project(":api:kotlin"))
}

tasks.test {
    useJUnitPlatform()
}