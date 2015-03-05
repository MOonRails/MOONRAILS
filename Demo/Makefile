MOON=java
PARAMS=-jar ./bin/moon.jar

gen/%.gen.ino: src/%.ino
	$(MOON) $(PARAMS) $< $@

all: clean upload
	cd ./gen/eu.moonrails.drivers.SingleFileArduinoDriver &&	make

gen:	gen/Ardu.gen.ino
		
clean:
	cd ./gen && rm -rf * .??*

upload: gen
	cd ./gen/eu.moonrails.drivers.SingleFileArduinoDriver &&	make upload

