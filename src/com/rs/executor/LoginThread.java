package com.rs.executor;

import com.rs.login.Login;
import com.rs.utils.Logger;

public final class LoginThread extends Thread {
	protected LoginThread() {
		super("Login thread");
	}

	@Override
	public final void run() {
		while (!LoginExecutorManager.workerShutdown) {
			try {
				Login.process();
				Thread.sleep(20);
			} catch (Throwable t) {
				Logger.handle(t);
			}
		}
	}

}
