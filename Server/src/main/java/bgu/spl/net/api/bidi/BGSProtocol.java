package bgu.spl.net.api.bidi;

import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BGSProtocol implements BidiMessagingProtocol<String> {
	private int id;
	private Connections<String> connections;
	private UserData user;
	private ConcurrentHashMap<String,UserData> UsersToDataMap;
	private ConcurrentHashMap<String,Integer> ConnectedUserToClient;
	private LinkedList<String> postAndPM;
	private boolean terminate;

	public BGSProtocol(DataBase data) {
		this.UsersToDataMap = data.getUsersToDataMap();
		this.ConnectedUserToClient = data.getConnectedUserToClient();
		this.postAndPM = data.getPostAndPM();
	}
	@Override
	public boolean shouldTerminate() {
		return terminate;
	}

	@Override
	public void start(int connectionId, Connections<String> connections) {
		this.id = connectionId;
		this.connections = connections;
		user = null;
		terminate = false;
	}

	@Override
	public void process(String message) {
		String[] messageArr = message.split("\0");
		String opCode = messageArr[0];
		switch(opCode) {
			case "1":
				String username = messageArr[1];
				String password = messageArr[2];
				synchronized (UsersToDataMap) {
					if(UsersToDataMap.containsKey(username))
						connections.send(id,generateMessage("ERROR", 1));
					else {
						UsersToDataMap.put(username, new UserData(username, password));
						connections.send(id,generateMessage("ACK", 1));
					}
				}
				break;
			case "2":
				String username2 = messageArr[1];
				String password2 = messageArr[2];
				if(user !=null)
					connections.send(id, generateMessage("ERROR", 2));
				else synchronized (UsersToDataMap) {
					if(!UsersToDataMap.containsKey(username2)||!password2.equals(UsersToDataMap.get(username2).getPassword())||ConnectedUserToClient.containsKey(username2))
						connections.send(id, generateMessage("ERROR", 2));
					else {
						ConnectedUserToClient.put(username2, id);
						connections.send(id,generateMessage("ACK", 2));
						user = UsersToDataMap.get(username2);
						ConcurrentLinkedQueue<String> pendingMessages = user.getPendingMessages();
						while(!pendingMessages.isEmpty()) {
							connections.send(id, pendingMessages.poll());
						}
					}
				}
				break;
			case "3":
				if(user == null)
					connections.send(id, generateMessage("ERROR", 3));
				else {
					synchronized (user) {
						connections.send(id, generateMessage("ACK", 3));
						ConnectedUserToClient.remove(user.getUserName());
						user = null;
						terminate = true;
					}
				}
				break;
			case "4":
				if(user==null)
					connections.send(id, generateMessage("ERROR", 4));
				else {
					int follow_unFollow = Integer.parseInt(messageArr[1]);
					int numOfUsers = Integer.parseInt(messageArr[2]);
					Set<String> following = user.getFollowing();
					int numOfUsersToAck = 0;
					String UserListToAck = "";
					if(follow_unFollow == 0 && numOfUsers > 0) {
						for(int i = 3; i < messageArr.length;i++) {
							if((!following.contains(messageArr[i]))&&(UsersToDataMap.containsKey(messageArr[i])
									&&(!user.getUserName().equals(messageArr[i])))) {  
								following.add(messageArr[i]);
								numOfUsersToAck++;
								UserListToAck += messageArr[i]+'\0';
								UsersToDataMap.get(messageArr[i]).getFollowers().add(user.getUserName());
							}
							
						}
					}
					else if(follow_unFollow == 1 && numOfUsers > 0) {
						for(int i = 3; i < messageArr.length; i++)
							if(following.contains(messageArr[i])) {
								following.remove(messageArr[i]);
								numOfUsersToAck++;
								UserListToAck += messageArr[i]+'\0';
								UsersToDataMap.get(messageArr[i]).getFollowers().remove(user.getUserName());
							}
					}
					if(numOfUsers < 1 || numOfUsersToAck == 0) {
						connections.send(id, generateMessage("ERROR", 4));
					}
					else
						connections.send(id, generateMessage("ACK", 4)+String.valueOf(numOfUsersToAck)+'\0'+UserListToAck);
				}
				
				break;
			case "5":
				String content = messageArr[1];
				if(user == null) {
					connections.send(id, generateMessage("ERROR", 5));
				}
				else {
					connections.send(id, generateMessage("ACK", 5));
					postAndPM.add(content);
					user.getPosts().add(content);
					Vector<String> usersToSendPost = new Vector<>();
					String[] contentStrings = content.split(" ");
					for(int i = 0; i < contentStrings.length; i++)
						if(contentStrings[i].indexOf('@') != -1) {
							String temp = contentStrings[i].substring(contentStrings[i].indexOf('@')+1);
							if(UsersToDataMap.containsKey(temp)&&(!usersToSendPost.contains(temp)))      
							usersToSendPost.add(temp);
						}
					Set<String> followers = user.getFollowers();
					for(String i : followers) {
						if(!usersToSendPost.contains(i))
							usersToSendPost.addElement(i);
					}
					for(String i : usersToSendPost)
						sendMsgServerToClient(i, "9"+'\0'+"1"+user.getUserName()+'\0'+content+'\0');
				}
				break;
			case "6":
				String username6 = messageArr[1];
				String content6 = messageArr[2];
				if(user == null || !UsersToDataMap.containsKey(username6))
					connections.send(id, generateMessage("ERROR", 6));
				else {
					connections.send(id, generateMessage("ACK", 6));
					postAndPM.add(content6);
					user.getPM().add(content6);
					sendMsgServerToClient(username6
							, "9"+'\0'+"0"+user.getUserName()+'\0'+content6+'\0');
				}
				break;
			case "7":
				if(user == null) {
					connections.send(id, generateMessage("ERROR", 7));
				}
				else {
					String UsersNameList = "";
					for(String i : UsersToDataMap.keySet()) 
						UsersNameList += i+'\0';
					connections.send(id, generateMessage("ACK", 7)+UsersToDataMap.size()+'\0'+UsersNameList+'\0');
				}
				break;
			case "8":
				String username8 = messageArr[1];
				if(user == null ||!UsersToDataMap.containsKey(username8))
					connections.send(id, generateMessage("ERROR", 8));
				else {
					int NumPosts = UsersToDataMap.get(username8).getPosts().size();
					int NumFollowers = UsersToDataMap.get(username8).getFollowers().size();
					int NumFollowing = UsersToDataMap.get(username8).getFollowing().size();
					connections.send(id, generateMessage("ACK", 8)+NumPosts+'\0'+NumFollowers+'\0'+NumFollowing+'\0');
				}
		}
	}
	
	private void sendMsgServerToClient(String userName, String s) {
		if(ConnectedUserToClient.containsKey(userName))
			connections.send(ConnectedUserToClient.get(userName), s);
		else
			UsersToDataMap.get(userName).getPendingMessages().add(s);	
	}
	
	private String generateMessage(String s, Integer num) {
		switch(s) {
		case "ACK":
			return "10"+'\0'+num+'\0';
		case "ERROR":
			return "11"+'\0'+num+'\0';
		}
		return null;
	}

}
