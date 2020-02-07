#ifndef ENCODERDECODER_H
#define ENCODERDECODER_H


#include <string>
#include <vector>
#include <map>


class encoderDecoder{

private:
    std::vector<char> bytes;
    int len = 0;
    std::string resultSt;
    std::map<std::string, int> opNames;
    int countBytes = 0;
    int countZeros = 0;
    short opcode = -1;
    short messageOpCode = -1;
    int numOfUsers = 0;
    int numPost = 0;
    int numFollowers = 0;
    int numFollowing = 0;
    std::string userNameList;
    std::string content;
public:
    encoderDecoder();

    std::string decodeNextByte(char* nextByte, std::string &result);

    std::string decode(std::string messageFromSer);

    std::vector<std::string> split(std::string line, char tav);

    const std::string encode(std::string message);

    void resetDecoder();

    void resetEncoder();

//    void pushByte(char* nextByte);
//
//    std::string popString();

    void shortToBytes(short num, std::vector<char> &bytesArr);

    short bytesToShort(std::vector<char> bytesArr);

    void shortToBytesfollow(short num, std::vector<char> bytesArr);




































};



















































#endif
