plugins {
    id("java")
}

group = "kz.zsanzharko"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("org.telegram:telegrambots:6.8.0")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.apache.poi:poi:5.2.3")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("org.apache.logging.log4j:log4j-to-slf4j:2.17.1")
    implementation("org.slf4j:slf4j-api:2.0.7")
    testImplementation("org.slf4j:slf4j-reload4j:2.0.7")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    compileOnly("org.projectlombok:lombok:1.18.20")
    annotationProcessor("org.projectlombok:lombok:1.18.20")
}

tasks.test {
    useJUnitPlatform()
}