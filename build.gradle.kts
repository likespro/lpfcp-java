/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * From https://github.com/likespro/lpfcp-java
 */

val GROUP = "io.github.likespro" // Until normal group ID - eth.likespro.commons
val VERSION = "1.0.0"
val NAME = "LPFCP Java"
val DESCRIPTION = "LPFCP protocol implementation in Java/Kotlin"
val URL = "https://github.com/likespro/lpfcp-java"
val LICENSES: MavenPomLicenseSpec.(project: Project) -> Unit = { project ->
    license {
        name.set("Mozilla Public License 2.0")
        url.set("https://www.mozilla.org/en-US/MPL/2.0/")
    }
}
val DEVELOPERS: MavenPomDeveloperSpec.() -> Unit = {
    developer {
        id.set("likespro")
        email.set("likespro.eth@gmail.com")
    }
}
val SCM: MavenPomScm.() -> Unit = {
    connection.set("scm:git:git://github.com/likespro/lpfcp-java.git")
    developerConnection.set("scm:git:ssh://github.com:likespro/lpfcp-java.git")
    url.set("https://github.com/likespro/lpfcp-java")
}
val PUBLISHED_ARTIFACTS_PREFIX = "lpfcp-" // Until normal group ID - eth.likespro.commons



/* =================== *
 * AUTOMATIZED SECTION *
 * =================== */



plugins {
    id("java-library")
    kotlin("jvm") version "2.1.21"
    id("jacoco")
    id("org.jetbrains.dokka") version "1.9.0"
    id("maven-publish")
    id("signing")
    id("com.gradleup.nmcp.aggregation").version("0.1.4")
    id("com.gradleup.nmcp").version("0.1.4")
}

allprojects {
    group = GROUP
    version = VERSION

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "jacoco")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "com.gradleup.nmcp")

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    jacoco {
        toolVersion = "0.8.10"
    }
    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.create("documentate") {
        group = "documentation"
        dependsOn(tasks.dokkaHtml)
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                artifactId = PUBLISHED_ARTIFACTS_PREFIX + project.name

                pom {
                    name.set(NAME)
                    description.set(DESCRIPTION)
                    url.set(URL)
                    licenses { LICENSES(project) }
                    developers { DEVELOPERS() }
                    scm { SCM() }
                }
            }
        }

        repositories {
            if(System.getenv("GITHUB_REPO") != null) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/" + System.getenv("GITHUB_REPO"))

                    credentials {
                        username = System.getenv("GITHUB_USERNAME")
                        password = System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }

    if(System.getenv("PGP_PRIVATE_KEY") != null) signing {
        sign(publishing.publications)
        useInMemoryPgpKeys(System.getenv("PGP_PRIVATE_KEY").replace("\\n", "\n")?.trimIndent(), System.getenv("PGP_PRIVATE_KEY_PASSWORD") ?: "")
    }
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

tasks.register<JacocoReport>("jacocoRootReport") {
    dependsOn(subprojects.map { it.tasks.named("test") })

    executionData.setFrom(
        subprojects.map {
            file("${it.buildDir}/jacoco/test.exec")
        }
    )

    // Classes (excluding generated and not-needed classes)
    classDirectories.setFrom(
        subprojects.map {
            files(
                fileTree("${it.buildDir}/classes/kotlin/main") {
                    exclude("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*")
                },
                fileTree("${it.buildDir}/classes/java/main") {
                    exclude("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*")
                }
            )
        }
    )

    // Sources of Kotlin & Java
    sourceDirectories.setFrom(
        subprojects.flatMap {
            listOf(
                file("${it.projectDir}/src/main/kotlin"),
                file("${it.projectDir}/src/main/java")
            )
        }
    )

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}
tasks.build {
    finalizedBy(tasks.named("jacocoRootReport"))
}

tasks.create("documentate") {
    group = "documentation"
    dependsOn(tasks.dokkaHtmlMultiModule)
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("SONATYPE_USERNAME")
        password = System.getenv("SONATYPE_PASSWORD")
        publishingType = "USER_MANAGED"
    }
}
dependencies {
    subprojects.forEach { nmcpAggregation(it) }
}
tasks.named("publish") {
    if(!(version as String).contains("-")) finalizedBy("publishAggregationToCentralPortal")
}

tasks.register("printProjectName") {
    doLast {
        println(rootProject.name)
    }
}
tasks.register("printProjectVersion") {
    doLast {
        println(version)
    }
}