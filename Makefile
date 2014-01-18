.PHONY:	clean

all:	PSONB/dist/PSONB.jar

PSONB/dist/PSONB.jar:
	cd PSONB && ant jar

clean:
	cd PSONB && ant clean
	
