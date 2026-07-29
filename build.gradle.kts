fun requiredMajor(mod: ModuleComponentIdentifier): String {
    if (mod.group == "org.eclipse.jetty") return "11." // Version >= 12 requires Java 17
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
