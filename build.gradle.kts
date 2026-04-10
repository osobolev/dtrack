plugins {
    id("com.github.ben-manes.versions") version "0.53.0"
}

fun requiredMajor(mod: ModuleComponentIdentifier): String {
    if (mod.group == "org.eclipse.jetty") return "9."
    if (mod.module == "HikariCP") return "4."
    return ""
}

tasks.withType(com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask::class).configureEach {
    rejectVersionIf {
        candidate.version.contains("-a") || 
        candidate.version.contains("-b") ||
        !candidate.version.startsWith(requiredMajor(candidate))
    }
}

tasks.register("clean", Delete::class) {
    delete("distr")
}
