plugins {
    id("java")
}

group = "org"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starter Web (без лишних зависимостей)
    implementation("org.springframework.boot:spring-boot-starter-web:3.1.5") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-reactor-netty")
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-r2dbc")
    }
    // JJWT для JWT токенов
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Spring Boot Starter Test
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.1.5")

    // JUnit
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
    }
}