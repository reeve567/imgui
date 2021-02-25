import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    java
    kotlin("jvm") version "1.4.0"
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
//    idea
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "com.github.kotlin_graphics"
    val moduleName = "$group.imgui.${project.name}"

    tasks.compileJava {
        // this is needed because we have a separate compile step in this example with the 'module-info.java' is in 'main/java' and the Kotlin code is in 'main/kotlin'
        options.compilerArgs = listOf("--patch-module", "$moduleName=${sourceSets.main.get().output.asPath}")
    }

    java { modularity.inferModulePath.set(true) }

    dependencies {

        implementation(kotlin("stdlib-jdk8"))

        implementation(platform("org.lwjgl:lwjgl-bom:${findProperty("lwjglVersion")}"))

        testImplementation("io.kotest:kotest-runner-junit5-jvm:${findProperty("kotestVersion")}")
        testImplementation("io.kotest:kotest-assertions-core-jvm:${findProperty("kotestVersion")}")
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }

    tasks {

        dokkaHtml {
            enabled = System.getenv("JITPACK") != "true"
            dokkaSourceSets.configureEach {
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/kotlin-graphics/glm/tree/master/src/main/kotlin"))
                    remoteLineSuffix.set("#L")
                }
            }
        }

        dokkaJavadoc { enabled = System.getenv("JITPACK") != "true" }

        withType<KotlinCompile>().all {
            kotlinOptions {
                jvmTarget = "11"
                freeCompilerArgs += listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
            }
            sourceCompatibility = "11"
        }

        withType<Test> { useJUnitPlatform() }

//        task lightJar (type: Jar) {
//            archiveClassifier = 'light'
//            from sourceSets . main . output
//                    exclude 'extraFonts'
//            inputs.property("moduleName", moduleName)
//            manifest {
//                attributes('Automatic-Module-Name': moduleName)
//            }
//            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//        }
    }

    val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
        enabled = System.getenv("JITPACK") != "true"
        dependsOn(tasks.dokkaJavadoc)
        from(tasks.dokkaJavadoc.get().outputDirectory.get())
        archiveClassifier.set("javadoc")
    }

    val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
        enabled = System.getenv("JITPACK") != "true"
        dependsOn(tasks.dokkaHtml)
        from(tasks.dokkaHtml.get().outputDirectory.get())
        archiveClassifier.set("html-doc")
    }

    artifacts {
        archives(dokkaJavadocJar)
        archives(dokkaHtmlJar)
    }

    publishing {
        publications.create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group as String
            artifactId = moduleName
        }
        repositories.maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kotlin-graphics/imgui")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    java.withSourcesJar()

    // == Add access to the 'modular' variant of kotlin("stdlib"): Put this into a buildSrc plugin and reuse it in all your subprojects
    configurations.all { attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 11) }
}