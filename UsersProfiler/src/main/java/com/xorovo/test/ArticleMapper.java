package com.xorovo.test;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.xorovo.userProfiler.connector.Event;

/**
 * @author Marco Arnone
 */
public class ArticleMapper implements RowMapper<Integer>
{
	@Override
	public Integer mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		return new Integer(rs.getInt("article"));
	}
}
