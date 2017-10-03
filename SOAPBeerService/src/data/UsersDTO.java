package data;

import java.io.*;

class UsersDTO{
	
	String username;
	String password;
	int age;
	String accessLevel;
	
	//Constructor
	public UsersDTO(String username, String password, int age, String accessLevel) {
		
		this.username = username;
		this.password = password;
		this.age = age;
		this.accessLevel = accessLevel;
	}

	@Override
	public String toString() {
		return "UsersDTO [Username=" + username + ", password=" + password + ","
				+ " age=" + age + ", accessLevel=" + accessLevel + "]";
	}
	
	
	
	
	
	
}