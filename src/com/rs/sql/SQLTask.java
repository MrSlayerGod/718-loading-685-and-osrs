/**
 * 
 */
package com.rs.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author dragonkk(Alex)
 * Mar 30, 2017
 */
public abstract class SQLTask<T> {
	
	private Object key; //for prepared statements
	private String sql; 
	
	public SQLTask(String sql) {
		this(null, sql);
	}
	
	/**
	 * @param key - caches the statement and acts as a prepared statement
	 * @param sql
	 */
	public SQLTask(Object key, String sql) {
		this.key = key;
		this.sql = sql;
	}
	
	public Object getKey() {
		return key;
	}
	
	public String getSQL() {
		return sql;
	}
	
	//only called if key isnt null.
	public void set(int batchID, PreparedStatement statement) throws SQLException {
		
	}
		
	//default 1. override this to add over a batch
	public int getBatchCount() {
		return 1;
	}
	
	//called during immediate mode if it fails.. sight.
	//immediate mode can be used for non important stuff which u dont wanna process no matter what
	public void failed(Throwable e) {
		
	}
	

	public abstract void run(T result) throws SQLException;
	
}
