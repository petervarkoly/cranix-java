package de.cranix.helper;

import java.util.Locale;
import static de.cranix.helper.CranixConstants.cranixBaseDir;

public class SslCrypto {
	
	public SslCrypto() {
		// TODO Auto-generated constructor stub
	}
	
	static public String enCrypt(String stringToEncrypt) {
		String[] program   = new String[1];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = cranixBaseDir + "tools/encrypt.sh";
		CrxSystemCmd.exec(program, reply, error, stringToEncrypt);
		return reply.toString();
	}
	
	static public String deCrypt(String stringToDecrypt) {
		String[] program   = new String[1];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = cranixBaseDir + "tools/decrypt.sh";
		CrxSystemCmd.exec(program, reply, error, stringToDecrypt);
		return reply.toString();
	}

}
