plugins {
    application
    java
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.rotaguard"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("com.rotaguard.jdbc.Main")
}

repositories {
    mavenCentral()
}

sourceSets {
    named("main") {
        java.setSrcDirs(listOf("src/main/java"))
    }
    named("test") {
        java.setSrcDirs(emptyList<String>())
        resources.setSrcDirs(emptyList<String>())
    }
}

dependencies {
    runtimeOnly("com.mysql:mysql-connector-j:8.4.0")
}

spotless {
    java {
        licenseHeaderFile(file("spotless/license-header.java"))
        googleJavaFormat("1.22.0")
        target("src/main/java/**/*.java")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

tasks.withType<Test>().configureEach {
    enabled = false
}

tasks.named("compileTestJava") {
    enabled = false
}

tasks.named("processTestResources") {
    enabled = false
}

tasks.named("check") {
    dependsOn("spotlessCheck")
}
