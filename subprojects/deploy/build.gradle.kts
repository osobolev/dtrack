plugins {
    id("lib")
}

val distr = configurations.create("distr")

dependencies {
    distr(project(":web"))
    distr(project(":admin"))
}

val dest = "$rootDir/distr"

tasks.register("webFiles", Copy::class) {
    from("../web/web")
    into("$dest/root")
}

tasks.register("copyJars", Copy::class) {
    from(distr)
    from("config")
    into("$dest")
}

tasks.register("distr") {
    dependsOn("webFiles", "copyJars")
}
