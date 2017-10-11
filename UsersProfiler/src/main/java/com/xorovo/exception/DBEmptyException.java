package com.xorovo.exception;

/**
 * @author Marco Arnone
 */
public class DBEmptyException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DBEmptyException(int numberDB)
	{
		super("The database " + Integer.toString(numberDB) + " is empty.");
	}
}
