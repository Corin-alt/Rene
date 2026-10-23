import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.6"
}

group = "fr.corentin"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.classgraph:classgraph:4.8.162")
    implementation("net.dv8tion:JDA:5.3.2")
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.reflections:reflections:0.10.2")
    implementation("io.github.cdimascio:dotenv-java:3.2.0")
    implementation("org.languagetool:languagetool-core:6.6")
    implementation("org.languagetool:language-fr:6.6")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    archiveFileName.set("${project.name}-${project.version}.jar")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "fr.corentin.rene.Rene"
    }
}