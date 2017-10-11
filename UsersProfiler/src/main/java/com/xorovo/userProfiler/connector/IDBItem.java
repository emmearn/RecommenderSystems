package com.xorovo.userProfiler.connector;

import java.sql.SQLException;

import com.xorovo.exception.DBEmptyException;

/**
 * @author Marco Arnone
 */
public interface IDBItem
{
	public void setDataSource() throws SQLException;
	public void addEvent(Event toAdd) throws DBEmptyException;
	public void addTags();
	public void addProfiles(String tag);
}
