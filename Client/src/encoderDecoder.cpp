#include "../include/encoderDecoder.h"
#include <string>
#include <cstring>
#include <sstream>
#include <iostream>
#include <map>
#include <encoderDecoder.h>


encoderDecoder::encoderDecoder() {}

const std::string encoderDecoder::encode(std::string message) {
    opNames["REGISTER"] = 1;
    opNames["LOGIN"] = 2;
    opNames["LOGOUT"] = 3;
    opNames["FOLLOW"] = 4;
    opNames["POST"] = 5;
    opNames["PM"] = 6;
    opNames["USERLIST"] = 7;
    opNames["STAT"] = 8;


    std::vector<char> result;
    std::string opcodeString = "";
    for (char i : message){
        if (i != ' ')
            opcodeString += i;
        else {
            std::size_t pos = opcodeString.length() + 1;
            message = message.substr(pos);
            break;
        }
    }
    if(opcodeString == "LOGOUT" ||  opcodeString == "USERLIST"){
        shortToBytes(opNames[opcodeString], result);
    }


    if (opcodeString == "REGISTER" || opcodeString == "LOGIN" || opcodeString == "STAT" ) {
        shortToBytes(opNames[opcodeString], result);
        for (char i : message) {
            if (i == ' ') {
                i = '\0';
            }
            result.push_back(i);
        }
        result.push_back('\0');
    }
    else if(opcodeString == "POST" ){
        shortToBytes(opNames[opcodeString], result);
        for (char i : message) {
            result.push_back(i);
        }
        result.push_back('\0');
    }
    else if(opcodeString == "PM"){
        shortToBytes(opNames[opcodeString], result);
        bool first = false;
        for (char i : message) {
            if(i == ' ' && !first){
                i = '\0';
                first = true;
            }
            result.push_back(i);
        }
        result.push_back('\0');
    }
    else if (opcodeString == "FOLLOW") {
        shortToBytes(4, result);
        std::vector<char> messageChar;
        result.push_back(message.at(0));
        std::string NumOfUsers = "";
        for (unsigned int i = 2; i < message.length(); i++) {
            if (message.at(i) != ' ')
                NumOfUsers.push_back(message.at(i));
            else {
                message = message.substr(i + 1);
                break;
            }

        }
        shortToBytes(std::stoi(NumOfUsers), result);
        for (char i : message) {
            if (i == ' ') {
                i = '\0';
            }
            result.push_back(i);
        }
        result.push_back('\0');
    }

        for (char i : result)
            resultSt += i;

        result.clear();

        return resultSt;

}


    std::string encoderDecoder::decodeNextByte(char *nextByte, std::string &result) {

        bytes.push_back(*nextByte);
        countBytes++;

        // Find the opcode
        if (countBytes == 3) {
            if ((bytes.at(0) == 0 && bytes.at(1) == 0)) {
                std::vector<char> byteTmp;
                byteTmp.push_back(bytes.at(1));
                byteTmp.push_back(bytes.at(2));
                bytes.clear();
                bytes.push_back(byteTmp.at(0));
                bytes.push_back(byteTmp.at(1));
                countBytes--;
            }

            std::vector<char> byteArr;
            byteArr.push_back(bytes.at(0));
            byteArr.push_back(bytes.at(1));
            opcode = bytesToShort(byteArr);
        }

        // Count splitters
        if ((*nextByte) == '\0')
            countZeros++;

        if (opcode == 9) {
            if (countZeros == 3) {
                char type(bytes.at(2));
                result =  std::to_string(opcode) + '\0' + type + '\0';
                for(unsigned int i =3 ; i < bytes.size();i++)
                    result += bytes.at(i);
                return result;
         }
        }




        else if (opcode == 11) {
            //Find messsage code
            if (countBytes == 4) {
                std::vector<char> messageByteArr;
                messageByteArr.push_back(bytes.at(2));
                messageByteArr.push_back(bytes.at(3));
                messageOpCode = bytesToShort(messageByteArr);

                result = std::to_string(opcode) + '\0' + std::to_string(messageOpCode) + '\0';
                return result;
            }


        } else if (opcode == 10) {
            //Find messsage code
            if (countBytes == 4) {
                std::vector<char> messageByteArr;
                messageByteArr.push_back(bytes.at(2));
                messageByteArr.push_back(bytes.at(3));
                messageOpCode = bytesToShort(messageByteArr);
            }

            if (messageOpCode == 1 || messageOpCode == 2 || messageOpCode == 3 || messageOpCode == 5 || messageOpCode == 6) {
                result = std::to_string(opcode) + '\0' + std::to_string(messageOpCode) + '\0';
                return result;

            } else if (messageOpCode == 4 || messageOpCode == 7) {
                if (countBytes == 6) {
                    std::vector<char> numOfUsersArray;
                    numOfUsersArray.push_back(bytes.at(4));
                    numOfUsersArray.push_back(bytes.at(5));
                    numOfUsers = bytesToShort(numOfUsersArray);
                    countZeros=0;
                } else if (countBytes > 6 && countZeros < numOfUsers) {
                    userNameList += nextByte[0];
                    }
                if(countZeros == numOfUsers) {
                    result = std::to_string(opcode) + '\0' + std::to_string(messageOpCode) + '\0' +
                             std::to_string(numOfUsers) +
                             '\0' + userNameList;
                    return result;
                }


            } else if (messageOpCode == 8) {
                if (countBytes == 10) {
                    std::vector<char> numPostsArr;
                    numPostsArr.push_back(bytes.at(4));
                    numPostsArr.push_back(bytes.at(5));
                    numPost = bytesToShort(numPostsArr);
                    std::vector<char> numFollowerArr;
                    numFollowerArr.push_back(bytes.at(6));
                    numFollowerArr.push_back(bytes.at(7));
                    numFollowers = bytesToShort(numFollowerArr);
                    std::vector<char> numFollowingArr;
                    numFollowingArr.push_back(bytes.at(8));
                    numFollowingArr.push_back(bytes.at(9));
                    numFollowing = bytesToShort(numFollowingArr);

                    result =
                            std::to_string(opcode) + '\0' + std::to_string(messageOpCode) + '\0' +
                            std::to_string(numPost) +
                            '\0' + std::to_string(numFollowers) + '\0' + std::to_string(numFollowing) + '\0';
                    return result;
                }
            }

            return "";

        }
        return "";
    }

    std::string encoderDecoder::decode(std::string messageFromSer) {
        std::vector<std::string> spilted = split(messageFromSer, '\0');


        switch (std::stoi(spilted[0])) {
            case 10: {
                if (spilted[1] == "1" || spilted[1] == "3" || spilted[1]=="2" || spilted[1]=="5" ||  spilted[1]=="6")
                    return "ACK " + spilted.at(1);

                if (spilted[1] == "4" || spilted[1] == "7") {
                    std::string userListToPrint = "";
                    for (unsigned int i = 3; i < spilted.size(); i++) {
                        userListToPrint += spilted[i] + " ";

                    }
                    return "ACK " + spilted.at(1) + " " + spilted.at(2) + " " + userListToPrint;
                }

                if (spilted[1] == "8") {
                    return "ACK " + spilted.at(1) + " " + spilted.at(2) + " " + spilted.at(3) + " " + spilted.at(4);
                }

            }

            case 9: {
                std::string post_pmString;
                std::string postingUser = "";
                std::string content = "";
                if (spilted[1] == "0")
                    post_pmString = "PM ";
                else
                    post_pmString = "Public ";
                postingUser = spilted[2];
                content = spilted[3];
                return "NOTIFICATION " + post_pmString  + postingUser  + " "+ content;
            }
            case 11: {
                return "ERROR " + spilted.at(1);
            }

        }
        return "";
    }

    void encoderDecoder::resetDecoder() {
        bytes.erase(bytes.begin(), bytes.end());
        countBytes = 0;
        countZeros = 0;
        opcode = -1;
        messageOpCode = -1;
        numOfUsers = 0;
        numPost = 0;
        numFollowers = 0;
        numFollowing = 0;
        userNameList = "";
        content = "";


    }

    void encoderDecoder::resetEncoder() {
        resultSt = "";
    }



    void encoderDecoder::shortToBytes(short num, std::vector<char> &bytesArr) {
        bytesArr.push_back((num >> 8) & 0xFF);
        bytesArr.push_back(num & 0xFF);
    }

    short encoderDecoder::bytesToShort(std::vector<char> bytesArr) {
        short result = (short) ((bytesArr[0] & 0xff) << 8);
        result += (short) (bytesArr[1] & 0xff);
        return result;
    }


    std::vector<std::string> encoderDecoder::split(std::string line, char tav) {
        std::vector<std::string> splited;
        size_t posComma = 0;
        std::string token;
        while ((posComma = line.find(tav)) != std::string::npos) {    //while there is a comma in the line
            token = line.substr(0, posComma);
            splited.push_back(token);
            line.erase(0, posComma + 1);
        }
        splited.push_back(line);
        return splited;
    }


