.PHONY:	clean


all:	PSONB/dist/PSONB.jar

PSONB/dist/PSONB.jar:
	which antzz; if [ $$? -eq 0 ] ; then cd PSONB && antzz jar ; else cd PSONB && ant jar ; fi

clean:
	which antzz; if [ $$? -eq 0 ] ; then cd PSONB && antzz clean ; else cd PSONB && ant clean ; fi
	
