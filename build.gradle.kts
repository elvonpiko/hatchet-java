plugins {
    // no java plugin for the parent
}

allprojects {
    group = "io.hatchet"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.apply("java-library")
    plugins.apply("maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<PublishToMavenLocal> {
        dependsOn("build")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
