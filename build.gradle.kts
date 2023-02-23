plugins {
    java

    id("io.freefair.lombok") version "6.5.0.3"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()

    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    annotationProcessor("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.1.2-SNAPSHOT")

    compileOnly("net.kyori:adventure-text-minimessage:4.11.0")
    compileOnly("com.electronwill.night-config:toml:3.6.5")

    implementation("org.enginehub:squirrelid:0.3.0")
}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    shadowJar {
        from("LICENSE")
    }
}

group = "com.github.ipddev.velocitywhitelist"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}