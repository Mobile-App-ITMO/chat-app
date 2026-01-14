plugins {
    kotlin("jvm") version "2.2.20"
    id("io.gatling.gradle") version "3.11.5"
}

repositories {
    mavenCentral()
}


dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.gatling.highcharts:gatling-charts-highcharts:3.11.5")
    implementation("io.gatling:gatling-app:3.11.5")
    implementation("io.gatling:gatling-core:3.11.5")
    implementation("io.gatling:gatling-app:3.11.5")
    implementation("io.gatling:gatling-test-framework:3.11.5")
}

