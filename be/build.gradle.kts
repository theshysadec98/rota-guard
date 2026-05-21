plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.rotaguard"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val querydslVersion = "5.1.0"

dependencies {
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
    runtimeOnly("org.postgresql:postgresql")

    annotationProcessor("com.querydsl:querydsl-apt:$querydslVersion:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

sourceSets {
    named("main") {
        java {
            srcDirs("build/generated/sources/annotationProcessor/java/main")
        }
    }
}

spotless {
    java {
        licenseHeaderFile(file("spotless/license-header.java"))
        googleJavaFormat("1.22.0")
        target("src/**/*.java")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

listOf("spotlessApply", "spotlessCheck").forEach { taskName ->
    tasks.named(taskName) {
        doFirst {
            logger.lifecycle("")
            logger.lifecycle("=== Nhom I | $taskName ===")
            logger.lifecycle("")
        }
        doLast {
            if (taskName == "spotlessApply") {
                logger.lifecycle("=== Nhom I | spotlessApply done ===")
            } else {
                logger.lifecycle("=== Nhom I | spotlessCheck OK ===")
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    enabled = false
}

tasks.named("check") {
    dependsOn("spotlessCheck")
}
