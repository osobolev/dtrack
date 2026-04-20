plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "dtrack"

fun add(name: String) {
    include(name)
    project(":$name").projectDir = file("subprojects/$name")
}

add("common")
add("dao_test")
add("web")
add("admin")
add("deploy")
