<p align="center">
 <img width="100px" src="https://github.com/likespro.png" align="center" alt="likespro avatar" />
 <h2 align="center">LPFCP Java</h2>
 <p align="center">Expose your functions to the network with just one annotation</p>
</p>
<p align="center">
    <a href="https://github.com/likespro/lpfcp-java">
      <img alt="LICENSE" src="https://img.shields.io/badge/License-MPL%202.0-brightgreen" />
    </a>
    <a href="https://github.com/likespro/lpfcp-java/graphs/contributors">
      <img alt="GitHub Contributors" src="https://img.shields.io/github/contributors/likespro/lpfcp-java" />
    </a>
    <a href="https://github.com/likespro/lpfcp-java/issues">
      <img alt="Issues" src="https://img.shields.io/github/issues/likespro/lpfcp-java?color=0088ff" />
    </a>
    <a href="https://github.com/likespro/lpfcp-java/pulls">
      <img alt="GitHub pull requests" src="https://img.shields.io/github/issues-pr/likespro/lpfcp-java?color=0088ff" />
    </a>
  </p>
<p align="center">
    <a href="https://github.com/likespro/lpfcp-java/actions/workflows/main-branch.yml">
      <img alt="Build Passing" src="https://github.com/likespro/lpfcp-java/workflows/Main Branch Workflow/badge.svg" />
    </a>
    <a href="https://codecov.io/gh/likespro/lpfcp-java"> 
        <img src="https://codecov.io/gh/likespro/lpfcp-java/graph/badge.svg?token=9H24353DTH"/> 
    </a>
    <a href="https://github.com/likespro/lpfcp-java">
      <img alt="Git Size" src="https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/likespro/lpfcp-java/badges/git-size.md" />
    </a>
    <a href="https://github.com/likespro/lpfcp-java">
      <img alt="Git File Count" src="https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/likespro/lpfcp-java/badges/git-file-count.md" />
    </a>
    <a href="https://github.com/likespro/lpfcp-java">
      <img alt="Git Lines Of Code" src="https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/likespro/lpfcp-java/badges/git-lines-of-code.md" />
    </a>
  </p>

**L**ikes**P**ro **F**unction **C**all **P**rotocol (LPFCP) Java lets you expose ordinary Java and Kotlin classes as remote-callable services with a single annotation.
LPFCP Java implements the LPFCP protocol — check out the [LPFCP protocol specification](https://github.com/likespro/lpfcp) — and provides both server and client parts instrumentals.

---

## Features

- 🌐 **Annotation-Driven** – Turn any Java/Kotlin class object into an HTTP-accessible service by annotating your functions to expose.
- 🚀 **Lightweight** – Minimal dependencies and overhead; ideal for microservices and serverless deployments.
- 🔌 **Java & Kotlin Support** – Use LPFCP from either language with the same ease.
- 🛠 **Zero-Boilerplate** – No manual serialization or wiring; LPFCP takes care of it using reflection.

---

## Getting Started

### Installation

**Gradle**:
```kotlin
dependencies {
    implementation("io.github.likespro:lpfcp-core:1.0.0") // Core features: getProcessor, .processRequest, etc.
    implementation("io.github.likespro:lpfcp-ktor:1.0.0") // Integration with Ktor: lpfcpServer, Route.lpfcp, etc.
}
```

**Maven**:
```xml
<!-- Core features: getProcessor, .processRequest, etc. -->
<dependency>
    <groupId>io.github.likespro</groupId>
    <artifactId>lpfcp-core</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Integration with Ktor: lpfcpServer, Route.lpfcp, etc. -->
<dependency>
    <groupId>io.github.likespro</groupId>
    <artifactId>lpfcp-ktor</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage
### 1. Define Your Interface (Shared Code)
```kotlin
interface Calculator {
    fun add(a: Int, b: Int): Int
    fun subtract(a: Int, b: Int): Int
}
```

### 2. Implement & Annotate (Server Code)
Annotate your implementation class with `@LPFCP.ExposedFunction`:
```kotlin
class CalculatorImpl : Calculator {
    @LPFCP.ExposedFunction
    override fun add(a: Int, b: Int) = a + b

    @LPFCP.ExposedFunction
    override fun subtract(a: Int, b: Int) = a - b
}
```

### 3. Start the Server (Server Code)
Use the embedded server helper to expose your service over HTTP:
```kotlin
fun main() {
    val calculator = CalculatorImpl()
    lpfcpServer(processor = calculator, port = 8080)
        .start(wait = true)
}
```

### 4. Call from a Client (Client Code)
Obtain a client-side proxy and invoke methods as if they were local:
```kotlin
fun main() {
    val calculator: Calculator = LPFCP.getProcessor<Calculator>(
        "http://localhost:8080/lpfcp"
    )
    println(calculator.add(1, 2))       // -> 3
    println(calculator.subtract(5, 3))  // -> 2
}
```

## Learn more
### Examples
Check out the [examples/](examples) folder for a working example of the calculator app in Java & Kotlin.

### Wiki
Check out https://likespro.gitbook.io/lpfcp-java to view guides to LPFCP Java library.

### Documentation
Check out https://likespro.github.io/lpfcp-java/ to view auto generated documentation for the project by Dokka (similar to Javadoc)

## License
LPFCP Java is released under the [`Mozilla Public License Version 2.0`](LICENSE).