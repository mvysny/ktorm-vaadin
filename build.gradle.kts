import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20"
    `maven-publish`
    java
    signing
}

defaultTasks("clean", "build")

allprojects {
    group = "com.gitlab.mvysny.jdbiormvaadin"
    version = "1.5-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_17
    }
}

subprojects {
    apply {
        plugin("maven-publish")
        plugin("kotlin")
        plugin("org.gradle.signing")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // creates a reusable function which configures proper deployment to Maven Central
    ext["publishing"] = { artifactId: String ->
        java {
            withJavadocJar()
            withSourcesJar()
        }

        tasks.withType<Javadoc> {
            isFailOnError = false
        }

        publishing {
            repositories {
                maven {
                    setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = project.properties["ossrhUsername"] as String? ?: "Unknown user"
                        password = project.properties["ossrhPassword"] as String? ?: "Unknown user"
                    }
                }
            }
            publications {
                create("mavenJava", MavenPublication::class.java).apply {
                    groupId = project.group.toString()
                    this.artifactId = artifactId
                    version = project.version.toString()
                    pom {
                        description = "JDBI-ORM: Vaadin integration"
                        name = artifactId
                        url = "https://gitlab.com/mvysny/jdbi-orm-vaadin"
                        licenses {
                            license {
                                name = "The MIT License"
                                url = "https://opensource.org/licenses/MIT"
                                distribution = "repo"
                            }
                        }
                        developers {
                            developer {
                                id = "mavi"
                                name = "Martin Vysny"
                                email = "martin@vysny.me"
                            }
                        }
                        scm {
                            url = "https://gitlab.com/mvysny/jdbi-orm-vaadin"
                        }
                    }
                    from(components["java"])
                }
            }
        }

        signing {
            sign(publishing.publications["mavenJava"])
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            // to see the stacktraces of failed tests in the CI console.
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }
}

if (JavaVersion.current() > JavaVersion.VERSION_17 && gradle.startParameter.taskNames.contains("publish")) {
    throw GradleException("Release this library with JDK 17, to ensure JDK17 compatibility; current JDK is ${JavaVersion.current()}")
}
