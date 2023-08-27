package com.rs.tools;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.rs.game.WorldTile;

public class Test {

	public static void main(String[] args) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
		
		try {
			String n = null;
			n.charAt(1);
		} catch (Throwable e) {
			e.printStackTrace();
			System.out.println("Caught");
		}
	}

}
