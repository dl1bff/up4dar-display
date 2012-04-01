

all: UPDisplay.jar


UPDisplay.class: UPDisplay.java
		javac UPDisplay.java
		

UPDisplay.jar: UPDisplay.class
		jar cvfm UPDisplay.jar mymanifest.txt UPDisp*.class
		
		