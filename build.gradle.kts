plugins {
    kotlin("jvm") version "2.0.0"
    application
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("com.diffplug.spotless") version "6.25.0"
    id("org.openjfx.javafxplugin") version "0.0.10"
}

group = "tools.aqua"

version = "0.5"

repositories {
    mavenCentral()
    jcenter()
}

var starsVersion = "0.5"

dependencies {
    testImplementation(kotlin("test"))
    implementation(group = "tools.aqua", name = "stars-core", version = starsVersion)
    implementation(group = "tools.aqua", name = "stars-logic-kcmftbl", version = starsVersion)
    implementation(group = "tools.aqua", name = "stars-data-av", version = starsVersion)
    implementation(group = "tools.aqua", name = "stars-importer-carla", version = starsVersion)
    implementation(group = "com.github.ajalt.clikt", name = "clikt", version = "4.4.0")
    implementation("com.google.code.gson:gson:2.8.9")

    // GUI Dependencies:
    implementation("org.openjfx:javafx-controls:17.0.0.1:win")
    implementation("org.openjfx:javafx-graphics:17.0.0.1:win")
    implementation("org.openjfx:javafx-fxml:17.0.0.1:win")
    implementation("no.tornado:tornadofx:1.7.20")  // Optional: Für TornadoFX

    implementation("com.github.vlsi.mxgraph:jgraphx:4.2.2")

    implementation("guru.nidi:graphviz-java:0.18.1")

    implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
}

tasks.test {
    useJUnitPlatform()
}

application { mainClass.set("tools.aqua.stars.carla.experiments.gui.MainKt") }

kotlin {
    jvmToolchain(17)
}