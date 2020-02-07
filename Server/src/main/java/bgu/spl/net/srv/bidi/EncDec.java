package bgu.spl.net.srv.bidi;

import bgu.spl.net.api.MessageEncoderDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Vector;

public class EncDec implements MessageEncoderDecoder<String> {
    private byte[] byteArray = new byte[1 << 10];
    private int len;
    private byte[] opCode = new byte[2];
    private int numByte = 0;
    private byte[] numUsers = new byte[2];
    private int countZero = 0;
    private short opCodeShort = 0;
    private short numOfUsers = 0;
    private String resultFromPop ="";

    @Override
    public String decodeNextByte(byte nextByte) {
    	if(numByte == 0)
    		resultFromPop = "";
    	if (numByte < 2) {
			opCodeShort = 0;
			opCode[numByte] = nextByte;
			numByte++;
			if (opCode[1] == 3 | opCode[1] == 7) {
				opCodeShort = bytesToShort(opCode);
				reset();
				return String.valueOf(opCodeShort);
			}
			return null;
		}
    	
		if (numByte == 2) 
			opCodeShort = bytesToShort(opCode);
		
    	if (opCodeShort !=4) {
			if (nextByte == '\0' && numByte != 0) {
					countZero++;
					resultFromPop += popString(byteArray);
					resultFromPop += '\0';
					byteArray = new byte[1 << 10];
					len = 0;
			}
		}
	switch(opCodeShort) {
		case 5:
			if(countZero < 1) {
				pushByte(nextByte);
		         return null;
			}
			else {
				reset();
				return String.valueOf(opCodeShort)+'\0'+resultFromPop;
			}
		case 8:
			if(countZero < 1) {
				pushByte(nextByte);
		         return null;
			}
			else {
				reset();
				return String.valueOf(opCodeShort)+'\0'+resultFromPop;
			}
		case 1:
			if(countZero < 2) {
				pushByte(nextByte);
		         return null;
			}
			else {
				reset();
				return String.valueOf(opCodeShort)+'\0'+resultFromPop;
			}
		case 2:
			if(countZero < 2) {
				pushByte(nextByte);
		         return null;
			}
			else {
				reset();
				return String.valueOf(opCodeShort)+'\0'+resultFromPop;
			}
		case 6:
			if(countZero < 2) {
				pushByte(nextByte);
		        return null;
			}
			else {
				reset();
				return String.valueOf(opCodeShort)+'\0'+resultFromPop;
			}
		case 4:
			if(numByte > 1 && numByte < 6) {
				pushByte(nextByte);
				if(numByte==3) {
					resultFromPop += popString(byteArray);
		    		resultFromPop += '\0';
		    		byteArray = new byte[1 << 10];
		    		len = 0;
				}
				else if(numByte==5) {
					numUsers[0] = byteArray[0];
					numUsers[1] = byteArray[1];
					numOfUsers = bytesToShort(numUsers);
					resultFromPop += numOfUsers;
					resultFromPop += '\0';
					byteArray = new byte[1 << 10];
		    		len = 0;
				}
				return null;
			}
			
			if(numByte > 5 && numOfUsers > countZero) {
				if (nextByte == '\0' && numByte != 0) {
					countZero++;
					resultFromPop += popString(byteArray);
					resultFromPop += '\0';
					byteArray = new byte[1 << 10];
					len = 0;
					if(numOfUsers == countZero) {
						resultFromPop += popString(byteArray);
						reset();
						return String.valueOf(opCodeShort)+'\0'+resultFromPop;
					}
				}
				else {
					pushByte(nextByte);
					return null;
				}
			}
	   }

        return null;
    }

    @Override
    public byte[] encode(String message) {
        String[] split = message.split("\0");
        byte[] opCode = new byte[2];
        byte[] messOpCode = new byte[2];
        byte[] save = new byte[4];
        switch (split[0]) {
            case ("9"):                                              
                opCode = shortToBytes((short) 9);
                message = message.substring(2);
                byte[] save1 = (message).getBytes();
                Vector<Byte> save2 = new Vector<>();
                save2.add(opCode[0]);
                save2.add(opCode[1]);
                for (int i = 0; i < save1.length; i++) {
                    save2.add(save1[i]);
                }
                byte[] ans = new byte[save2.size()];
                for(int i = 0; i < save2.size();i++) {
                	ans[i] = save2.get(i);
                }
                return ans;

            case ("10"):
                opCode = shortToBytes((short) 10);                               
                String messOpCodeNum = split[1];
                messOpCode = shortToBytes((short) Integer.parseInt(split[1]));
                if (messOpCodeNum.equals("1") || messOpCodeNum.equals("3")|| messOpCodeNum.equals("2")|| messOpCodeNum.equals("5")|| messOpCodeNum.equals("6")) {
                    save[0] = opCode[0];
                    save[1] = opCode[1];
                    save[2] = messOpCode[0];
                    save[3] = messOpCode[1];
                    return save;
                }
                if (messOpCodeNum.equals("4") || messOpCodeNum.equals("7")) {      
                    byte[] numOfUsers = shortToBytes((short) Integer.parseInt(split[2]));
                    message = message.substring(7);
                    byte[] userNameList = (message).getBytes();
                    byte[] result2 = new byte[6+userNameList.length];
                    result2[0] = opCode[0];
                    result2[1] = opCode[1];
                    result2[2] = messOpCode[0];
                    result2[3] = messOpCode[1];
                    result2[4] = numOfUsers[0];
                    result2[5] = numOfUsers[1];
                    
                    for (int i = 0; i < userNameList.length; i++) {
                    	result2[i + 6] = userNameList[i];
                    }
                   
                    return result2;
                }

                if (messOpCodeNum.equals("8")) {   
                    byte[] numPosts = shortToBytes((short) Integer.parseInt(split[2]));
                    byte[] numFollowers = shortToBytes((short) Integer.parseInt(split[3]));
                    byte[] numFollowing = shortToBytes((short) Integer.parseInt(split[4]));     
                    byte[] result = new byte[10];
                    result[0] = opCode[0];
                    result[1] = opCode[1];
                    result[2] = messOpCode[0];
                    result[3] = messOpCode[1];
                    result[4] = numPosts[0];
                    result[5] = numPosts[1];
                    result[6] = numFollowers[0];
                    result[7] = numFollowers[1];
                    result[8] = numFollowing[0];
                    result[9] = numFollowing[1];
                    return result;
                }

            case ("11"):                                                       
                opCode = shortToBytes((short) 11);
                messOpCode = shortToBytes((short) Integer.parseInt(split[1]));
                save[0] = opCode[0];
                save[1] = opCode[1];
                save[2] = messOpCode[0];
                save[3] = messOpCode[1];
                return save;
        }
        return null;
    }


    private void pushByte(byte nextByte) {
    	if(byteArray[0]==0 && len == 1 && opCodeShort !=4 && opCodeShort !=7) {
    		byteArray = new byte[1 << 10];
    		len = 0;
    	}   		
        if (len >= byteArray.length) {
            byteArray = Arrays.copyOf(byteArray, len * 2);
        }

        byteArray[len++] = nextByte;
        numByte++;
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private String popString(byte[] arr) { 
        String result = new String(arr, 0, len,StandardCharsets.UTF_8);
        return result;
    }

    private void reset(){
    	 len=0;
         opCode = new byte[2];
         numByte = 0;
         numUsers = new byte[2];
         countZero = 0;
         numOfUsers = 0;
    }
}
