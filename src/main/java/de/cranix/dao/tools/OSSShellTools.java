/* (c) 2017 Péter Varkoly <peter@varkoly.de> - all rights reserved */
package de.cranix.dao.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSSShellTools {

		static Logger logger = LoggerFactory.getLogger(OSSShellTools.class);
		public static int exec(String program[], StringBuffer reply, StringBuffer error, String request, boolean log) {

		int procResult = -10000;
		try {
			Process proc = Runtime.getRuntime().exec(program);
			InputStream errorStrm    = proc.getErrorStream();
			InputStream replyStrm    = proc.getInputStream();
			OutputStream requestStrm = proc.getOutputStream();
			if (request != null) {
				logger.debug(request);
				requestStrm.write(request.getBytes("UTF-8"));
			}
			requestStrm.close();

			final int BUFSIZE = 2000;
			int readCtrError = 0;
			int readCtrReply = 0;

			boolean processTerminated = false;
			byte[] buffer = new byte[BUFSIZE];
			do {
				readCtrError = 0;
				readCtrReply = 0;
				if (errorStrm.available() > 0 || processTerminated) {
					do {
						readCtrError = errorStrm.read(buffer, 0, BUFSIZE);
						if (readCtrError > 0) {
							String tmp = new String(buffer, 0, readCtrError, "UTF-8");
							error.append(tmp);
						}
					} while (readCtrError >= 0);
				}
				if (replyStrm.available() > 0 || processTerminated) {
					do {
						readCtrReply = replyStrm.read(buffer, 0, BUFSIZE);

						if (readCtrReply > 0) {
							String tmp = new String(buffer, 0, readCtrReply, "UTF-8");
							// log.debug("replyStream data: " + tmp);
							reply.append(tmp);

						}
					} while (readCtrReply >= 0);

				}
				try {
					if (!processTerminated) {
						procResult = proc.exitValue();
						processTerminated = true;
					}
				} catch (IllegalThreadStateException e) {

				}
			} while (!processTerminated || readCtrError >= 0 || readCtrReply >= 0);

		} catch (IOException e) {
			error.append(e.getMessage());
		}
		if( log ) {
			String uuId = UUID.randomUUID().toString();
			//TODO we have to implement this
		}
		return procResult;
	}

	public static int exec(String program[], StringBuffer reply, StringBuffer error, String request) {
		return exec(program,reply,error,request,true);
	}
}
