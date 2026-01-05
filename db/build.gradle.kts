plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":core"))
    implementation(libs.exposed)
    implementation(libs.exposed.datetime)
    implementation(libs.h2)
    implementation("io.github.crackthecodeabhi:kreds:0.9.0")
    
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.exposed.jdbc)
    testImplementation(libs.kotlin.test.junit)
}