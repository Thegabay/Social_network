#include <stdlib.h>
#include "../include/connectionHandler.h"
#include <thread>
#include <mutex>
#include <condition_variable>

std::mutex lock1_mutex;
std::condition_variable cv;
bool terminateKeyboard = false;
bool terminate = false;
std::unique_lock<std::mutex> lck(lock1_mutex);

class readFromKeyboard {
private:
    ConnectionHandler *connectionHandler;
public:
    readFromKeyboard(ConnectionHandler *connectionHandler) : connectionHandler(connectionHandler) {}

    void run() {
        while (!terminate) {
            const short bufsize = 1024;
            char buf[bufsize];
            std::cin.getline(buf, bufsize);
            std::string line(buf);
            if (!connectionHandler->sendLine(line)) {
                std::cout << "Disconnected. Exiting...\n" << std::endl;
            }

            if (line == "LOGOUT")
                cv.wait(lck);

        }
    }
};


class readFromSocket {
private:
    ConnectionHandler *connectionHandler;
public:
    readFromSocket(ConnectionHandler *connectionHandler) : connectionHandler(connectionHandler) {}

    void run() {
        while (!terminate) {
            std::string answer = "";
            bool status = connectionHandler->getLine(answer);
            if (!status)
                std::cout << "Disconnected. Exiting...\n" << std::endl;
        //    else
        //        std::cout<<"connectionHandler get line"<<std::endl;

            if (connectionHandler->shouldTerminatefun()==true) {
                    terminate=true;
                    cv.notify_all();
            }
            else
                cv.notify_all();

            answer = "";
        }
    }
};

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/
int main(int argc, char *argv[]) {
    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    readFromKeyboard task1(&connectionHandler);
    readFromSocket task2(&connectionHandler);

    std::thread th1(&readFromKeyboard::run, &task1);
    std::thread th2(&readFromSocket::run, &task2);
    th1.join();
    th2.join();
    connectionHandler.close();
    return 0;
}
