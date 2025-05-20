plugins {
    id("java")
    kotlin("jvm") version "2.1.21"
}

allprojects {
    group = "io.github.likespro"
    version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
}

tasks.register<Copy>("collectAllJars") {
    into(layout.buildDirectory.dir("libs"))
    subprojects.forEach { proj ->
        dependsOn(proj.tasks.named("jar"))
        from(proj.tasks.named<Jar>("jar").flatMap { it.archiveFile })
    }
}
tasks.jar {
    enabled = false
    dependsOn("collectAllJars")
}