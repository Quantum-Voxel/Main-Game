plugins {
    scala
}

group = "com.example.mod"

dependencies {
    api(project(":client"))
    api("org.scala-lang:scala3-library_3:3.+")
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_25
}
