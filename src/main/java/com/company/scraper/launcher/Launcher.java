package com.company.scraper.launcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.company.scraper.exception.ApplicationException;
import com.company.scraper.service.ScraperService;
import com.company.scraper.service.impl.ScraperServiceImpl;

public class Launcher {

	private static final Logger log = LogManager.getLogger(Launcher.class);
	
	public static void main(String[] args) throws ApplicationException{
		log.info("Enter launcher...");
		
		ScraperService service = new ScraperServiceImpl();
		service.start();
	}
}
