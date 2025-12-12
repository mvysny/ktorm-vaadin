plugins {
    alias(libs.plugins.vaadin)
    application
}

dependencies {
    implementation(project(":ktorm-vaadin"))

    // Vaadin
    implementation(libs.vaadin.core)
    implementation(libs.karibu.dsl)
    implementation(libs.vaadinboot)
    api("com.github.mvysny.karibu-tools:karibu-tools-23:0.26")

    implementation(libs.slf4j.simple)
    implementation(libs.hikaricp)
    implementation(libs.h2)
    implementation(libs.flyway)

    // Fast Vaadin unit-testing with Karibu-Testing: https://github.com/mvysny/karibu-testing
    testImplementation(libs.karibu.testing)
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "testapp.MainKt"
}
