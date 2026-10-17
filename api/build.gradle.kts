plugins {
    id("java-library")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

repositories {
    mavenCentral()
}

dependencies {
    // Spigot API
    compileOnly("org.spigotmc:spigot-api:26.2-R0.1-SNAPSHOT")

    compileOnlyApi("net.kyori:adventure-api:4.26.1")
    compileOnlyApi("net.kyori:adventure-text-minimessage:4.26.1")
    compileOnlyApi("it.unimi.dsi:fastutil:8.5.18")

    // Annotations
    compileOnlyApi("org.jetbrains:annotations:26.1.0")

    // NoteBlockAPI
    compileOnly("com.github.FilleDev:NoteBlockAPI:1.7.0")
}
