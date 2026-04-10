plugins {
    id("lib")
}

dependencies {
    implementation("com.google.code.gson:gson:2.13.2")

    implementation(project(":common"))

    runtimeOnly("org.postgresql:postgresql:42.7.10")

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
