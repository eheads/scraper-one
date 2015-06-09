package com.company.scraper.exception;

public class TimeZoneException extends ApplicationException {
	
	private static final long serialVersionUID = 1L;
	
	public TimeZoneException(String message) {
        super(message);
		//logger.error(message);
    }
	public TimeZoneException(Exception e) {
        super(e.getMessage());
		//logger.error(e.getMessage());
    }
    public TimeZoneException(String message, Throwable exception) {
        super("TimeZoneException: " + message, exception);
		//logger.error(message, exception);
    }
}
