package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BGSProtocol;
import bgu.spl.net.api.bidi.DataBase;
import bgu.spl.net.srv.bidi.EncDec;
import bgu.spl.net.srv.bidi.Server;

public class ReactorMain {

	public static void main(String[] args) {
		DataBase data = new DataBase();
		
		Server.reactor(Integer.parseInt(args[1]),
				Integer.parseInt(args[0]), 
				()-> new BGSProtocol(data),
				()-> new EncDec()).serve();
	}

}
