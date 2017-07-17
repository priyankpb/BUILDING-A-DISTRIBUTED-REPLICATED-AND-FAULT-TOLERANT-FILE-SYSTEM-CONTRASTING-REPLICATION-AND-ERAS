all: compile
	@echo -e '[INFO] Done!'
clean:
	@echo -e '[INFO] Cleaning Up..'
	@-rm -rf cs555RS/**/*.class

compile: 
	@echo -e '[INFO] Compiling the Source..'
	@javac -cp ".:reed-solomon-erasure-coding.jar" cs555RS/**/*.java
