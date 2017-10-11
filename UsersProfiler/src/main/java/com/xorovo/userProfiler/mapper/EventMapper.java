package com.xorovo.userProfiler.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.xorovo.userProfiler.connector.Event;

/**
 * @author Marco Arnone
 */
public class EventMapper implements RowMapper<Event>
{
	@Override
	public Event mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		return new Event(rs.getInt("id"), rs.getInt("article_id"), rs.getInt("device_id"));
	}
}
