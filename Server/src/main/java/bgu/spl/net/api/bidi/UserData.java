package bgu.spl.net.api.bidi;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UserData {
	private boolean login;
	private String userName;
	private String password;
	private Set<String> followers;
	private Set<String> following;
	private ConcurrentLinkedQueue<String> posts;
	private ConcurrentLinkedQueue<String> PM;
	private ConcurrentLinkedQueue<String> pendingMessages;
	
	

	public UserData(String userName,String password ) {
		login = false;
		this.userName = userName;
		this.password = password;
		followers = new HashSet<>();
		following = new HashSet<>();
		posts = new ConcurrentLinkedQueue<>();
		PM = new ConcurrentLinkedQueue<>();
		pendingMessages = new ConcurrentLinkedQueue<>();
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public String getUserName() {
		return userName;
	}


	public Set<String> getFollowers() {
		return followers;
	}


	public Set<String> getFollowing() {
		return following;
	}


	public ConcurrentLinkedQueue<String> getPosts() {
		return posts;
	}


	public ConcurrentLinkedQueue<String> getPM() {
		return PM;
	}
	
	public void login() {
		login = !login;
	}

	public ConcurrentLinkedQueue<String> getPendingMessages() {
		return pendingMessages;
	}
	

}
