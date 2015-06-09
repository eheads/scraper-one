package com.company.scraper.task;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.company.scraper.constant.Constant;
import com.company.scraper.util.DateUtil;

public class ScheduledTask implements Runnable{
	private static final Logger log = LogManager.getLogger(ScheduledTask.class);

	private int taskId;
	private int interval;
	private String startTime;
	private String endTime;
	private String timeZone;
	private TimeZone configuredTimeZone;
	private String lastPublishedTime;
	private String lastResult = "";
	//private static Vector<String> scrapedData = new Vector<String>();
	private List<String> scrapedData = new ArrayList<String>();

	public ScheduledTask(int taskId, int interval, String startTime, String endTime, String timeZone) {
		super();
		this.taskId = taskId;
		this.interval = interval;
		this.startTime = startTime;
		this.endTime = endTime;
		this.timeZone = timeZone;
		this.configuredTimeZone = getTimeZoneById(timeZone);
	}

	@Override
	public void run() {
		log.info("[Shift#{}] Enter run.",taskId);

		synchronized (this) {
			startProcess();
		}

		log.info("[Shift#{}] Exit run.",taskId);
	}

	private void startProcess(){
		Calendar date = Calendar.getInstance();
		long delay = 0;
		long now = DateUtil.convertDatetoServerDate(date.getTimeInMillis(), date.getTimeZone());
		long start = DateUtil.getScheduleInServerTime(startTime, configuredTimeZone);
		long end = DateUtil.getScheduleInServerTime(endTime, configuredTimeZone);	

		log.info("[Shift#{}] Configured start time={}",taskId,startTime);
		log.info("[Shift#{}] Configured end time={}",taskId,endTime);

		log.info("[Shift#{}] now={}",taskId,DateUtil.formatDateToHHmm(now));
		log.info("[Shift#{}] Shift start time(in system time): {}",taskId,DateUtil.formatDateToHHmm(start));
		log.info("[Shift#{}] Shift end time(in system time): {}",taskId,DateUtil.formatDateToHHmm(end));

		//Requirement#4: Scrape only in shift times, out of shift time scraping must be interrupted
		if(start < now && now >= end || (now < start)){
			if(now >= end){
				delay = DateUtil.getNextDayScheduleInServerTime(startTime, configuredTimeZone);
				log.info("[Shift#{}] Shift has already ended. Will resume processing on {} Server Time with interval of {} minute/s...", 
						taskId, DateUtil.formatDateToYYYYMMMDDHHmm(delay), interval	);
			}else{
				delay = prepareShiftSchedule();
				log.info("[Shift#{}] Shift has not started yet. Will start processing on {} Server Time with interval of {} minute/s...", 
						taskId, DateUtil.formatDateToYYYYMMMDDHHmm(delay), interval);
			}

			//			try {
			//				TimeUnit.MILLISECONDS.sleep(delay);
			//			} catch (InterruptedException e) {
			//				log.error(e.getMessage());
			//			}
		}else{
			log.info("[Shift#{}] Start scraping...",taskId);
			scrape();
		}
	}

	private long prepareShiftSchedule(){
		log.info("[Shift#{}] Preparing schedule...",taskId);
		long result = DateUtil.getScheduleInServerTime(startTime, configuredTimeZone);
		log.info("[Shift#{}] Prepared schdule:{}",taskId, DateUtil.formatDateToYYYYMMMDDHHmm(result));

		return result;
	}

	public TimeZone getTimeZoneById(String id) {
		return TimeZone.getTimeZone(id);
	}

	private void scrape(){

		URL url = null;

		try {

			url = new URL("http://loto.tdlbox.com/simulator/draw/get/?page=draws_publishing");
			//url = new URL(Constant.URL_TO_SCRAPE);
			InputStream is = url.openStream();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringComments(true);
			factory.setCoalescing(true);

			Document d = factory.newDocumentBuilder().parse(new InputSource(is));

			//Element e = d.getDocumentElement();
			iterNode(d.getDocumentElement());

		} catch (MalformedURLException e) {
			log.error(e.getMessage());
		}catch (IOException e) {
			log.error(e.getMessage());
		} catch (SAXException e) {
			log.error(e.getMessage());
		} catch (ParserConfigurationException e) {
			log.error(e.getMessage());
		}

		appendToCSCV();
	}

	private void iterNode(Node e){
		for(int i=0; i < e.getChildNodes().getLength(); i++){
			Node node = e.getChildNodes().item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE  ) {
				iterNode(node);
			}else if (node.getNodeType() == Node.TEXT_NODE && 
					node.getParentNode().getNodeName().equalsIgnoreCase(Constant.HTML_TD)) {
				scrapedData.add(node.getNodeValue().trim());
				//log.info("node value: {}",node.getNodeValue());
			}
		}
	}

	private void appendToCSCV(){
		log.info("[Shift#{}] Enter appendToCSCV...",taskId);
		try {

			//CSV file already added by maven distribution build as ./file/data.csv
			FileWriter writer = new FileWriter(Constant.CSV_FILE_PATH, true);
			BufferedWriter out = new BufferedWriter(writer);
			StringBuffer partialCSV = new StringBuffer();


			log.info("[Shift#{}] scrapedData size: {}",taskId,scrapedData.size());
			log.info("lastResult: "+lastResult);
			log.info("lastPublishedTime: "+lastPublishedTime);

			int scrappedDateSize = scrapedData.size();

			if(scrappedDateSize==0){
				partialCSV.append(Constant.CSV_NO_DATA_FOUND_MSG)
				.append(Constant.COMMA_DELIMITER)
				.append(Constant.NEW_LINE_SEPARATOR);
			}else{
				log.info("[Shift#{}] Start writing to csv...",taskId);

				for(int i=0; i < scrappedDateSize; i++){
					String data = scrapedData.get(i);
					//Get only the result (last column)
					//Requirement#5: Should not allow consecutive duplicate
					//Check first <td> record
					if(lastPublishedTime != null && (i+1)%3==1){
						long lastPublishedTimeInMS = DateUtil.getScheduleInServerTime(lastPublishedTime, configuredTimeZone);
						long scrapedDataTimeInMS = DateUtil.getScheduleInServerTime(data, configuredTimeZone);

						log.info("[Shift#{}] lastPublishedTime: {}",taskId, DateUtil.formatDateToHHmm(lastPublishedTimeInMS));
						log.info("[Shift#{}] scrapedDataTime: {}",taskId, DateUtil.formatDateToHHmm(scrapedDataTimeInMS));

						if(lastPublishedTimeInMS < scrapedDataTimeInMS ){
							continue;
						}
					}

					//Check last <td> record
					if((i+1)%3==0){
						if(!lastResult.equals(data)){
							partialCSV.append(data)
							.append(Constant.COMMA_DELIMITER)
							.append(Constant.NEW_LINE_SEPARATOR);
							lastResult = data;
						}
					}	
				}

				lastPublishedTime = scrapedData.get(scrappedDateSize-3);
				log.info("[Shift#{}] lastPublishedTime: {}",taskId,lastPublishedTime);
				log.info("[Shift#{}] lastResult: {}",taskId, lastResult);
				log.info("[Shift#{}] Done writing to csv.",taskId);
			}

			out.append(partialCSV);
			out.close();
			partialCSV = new StringBuffer();
		} catch (IOException e) {
			log.info("[Shift#1{}] IOException encountered. Pls see error log for full details.",taskId);
			log.error(e.getMessage());
		} catch (Exception e) {
			log.info(e.getMessage());
		}

		scrapedData.clear();
		log.info("[Shift#{}] Exit appendToCSCV.",taskId);
	}
}