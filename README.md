# Distributed Matrix Multiplication Using Strassen's Algorithm

## Overview
This project implements a distributed system for performing matrix multiplication using Strassen's algorithm. It consists of three main components:

1. **MyClient** - Generates matrices and communicates with the server.
2. **ServerRouter** - Routes clients to an available server.
3. **MyServer** - Handles matrix multiplication using Strassen's algorithm and multithreading.

## Features
- Uses a client-server model for distributed computation.
- Implements Strassen's algorithm for matrix multiplication.
- Supports multithreading for parallel computation.
- Includes a router to manage multiple client-server connections.

## File Structure
- `MyClient.java` - Sends matrices to the server and receives results.
- `ServerRouter.java` - Routes client requests to the appropriate server.
- `MyServer.java` - Processes matrix multiplications and returns results.

## How It Works
1. The **client** generates a set of matrices and connects to the **ServerRouter**.
2. The **ServerRouter** directs the client to an available **server**.
3. The **server** performs the matrix multiplication using Strassen's algorithm.
4. The result is sent back to the **client**, which calculates execution time and efficiency.
