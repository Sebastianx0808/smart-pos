plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation ("mysql:mysql-connector-java:8.0.33")
    implementation ("org.knowm.xchart:xchart:3.8.3")
}

tasks.test {
    useJUnitPlatform()
}