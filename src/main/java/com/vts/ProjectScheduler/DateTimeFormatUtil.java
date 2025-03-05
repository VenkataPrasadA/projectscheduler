package com.vts.ProjectScheduler;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

public class DateTimeFormatUtil {

	private static SimpleDateFormat regularDateFormat=new SimpleDateFormat("dd-MM-yyyy");
	private static SimpleDateFormat sqlDateFormat=new SimpleDateFormat("yyyy-MM-dd");
	
	public static  SimpleDateFormat getMonthNameAndYear() {
		return new SimpleDateFormat("MMM-yyyy");
	}
	public static  SimpleDateFormat getSqlDateFormat() {
		return sqlDateFormat;
	}
	public static  SimpleDateFormat getSqlDateAndTimeFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	public static  SimpleDateFormat getRegularDateFormat() {
		return regularDateFormat;
	}
	public static  SimpleDateFormat getDateMonthShortName() {
		return new SimpleDateFormat("dd-MMM-yyyy");
	}
	public static  SimpleDateFormat getDateMonthFullName() {
		return new SimpleDateFormat("dd-MMMM-yyyy");
	}
	
	
	

	public static  int getYearFromRegularDate(String datestring) 
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate ldate=LocalDate.parse(datestring,formatter);
		return ldate.getYear();
	}
	public static  int getYearFromSqlDate(String datestring) 
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate ldate=LocalDate.parse(datestring,formatter);
		return ldate.getYear();
		
	}
	public static  int getYearFromSqlDateAndTime(String datetimestring)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime ldatetime=LocalDateTime.parse(datetimestring,formatter);
		return ldatetime.getYear();
		
	}
	
	public static  int getMonthFromRegularDate(String datestring) 
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate ldate=LocalDate.parse(datestring,formatter);
		return ldate.getMonthValue();
	}
	public static  int getMonthFromSqlDate(String datestring) 
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate ldate=LocalDate.parse(datestring,formatter);
		return ldate.getMonthValue();
		
	}
	public static  int getMonthFromSqlDateAndTime(String datetimestring)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime ldatetime=LocalDateTime.parse(datetimestring,formatter);
		return ldatetime.getMonthValue();
		
	}
	
	public static  String getMonthValFromRegularDate(String datestring) 
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate ldate=LocalDate.parse(datestring,formatter);
		return ldate.getMonth().getDisplayName(TextStyle.SHORT,Locale.ENGLISH);
	}
	public static  String getMonthValFromSqlDate(String datestring) 
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate ldate=LocalDate.parse(datestring,formatter);
		return ldate.getMonth().getDisplayName(TextStyle.SHORT,Locale.ENGLISH);
		
	}
	public static  String getMonthValFromSqlDateAndTime(String datetimestring)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime ldatetime=LocalDateTime.parse(datetimestring,formatter);
		return ldatetime.getMonth().getDisplayName(TextStyle.SHORT,Locale.ENGLISH);		
	}
	
	
	public static  String getCurrentYear()
	{		
		return  String.valueOf(LocalDate.now().getYear());
	}
	
	public static  String getNextYear()
	{		
		return  String.valueOf(LocalDate.now().plusYears(1).getYear());
	}
	
	public static  String getPrevYear()
	{		
		return  String.valueOf(LocalDate.now().minusYears(1).getYear());
	}
	
	public static  String getCurrentMonthShortName()
	{
		return new DateFormatSymbols().getShortMonths()[LocalDate.now().getMonthValue()];
	}
	
	public static  String getCurrentMonthFullName()
	{
		return new DateFormatSymbols().getMonths()[LocalDate.now().getMonthValue()];
	}
	
	/*---------------------------------------------*/
	
	public static  String getFirstDayofCurrentMonthSqlFormat()
	{
		String firstday=LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString();
		return firstday;
	}
	
	public static  String getLastDayofCurrentMonthSqlFormat()
	{
		String lastday=LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).toString();
		return lastday;
	}
	
	public static  String getFirstDayofCurrentMonthRegularFormat() throws Exception
	{
		String firstday=regularDateFormat.format(sqlDateFormat.parse(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).toString()));
		return firstday;
	}
	
	public static  String getLastDayofCurrentMonthRegularFormat() throws Exception
	{
		String lastday=regularDateFormat.format(sqlDateFormat.parse(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).toString()));
		return lastday;
	}	
	
	
	
	public static  String getFinancialYearStartDateSqlFormat()
	{
		String firstday=LocalDate.now().getYear()+"-04-01";
		return firstday;
	}	
	
	public static  String getFinancialYearEndDateSqlFormat()
	{
		String lastday=LocalDate.now().plusYears(1).getYear()+"-03-31";
		return lastday;
	}
	public static  String getFinancialThisYearEndDateSqlFormat()
	{
		String lastday=LocalDate.now().getYear()+"-03-31";
		return lastday;
	}
	
	public static  String getFinancialYearStartDateRegularFormat() throws Exception
	{
		String firstday="01-04-"+LocalDate.now().getYear();
		return firstday;
	}
	
	public static  String getFinancialYearEndDateRegularFormat() throws Exception
	{
		String lastday="31-03-"+LocalDate.now().plusYears(1).getYear();
		return lastday;
	}
	
	public static  String getPreviousFinancialYearStartDateRegularFormat() throws Exception
	{
		String pfirstday="01-04-"+LocalDate.now().minusYears(1).getYear();
		return pfirstday;
	}
	public static  String getPreviousFinancialYearStartDateSqlFormat()
	{
		String pfirstday=LocalDate.now().minusYears(1).getYear()+"-04-01";
		return pfirstday;
	}	
	
	
	public static  String getCurrentYearStartDateSqlFormat()
	{
		String firstday=LocalDate.now().getYear()+"-01-01";
		return firstday;
	}	
	
	
	public static  String getCurrentYearEndDateSqlFormat()
	{
		String Lastday=LocalDate.now().getYear()+"-12-31";
		return Lastday;
	}	
	
	public static  String getCurrentYearStartDateRegularFormat()
	{
		String pfirstday="01-01-"+LocalDate.now().getYear();
		return pfirstday;
	}	
	
	public static  String SqlToRegularDate (String sqldate) throws ParseException
	{
		return regularDateFormat.format(sqlDateFormat.parse(sqldate));
	}

	public String RegularToSqlDate (String regulardate) throws ParseException
	{
		return sqlDateFormat.format(regularDateFormat.parse(regulardate));
	}
	public static java.sql.Date dateConversionSql(String sDate) {
		java.sql.Date ddate = null;

		SimpleDateFormat sdf4 = new SimpleDateFormat("dd-MM-yyyy");
		try {
			java.util.Date jdate = sdf4.parse(sDate);
			long ms = jdate.getTime();
			ddate = new java.sql.Date(ms);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return (ddate);
	}
	
	public static String getCurrentWeekMonday() {
	    LocalDate currentDate = LocalDate.now();
	    DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
	    int daysUntilMonday = DayOfWeek.MONDAY.getValue() - currentDayOfWeek.getValue();

	    if (daysUntilMonday > 0) {
	        daysUntilMonday -= 7; // Subtract 7 days to move back to the previous Monday if the current day is after Monday.
	    }

	    LocalDate currentWeekMonday = currentDate.plusDays(daysUntilMonday);
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	    return currentWeekMonday.format(formatter);
	}

	public static String getCurrentWeekSunday() {
	    LocalDate currentDate = LocalDate.now();
	    DayOfWeek currentDayOfWeek = currentDate.getDayOfWeek();
	    int daysUntilSunday = DayOfWeek.SUNDAY.getValue() - currentDayOfWeek.getValue();

	    if (daysUntilSunday <= 0) {
	        daysUntilSunday += 7; // Add 7 days to move to the next Sunday if the current day is already a Sunday.
	    }

	    LocalDate currentWeekSunday = currentDate.plusDays(daysUntilSunday);
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	    return currentWeekSunday.format(formatter);
	}
}
