dependencies {
    api("io.github.likespro:commons-core-mit:3.1.0")
    implementation("io.github.likespro:commons-reflection:3.1.0")
    implementation("io.github.likespro:commons-network:3.1.0")

    implementation("com.google.code.gson:gson:2.13.1") // TODO remove this after commons:3.1.1

    implementation(kotlin("reflect"))
}