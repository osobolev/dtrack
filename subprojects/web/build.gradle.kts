plugins {
    id("lib")
    id("war")
}

tasks.war {
    webAppDirectory.set(file("web"))
}

dependencies {
    implementation(libs.jetty)
    implementation(libs.owasp.sanitizer)
    implementation(libs.freemarker)
    implementation(libs.commons.fileupload)
    implementation(libs.hikari)
    implementation(libs.small.json)
    implementation(libs.jose4j)

    implementation(project(":common"))

    runtimeOnly(libs.postgres.jdbc)
    runtimeOnly(libs.slf4j.nop)
    runtimeOnly(libs.commons.io)

    manualImplementation(project(":dao_test"))
}

tasks.jar {
    manifest {
        attributes(
            "Class-Path" to configurations.runtimeClasspath.map { conf -> conf.files.map { f -> f.name }.sorted().joinToString(" ") },
            "Main-Class" to "dtrack.web.Main"
        )
    }
}
