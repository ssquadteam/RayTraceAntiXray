plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

group = "com.vanillage.raytraceantixray"
version = "1.17.2"
description = "RayTraceAntiXray"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/") // Kyori repository
}

configurations.all {
    resolutionStrategy.cacheDynamicVersionsFor(10, "minutes")
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.17.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")
    implementation("net.kyori:adventure-text-serializer-plain:4.17.0")
    implementation("net.kyori:adventure-text-serializer-ansi:4.17.0")
    
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))

    // Upstream branch was a multi-module maven project
    sourceSets.getByName("main") {
        java.srcDir("RayTraceAntiXray/src/main/java")
        resources.srcDir("RayTraceAntiXray/src/main/resources")
    }
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
