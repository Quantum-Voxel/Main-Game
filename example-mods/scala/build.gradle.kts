plugins {
    scala
}

group = "com.example.mod"

dependencies {
    api(project(":client"))
    api("org.scala-lang:scala3-library_3:3.3.1")
}

tasks.test {
    useJUnitPlatform()
}