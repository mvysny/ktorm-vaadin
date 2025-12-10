dependencies {
    implementation(libs.slf4j.api)
    api(libs.ktorm)
    compileOnly(libs.vaadin.core)
    testImplementation(libs.vaadin.core)
    api(libs.hibernate.validator)
    implementation(libs.jakarta.el)

    // tests
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.hikaricp)
    testImplementation(libs.slf4j.simple)
    testImplementation(libs.h2) // https://repo1.maven.org/maven2/com/h2database/h2/
    testImplementation(libs.karibu.testing)
}

val publishing = ext["publishing"] as (artifactId: String) -> Unit
publishing("ktorm-vaadin")
