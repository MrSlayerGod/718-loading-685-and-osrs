package mgitests;

import java.util.concurrent.ConcurrentLinkedQueue;

public class QTest {

	
	public static void main(String[] args) {
		ConcurrentLinkedQueue<Integer> q = new ConcurrentLinkedQueue<Integer>();
		q.add(1);
		q.add(2);
		q.add(3);
		
		
		System.out.println(q.poll());
	
	}

	
}
