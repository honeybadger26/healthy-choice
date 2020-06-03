default:
	mvn compile assembly:single

run:
	java -jar target/healthy-choice-1.0-SNAPSHOT-jar-with-dependencies.jar

clean:
	mvn clean
	rm sqlite.db