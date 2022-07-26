plugins {
    java

    id("io.freefair.lombok") version "6.5.0.3"
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")

    compileOnly("net.kyori:adventure-text-minimessage:4.11.0")
    compileOnly("com.electronwill.night-config:toml:3.6.5")
}

tasks {
    jar {
        from("LICENSE")
    }
}

group = "com.github.ipddev.velocitywhitelist"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}