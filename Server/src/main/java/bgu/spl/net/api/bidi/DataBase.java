package bgu.spl.net.api.bidi;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class DataBase {
	
	private  ConcurrentHashMap<String,UserData> UsersToDataMap ;
	private  ConcurrentHashMap<String,Integer> ConnectedUserToClient ;
	private  LinkedList<String> postAndPM ;
	
	public DataBase() {
		UsersToDataMap = new ConcurrentHashMap<>();
		ConnectedUserToClient = new ConcurrentHashMap<>();
		postAndPM = new LinkedList<>();
	}

	public ConcurrentHashMap<String, UserData> getUsersToDataMap() {
		return UsersToDataMap;
	}

	public ConcurrentHashMap<String, Integer> getConnectedUserToClient() {
		return ConnectedUserToClient;
	}

	public LinkedList<String> getPostAndPM() {
		return postAndPM;
	}
	
	
}
