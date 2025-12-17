# Distributed Matrix Multiplication Using Strassen's Algorithm

This project implements matrix multiplication using Strassen's algorithm, with a focus on a cleaner, more modular design and a distributed architecture.



## Versions

### V1
- Implemented basic Strassen's algorithm on a single machine.
- Functionally correct, but had poor readability and maintainability.
- Roughly performed matrix multiplication, but lacked scalability and structure.
- Issues with number types wrapping and inefficient memory usage.

### V2 (Current)
- Major refactor focused on:
    - Improved code readability.
    - Clear separation of concerns.
    - More modular design for easier maintenance and experimentation.
- Adds a distributed architecture intended to run across multiple machines / worker nodes.
- Dynamic workload allocation based on available worker nodes.
- Better performance through improved parallelization and batching.
- Error handling and validation are minimal. Unexpected inputs may result in failures.


### Future Improvements
- Additional logging, monitoring, and configuration options.
- More comming soon
---

## Requirements

- Java 21
- Maven 3.9.11
- Optional for distributed mode but have multiple machines/ computers OR just run multiple instances on the same machine with different ports.

---

## Building the Project

From the project root (where `pom.xml` is located), run:

```bash
mvn clean package
```

This will make a shaded/uber JAR named something like:

```text
target/distributed-matrix-1.0-SNAPSHOT.jar
```

---

## Running the JAR
If the application supports a simple local run (no distributed nodes), you can run:

```bash
java -jar target/distributed-matrix-1.0-SNAPSHOT.jar
```
- set java flags if needed for memory management, e.g. `-Xmx10G` for 10GB 
heap size and `-Xms10G` for 10GB initial heap size . This would look something like :
```bash
java -Xms10G -Xmx10G -jar target/distributed-matrix-1.0-SNAPSHOT.jar
```


## Project Status

V2 is in a working but experimental state. The core logic is implemented and works for standard cases, but there are still edge-cases around the limits of how many matrices you can process.

Contributions, bug reports, and suggestions are welcome.