plugins {
    kotlin("jvm") version "1.7.10"
}

group = "de.hanno"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")

    implementation("com.michael-bull.kotlin-result:kotlin-result:1.1.16")
    implementation("com.michael-bull.kotlin-result:kotlin-result-coroutines:1.1.16")
    implementation("com.michael-bull.kotlin-retry:kotlin-retry:1.0.9")
    implementation("io.github.resilience4j:resilience4j-retrofit:1.7.1")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")

}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
}
