package net.sf.iwant.planner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.sf.iwant.entry.Iwant;

public class Planner {

	private final List<Thread> threads = new ArrayList<Thread>();
	private final TaskQueue queue;
	private Throwable failure;

	public Planner(Task rootTask, int workerCount) {
		queue = new TaskQueue(rootTask);
		for (int i = 0; i < workerCount; i++) {
			Worker worker = new Worker(i);
			Thread thread = new Thread(worker);
			threads.add(thread);
		}
	}

	private static void log(Object... msg) {
		Iwant.debugLog("Planner", Arrays.toString(msg));
	}

	private synchronized TaskAllocation waitForNextTask() {
		while (true) {
			if (failure != null) {
				log("Returning no more tasks because of failure: ", failure);
				return null;
			}
			TaskAllocation next = queue.next();
			if (next != null) {
				return next;
			}
			if (queue.isEmpty()) {
				// no tasks because queue is empty, please stop
				return null;
			}
			// there is still work to do but it's not yet doable
			try {
				wait();
			} catch (InterruptedException e) {
				log(e);
				throw new IllegalStateException(e);
			}
		}
	}

	private synchronized void markDone(TaskAllocation allocation) {
		queue.markDone(allocation);
		notifyAll();
	}

	private synchronized void refreshFailed(Throwable failure) {
		this.failure = failure;
		notifyAll();
	}

	private synchronized Throwable refreshFailure() {
		return failure;
	}

	private class Worker implements Runnable {

		private final int id;

		Worker(int id) {
			this.id = id;
		}

		private void consoleLog(String msg) {
			String fullMsg = "(" + id + " " + msg + ")";
			System.err.println(fullMsg);
			log(fullMsg);
		}

		@Override
		public void run() {
			log(this, " starts.");
			try {
				while (true) {
					log(this, " waits for next task.");
					TaskAllocation allocation = waitForNextTask();
					if (allocation == null) {
						log(this, " stops, no more tasks.");
						return;
					}
					consoleLog("" + allocation.task());
					try {
						Task task = allocation.task();
						Map<ResourcePool, Resource> resources = allocation
								.allocatedResources();
						task.refresh(resources);
					} catch (Throwable e) {
						consoleLog("failed    " + allocation.task());
						refreshFailed(e);
						return;
					}
					log(this, " finished ", allocation);
					markDone(allocation);
				}
			} finally {
				log(this, " ends.");
			}
		}

		@Override
		public String toString() {
			return "Worker-" + id;
		}

	}

	public void start() {
		for (Thread thread : threads) {
			thread.start();
		}
	}

	public void join() {
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				log(e);
				throw new IllegalStateException("TODO test this branch");
			}
		}
		Throwable refreshFailure = refreshFailure();
		if (refreshFailure != null) {
			throw new IllegalStateException(refreshFailure);
		}
	}

}
