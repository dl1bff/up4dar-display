

all: UPDisplay.jar UPDisplayA.jar


UPDisplay.class: UPDisplay.java
		javac UPDisplay.java
		

UPDisplay.jar: UPDisplay.class
		jar cvfm UPDisplay.jar mymanifest.txt UPDisplay.class UPDisplay$*.class
		

UPDisplayA.class: UPDisplayA.java
		javac UPDisplayA.java
		

UPDisplayA.jar: UPDisplayA.class
		jar cvfm UPDisplayA.jar mymanifest-audio.txt UPDisplayA.class UPDisplayA$*.class
		
		