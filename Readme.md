# healthy-choice
A Simple CLI app for meal planning

## Instructions
1. Install JDK with Java 8 or higher: https://www.oracle.com/java/technologies/javase-downloads.html
2. Install maven: https://maven.apache.org/install.html
3. Run the following (or use `make && make run` as below):

        mvn compile assembly:single
        java -jar target/healthy-choice-1.0-SNAPSHOT-jar-with-dependencies.jar

4. Enjoy?

To run provided tests, execute `mvn test`

## Notes
Makefile is included for the following:
| Action                                        | Command       |
| -                                             | -             | 
| Compile                                       | `make`        |
| Run (Need to compile first of course):        | `make run`    |
| Cleanup                                       | `make clean`  |        
