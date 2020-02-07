#include "../include/connectionHandler.h"
#include "../include/encoderDecoder.h"

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port) : host_(host), port_(port), io_service_(),
                                                                socket_(io_service_) {}

ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception &e) {
        std::cerr << "Connection failed (ERROR: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getLine(std::string &line) {
//    std::cout << "in fun get line" << std::endl;
    bool s = getFrameAscii(line);
    if (s) {
        //change from here
        result = encDec.decode(line);
        if(result == "ACK 3")
            shouldTerminate = true;
    }
    std::cout << result << std::endl;
 //       std::cout << encDec.decode(line) << std::endl;
    return s;


}

bool ConnectionHandler::getFrameAscii(std::string &frame) {
    char ch;
    try {
        do {
            getBytes(&ch, 1);
            frame = encDec.decodeNextByte(&ch, frame);
        } while (frame.empty());
    } catch (std::exception &e) {
        std::cerr << "recv failed (ERROR: " << e.what() << ')' << std::endl;
        encDec.resetDecoder();
        return false;
    }
    encDec.resetDecoder();
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp) {
            tmp += socket_.read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (ERROR: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendLine(std::string &line) {
    bool res = sendFrameAscii(encDec.encode(line));      //send to encode the line, and then send it to the "sendFrameAscii" the encoded line
    encDec.resetEncoder();
    return res;
}

bool ConnectionHandler::sendFrameAscii(const std::string &frame) {
    bool result = sendBytes(frame.c_str(), frame.length());
//    std::cout << "SENT!" << std::endl;
    return result;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (ERROR: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


// Close down the connection properly.
void ConnectionHandler::close() {
    try {
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}
bool ConnectionHandler::shouldTerminatefun(){
    return shouldTerminate;
};