package org.bimserver.ifcvalidator.checks;

/******************************************************************************
 * Copyright (C) 2009-2017  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

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