/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved  */
package de.cranix.dao.controller;

import de.cranix.dao.Session;



import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Runnable;
import java.net.Socket;
import de.cranix.dao.User;
import java.util.Scanner;

import javax.persistence.EntityManager;


public class GetJPAInf extends Controller implements Runnable {
	private Socket client;

	public GetJPAInf(Session session,EntityManager em,Socket client) {
		super(session,em);
		this.client = client;
	}

	public void run(){
		final DeviceController deviceController = new DeviceController(this.session,this.em);;
		String ip;
	//	BufferedReader in = null;
		Scanner in = null;
		PrintWriter out = null;
		try{
	//in = new BufferedReader(new )
	//				InputStreamReader(client.getInputStream()));
			in  = new Scanner(client.getInputStream());
			out = new 
					PrintWriter(client.getOutputStream(), true);
		} catch (IOException e) {
			System.out.println("in or out failed");
			in.close();
			System.exit(-1);
		}

		while(true){
				ip = in.nextLine();
				 if( deviceController.getLoggedInUsersObject(ip).isEmpty() ) {
	                 out.println("ERR user=\"No user logged in " + ip + "\"");
	             } else {
	            	 User user = deviceController.getLoggedInUsersObject(ip).get(0);
	            	 //TODO check internetDisabled
	                 out.println("OK user=\"" + user.getUid() + "\"");
	             }
		}
	}
}
