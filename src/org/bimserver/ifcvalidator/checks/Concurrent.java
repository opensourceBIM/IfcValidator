package org.bimserver.ifcvalidator.checks;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Concurrent {
	private ThreadPoolExecutor threadPoolExecutor;

	public Concurrent(int capacity) {
		if (capacity > 0) {
			threadPoolExecutor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 1, TimeUnit.HOURS, new ArrayBlockingQueue<>(capacity));
		}
	}

	public void run(Runnable runnable) {
		threadPoolExecutor.submit(runnable);
	}
	
	public void await() {
		if (threadPoolExecutor != null) {
			threadPoolExecutor.shutdown();
			try {
				threadPoolExecutor.awaitTermination(1, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}