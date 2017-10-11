package com.xorovo.userProfiler.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Marco Arnone
 */
public class TagMapper implements RowMapper<String>
{
	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException
	{
		String tag = new String(rs.getString("tag")); 
		return tag;
	}
}
