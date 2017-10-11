package com.xorovo.userProfiler.connector;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Marco Arnone
 */
public interface IDBTracking
{
	public JdbcTemplate setDataSource(String url, String username, String password);
	public List<Event> getEvent();
	public void updateCounter();
	public void setEventCounter();
}
