package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.BGSProtocol;
import bgu.spl.net.api.bidi.DataBase;
import bgu.spl.net.srv.bidi.EncDec;
import bgu.spl.net.srv.bidi.Server;

public class TPCMain {

	public static void main(String[] args) {
		DataBase data = new DataBase();
		
		Server.threadPerClient(Integer.parseInt(args[0]), 
				()-> new BGSProtocol(data),
				()-> new EncDec()).serve();
	}
}
