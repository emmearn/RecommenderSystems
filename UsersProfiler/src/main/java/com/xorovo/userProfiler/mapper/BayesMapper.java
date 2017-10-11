package com.xorovo.userProfiler.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author Marco Arnone
 */
public class BayesMapper implements RowMapper<Float>
{
	@Override
	public Float mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		Float id = new Float(rs.getFloat("rate")); 
		return id;
	}
}
