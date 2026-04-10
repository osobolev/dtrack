plugins {
    id("lib")
}

dependencies {
    implementation(libs.gson)

    implementation(project(":common"))

    runtimeOnly(libs.postgres.jdbc)

    manualImplementation(project(":dao_test"))
}

tasks.jar {
    manifest {
        attributes(
            "Class-Path" to configurations.runtimeClasspath.map { conf -> conf.files.map { f -> f.name }.sorted().joinToString(" ") },
            "Main-Class" to "dtrack.admin.Main"
        )
    }
}
