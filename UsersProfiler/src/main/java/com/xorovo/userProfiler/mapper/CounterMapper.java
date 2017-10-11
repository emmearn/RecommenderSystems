package com.xorovo.userProfiler.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Marco Arnone
 */
public class CounterMapper implements RowMapper<Integer>
{
	@Override
	public Integer mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		Integer id = new Integer(rs.getInt("counter")); 
		return id;
	}
}
