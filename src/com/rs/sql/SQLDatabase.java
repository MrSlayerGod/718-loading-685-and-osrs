/**
 * 
 */
package com.rs.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * First attempt at a sql system. >-<. 
 * Possible future expansion: Add support 4 batches(not that it increases perfomance that much)
 * @author dragonkk(Alex)
 * Mar 30, 2017
 */
public class SQLDatabase implements Runnable {
	
	
	private static final int SQL_TIME_OUT = 5000;
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			DriverManager.setLoginTimeout(SQL_TIME_OUT); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static final int MAX_CACHED_REQUESTS = 1000000; //1m and thats huge tbh, might even be too much
	
	private String host, database, username, password;
	private Connection connection;
	@SuppressWarnings("rawtypes")
	private Queue<SQLTask> requests;
	private Map<Object, PreparedStatement> preparedStatements;
	private Thread thread;
	
	/**
	 * Creates a sql database service
	 * Close it by calling finish()
	 * Suggestion: use submit when working with data that cant be lost such as player files
	 * Use immediate for data that doesnt matter if lost (such as logs/highscores)
	 * @param host
	 * @param database
	 * @param username
	 * @param password
	 * @param immediateOnly - Notice that in immediate mode you wont be able to submit sql tasks, only run them immediately
	 */
	@SuppressWarnings("rawtypes")
	public SQLDatabase(String host, String database, String username, String password, boolean immediateOnly) {
		this.host = host;
		this.database = database;
		this.username = username;
		this.password = password;
		preparedStatements = new HashMap<Object, PreparedStatement>();
		if(!immediateOnly)
			requests = new LinkedBlockingQueue<SQLTask>();
	}
	
	@SuppressWarnings("deprecation")
	private void resume() {
		if(thread == null) {
			thread = new Thread(this);
			//thread.setDaemon(true); //if set true, automatically ends when server finishes
			thread.setName("SQLDatabase "+database+" Thread");
			thread.start();
		}else
			thread.resume();
	}
	
	public void finish() {
		if(thread != null)
			thread.interrupt();
		disconnect();
	}
	
	@SuppressWarnings("deprecation")
	private void pause() {
		thread.suspend();
	}
	

	//do not call this
	public int getRequestsCount() {
		synchronized(requests) {
			return requests.size();
		}
	}
	/**
	 * submits a task that gets queued and waits until processed
	 * if too many tasks queued(over a million) due to connection offline or not enough processing power it switches to immediate mode after
	 * 1m tasks. notice that if immediate mode also fails then it calls failed()
	 */
	public void submit(@SuppressWarnings("rawtypes") SQLTask task) {
		if(task.getSQL().isEmpty())
			return;
		synchronized(requests) {
			if(requests.size() >= MAX_CACHED_REQUESTS) {
				immediate(task);
				return;
			}
			requests.offer(task);
		}
		if(requests.size() == 1) 
			resume();
		
	}
	
	//temporary
	//public static ScheduledExecutorService slowExecutor = Executors.newScheduledThreadPool(4);
	
	/**
	 * notice that immediate mode tasks will fail if not connected
	 */
	public void immediate(@SuppressWarnings("rawtypes") SQLTask task) {
		/*slowExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					try {
						if(connection == null || !connection.isValid(SQL_TIME_OUT)) 
							connect();
						process(task);
					}catch(Throwable e) { //failed. 
						e.printStackTrace();
						try {
							task.failed(e);
						}catch(Throwable e2) {
							e2.printStackTrace();
						}
					}
				} catch (Throwable e) {
					Logger.handle(e);
				}
			}
		});*/
	}
	
	public static void main(String[] args) throws SQLException {
		DriverManager.getConnection("jdbc:mysql://162.218.48.74:3306/onyxftwc_pay", "onyxftwc_store001", "5B15jLqi#[Fz");

	}
	
	private boolean connect() {
		disconnect();
		try {
			//debugging &logger=com.mysql.jdbc.log.Slf4JLogger&profileSQL=true
			DriverManager.setLoginTimeout(SQL_TIME_OUT);
			connection = DriverManager.getConnection("jdbc:mysql://"+host+"/"+database+"?useSSL=false&useServerPrepStmts=false&rewriteBatchedStatements=true&useCompression=true", username, password);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void disconnect() {
		if(connection != null) {
			preparedStatements.clear();
			try {
				connection.close();
			}catch(Throwable e) {
				
			}
		}
	}

	@Override
	public void run() {
		for(;;) {
			try {
				if((connection == null || !connection.isValid(SQL_TIME_OUT)) && !connect()) 
					continue;
				for(;;) {
					if(!connection.isValid(SQL_TIME_OUT))
						break;	
					@SuppressWarnings("rawtypes")
					SQLTask query;
					synchronized(requests) {
						query = requests.peek();
					}
					process(query);
					synchronized(requests) {
						requests.remove();
					}
					if(requests.size() == 0) 
						pause();
				}
			}catch(Throwable e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	private void process(@SuppressWarnings("rawtypes") SQLTask task) throws SQLException {
		try {
			Object result;
			if(task.getKey() != null) {
				PreparedStatement statement = preparedStatements.get(task.getKey());
				if(statement == null)
					preparedStatements.put(task.getKey(), statement = connection.prepareStatement(task.getSQL()));
				try {
					for(int i = 0; i < task.getBatchCount(); i++)
						task.set(i, statement);
				}catch(SQLException e2) {
					e2.printStackTrace();
					try {
						task.failed(e2);
					}catch(Throwable e3) {
						e2.printStackTrace();
					}
					return;
				}
				result = task.getBatchCount() > 1 ? statement.executeBatch() : task instanceof SQLQuery ? statement.executeQuery() : statement.executeUpdate();
				statement.clearParameters();
			}else{
				Statement statement = connection.createStatement();
				statement.closeOnCompletion();
				result = task instanceof SQLQuery ? statement.executeQuery(task.getSQL()) : statement.executeUpdate(task.getSQL());
			}
			try {
				task.run(result);
			}catch(Throwable e2) {
				e2.printStackTrace();
			}
		}catch(Throwable e) {
			try {
				task.failed(e);
			}catch(Throwable e2) {
				e2.printStackTrace();
			}
		}

	}
}
