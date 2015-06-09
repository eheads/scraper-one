package com.company.scraper.service.impl;

import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sun.util.calendar.ZoneInfo;

import com.company.scraper.constant.Constant;
import com.company.scraper.exception.ApplicationException;
import com.company.scraper.exception.TimeZoneException;
import com.company.scraper.service.ScraperService;
import com.company.scraper.task.ScheduledTask;
import com.company.scraper.util.PropertyUtil;

public class ScraperServiceImpl implements ScraperService {

	private static final Logger log = LogManager.getLogger(ScraperServiceImpl.class);

	@Override
	public void start() throws ApplicationException {

		int interval = PropertyUtil.getShiftInterval(Constant.KEY_SHIFT_1_INTERVAL_MINUTES);
		String startTime = PropertyUtil.getShift1StartTime(Constant.KEY_SHIFT_1_START_TIME); 
		String endTime = PropertyUtil.getShift1EndTime(Constant.KEY_SHIFT_1_END_TIME);
		String timeZone = PropertyUtil.getShift1EndTime(Constant.KEY_SHIFT_1_TIME_ZONE);
		
		int interval2 = PropertyUtil.getShiftInterval(Constant.KEY_SHIFT_2_INTERVAL_MINUTES);
		String startTime2 = PropertyUtil.getShift1StartTime(Constant.KEY_SHIFT_2_START_TIME); 
		String endTime2 = PropertyUtil.getShift1EndTime(Constant.KEY_SHIFT_2_END_TIME);
		String timeZone2 = PropertyUtil.getShift1EndTime(Constant.KEY_SHIFT_2_TIME_ZONE);

		validate(1, interval, startTime, endTime, timeZone);
		validate(2, interval2, startTime2, endTime2, timeZone2);

		ScheduledExecutorService ses = Executors.newScheduledThreadPool(3);

		ScheduledTask sched = new ScheduledTask(1, interval, startTime, endTime, timeZone);
        ses.scheduleAtFixedRate(sched, -1, interval, TimeUnit.MINUTES);
        
        ScheduledTask sched2 = new ScheduledTask(2, interval2, startTime2, endTime2, timeZone2);
        ses.scheduleAtFixedRate(sched2, -1, interval2, TimeUnit.MINUTES);

		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
	}

	private void validate(int shiftId, int interval, String startTime, String endTime, String timeZone) throws TimeZoneException{

		log.info("[Shift#{}] Configured TimeZone id: {}",shiftId, timeZone);
		log.info("[Shift#{}]System TimeZone: {}",shiftId, ZoneInfo.getDefault());
		TimeZone t = TimeZone.getTimeZone(timeZone);
		log.info("[Shift#{}]Configured TimeZone: {}",shiftId, t);
		
		if(t == null){
			throw new TimeZoneException("Unrecognized time zone!");
		}

	}
}
