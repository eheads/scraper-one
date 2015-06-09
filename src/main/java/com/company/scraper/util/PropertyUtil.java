package com.company.scraper.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.company.scraper.constant.Constant;
import com.company.scraper.exception.ApplicationException;



public final class PropertyUtil {

	private static final Logger log = LogManager.getLogger(PropertyUtil.class);
	//private static final String FILE_PATH = "./conf/application.properties";

	public static String getValue(String key) throws ApplicationException{
		String value = null;

		Properties applicationProperties = new Properties();
		FileInputStream file;

		try {
			//load the file handle for application.properties
			file = new FileInputStream(Constant.PROPERTIES_FILE_PATH);

			//load all the properties from this file
			applicationProperties.load(file);

			//we have loaded the properties, so close the file handle
			file.close();

			value = applicationProperties.getProperty(key);

		}catch (FileNotFoundException e) {
			log.error(e.getMessage());
			throw new ApplicationException(e.getMessage());
		}catch (IOException e) {
			log.error(e.getMessage());
		}
		return value;
	}

	public static int getShiftInterval(String key) throws ApplicationException{
		String interval = getValue(key);
		return Integer.parseInt(interval);
	}

	public static String getShift1StartTime(String key) throws ApplicationException{
		return getShiftTime(key);
	}

	public static String getShift1EndTime(String key) throws ApplicationException{
		return getShiftTime(key);
	}

	public static String getShift2StartTime(String key) throws ApplicationException{
		return getShiftTime(key);
	}
	public static String getShift2EndTime(String key) throws ApplicationException{
		return getShiftTime(key);
	}

	private static String getShiftTime(String key) throws ApplicationException{
		return getValue(key);
	}
}