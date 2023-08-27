/**
 * 
 */
package com.rs.sql;

/**
 * @author dragonkk(Alex)
 * Mar 30, 2017
 */
public abstract class SQLUpdate extends SQLTask<Object> {
	
	/**
	 * An sql update statement.
	 * If you wish to set it as a prepared statement, define a key and override set(statement) to set data
	 */
	public SQLUpdate(String sql) {
		super(sql);
	}
	
	public SQLUpdate(Object key, String sql) {
		super(key, sql);
	}
	
	public abstract void run(Object result);
}
