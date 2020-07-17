package de.cranix.dao.tools;

import java.util.Locale;
import static de.cranix.dao.internal.CranixConstants.*;

public class SslCrypto {
	
	private static String basePath;
	static {
		String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
		if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
			basePath = "/usr/local/oss/";
		} else {
			basePath = cranixBaseDir;
		}
	}

	public SslCrypto() {
		// TODO Auto-generated constructor stub
	}
	
	static public String enCrypt(String stringToEncrypt) {
		String[] program   = new String[1];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = basePath + "tools/encrypt.sh";
		OSSShellTools.exec(program, reply, error, stringToEncrypt);
		return reply.toString();
	}
	
	static public String deCrypt(String stringToDecrypt) {
		String[] program   = new String[1];
		StringBuffer reply = new StringBuffer();
		StringBuffer error = new StringBuffer();
		program[0] = basePath + "tools/decrypt.sh";
		OSSShellTools.exec(program, reply, error, stringToDecrypt);
		return reply.toString();
	}

}
