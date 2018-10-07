package net.rptools.maptool_fx.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;

public class LogHelper {
	public static String getLoggerFileName(Logger log) {
		org.apache.logging.log4j.core.Logger loggerImpl = (org.apache.logging.log4j.core.Logger) log;
		Appender appender = loggerImpl.getAppenders().get("LogFile");

		if (appender != null)
			return ((FileAppender) appender).getFileName();
		else
			return "NOT_CONFIGURED";
	}
}
