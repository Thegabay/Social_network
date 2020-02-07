CFLAGS:=-c -Wall -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system

all: BGSclient
	g++ -pthread -o bin/BGSclient bin/connectionHandler.o bin/Client.o bin/encoderDecoder.o $(LDFLAGS)

BGSclient: bin/connectionHandler.o bin/Client.o bin/encoderDecoder.o
	
bin/connectionHandler.o: src/connectionHandler.cpp
	g++ $(CFLAGS) -o bin/connectionHandler.o src/connectionHandler.cpp

bin/Client.o: src/Client.cpp
	g++ $(CFLAGS) -o bin/Client.o src/Client.cpp

bin/encoderDecoder.o: src/encoderDecoder.cpp
	g++ $(CFLAGS) -o bin/encoderDecoder.o src/encoderDecoder.cpp
	
.PHONY: clean
clean:
	rm -f bin/*
