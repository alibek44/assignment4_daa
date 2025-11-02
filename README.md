Assignment 4 – Smart City Scheduling
Project Overview
This project implements algorithms for analyzing directed graphs representing task dependencies in a Smart City Scheduling system. It uses Tarjan’s algorithm for Strongly Connected Components (SCC), Kahn’s algorithm for Topological Sorting, and dynamic programming for computing shortest and longest paths in Directed Acyclic Graphs (DAGs). The program processes JSON datasets and outputs results to metrics.csv with detailed operation statistics and performance data.
Run Instructions
1. Build the project
   Make sure you have Java 17+ and Apache Maven installed.
   Then open Terminal inside the project root (assignment4_daa/) and run:
   mvn clean package -DskipTests
   This will compile the code and create a runnable .jar file in the target/ folder.
2. Run on all datasets
   Run the program on every JSON dataset from the data/ folder:
   java -jar target/assignment4_daa-1.0.0.jar

If you want to start fresh (and delete the previous metrics file):
java -jar target/assignment4_daa-1.0.0.jar --all --fresh

All results are saved automatically into: out/metrics.csv
3. Run a single dataset
   To run only one dataset (for example small_1.json):
   java -jar target/assignment4_daa-1.0.0.jar --data data/small_1.json
4. Command-line options
   Flag	Description
   --all	Run all JSON datasets from /data folder
   --data <path>	Run a specific dataset only
   --fresh	Delete old out/metrics.csv before execution
5. Output description
- Console will show SCC components, topological order, shortest and longest paths.
- The file out/metrics.csv will contain all timing, operation counts, and path details.
- You can open the CSV file in Excel to see metrics for each phase (Tarjan, Topo, DAG-SP, etc).
6. Run tests
   To verify parsing and algorithm correctness:
   mvn test

You should see:
Tests passed: 1 of 1 test – exit code 0

Example test:
- GraphParsingTest checks if data/small_1.json loads correctly.
- TarjanSCCTest validates SCC decomposition results.
