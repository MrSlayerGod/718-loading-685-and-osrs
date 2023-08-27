/**
 * 
 */
package com.rs.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author dragonkk(Alex)
 * Mar 30, 2017
 */
public abstract class SQLQuery extends SQLTask<ResultSet> {

	/**
	 * An sql query statement.
	 * If you wish to set it as a prepared statement, define a key and override set(statement) to set data
	 */
	public SQLQuery(String sql) {
		super(sql);
	}
	
	public SQLQuery(Object key, String sql) {
		super(key, sql);
	}
	
	@Override
	public abstract void run(ResultSet result) throws SQLException;
}
