package net.sf.iwant.planner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.iwant.entry.Iwant;
import net.sf.iwant.entry.Iwant.IwantException;

public class Planner {

	private final List<Thread> threads = new ArrayList<Thread>();
	private final TaskQueue queue;
	private Throwable failure;

	public Planner(Task rootTask, int workerCount) {
		log("Constructing");
		queue = new TaskQueue(rootTask);
		for (int i = 0; i < workerCount; i++) {
			Worker worker = new Worker(i);
			Thread thread = new Thread(worker, "w-" + i);
			threads.add(thread);
		}
		log("Constructed with " + workerCount + " workers.");
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

	private final AtomicInteger workersWorking = new AtomicInteger();

	String taskStartMessage(int workerId, int workersWorking, Task task) {
		StringBuilder b = new StringBuilder();
		b.append("(");
		b.append(workerId).append("/").append(workersWorking).append(" ")
				.append(dirtinessToString(queue.dirtiness(task))).append(" ")
				.append(task).append(")");
		return b.toString();
	}

	static String dirtinessToString(TaskDirtiness dirtiness) {
		switch (dirtiness) {
		case DIRTY_DESCRIPTOR_CHANGED:
			return "D~";
		case DIRTY_NO_CACHED_CONTENT:
			return "C!";
		case DIRTY_NO_CACHED_DESCRIPTOR:
			return "D!";
		case DIRTY_SRC_MODIFIED:
			return "S~";
		case NOT_DIRTY:
			return "  ";
		default:
			throw new UnsupportedOperationException("Unsupported dirtiness: "
					+ dirtiness);
		}
	}

	private class Worker implements Runnable {

		private final int id;

		Worker(int id) {
			this.id = id;
		}

		private void consoleLog(String msg) {
			System.err.println(msg);
			log(msg);
		}

		@Override
		public void run() {
			while (true) {
				TaskAllocation allocation = waitForNextTask();
				if (allocation == null) {
					return;
				}
				try {
					Task task = allocation.task();
					Map<ResourcePool, Resource> resources = allocation
							.allocatedResources();
					workersWorking.incrementAndGet();
					consoleLog(taskStartMessage(id, workersWorking.get(), task));
					task.refresh(resources);
				} catch (Throwable e) {
					consoleLog("(FAILED " + allocation.task() + ")");
					refreshFailed(e);
					return;
				} finally {
					workersWorking.decrementAndGet();
				}
				log(this, " finished ", allocation);
				markDone(allocation);
			}
		}

		@Override
		public String toString() {
			return "Worker-" + id;
		}

	}

	public void start() {
		log("Starting");
		for (Thread thread : threads) {
			thread.start();
		}
		log("Started " + threads.size() + " workers.");
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
		log("joined workers");
		Throwable refreshFailure = refreshFailure();
		if (refreshFailure != null) {
			// an "expected" case:
			if (refreshFailure instanceof IwantException) {
				throw (IwantException) refreshFailure;
			}
			// something unexpected happened:
			throw new IllegalStateException(refreshFailure);
		}
	}

}
