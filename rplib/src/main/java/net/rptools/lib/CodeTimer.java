package net.rptools.lib;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeTimer {
	private final Map<String, Timer> timeMap = new HashMap<String, Timer>();
	private final Map<String, Integer> orderMap = new HashMap<String, Integer>();
	private final String name;
	private final long created = System.currentTimeMillis();
	private boolean enabled;
	private int threshold = 1;

	private final DecimalFormat df = new DecimalFormat();

	public CodeTimer() {
		this("");
	}

	public CodeTimer(String n) {
		name = n;
		enabled = true;
		df.setMinimumIntegerDigits(5);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void start(String id) {
		if (!enabled) {
			return;
		}
		int count = orderMap.size();
		orderMap.put(id, count);
		Timer timer = timeMap.get(id);
		if (timer == null) {
			timer = new Timer();
			timeMap.put(id, timer);
		}
		timer.start();
	}

	public void stop(String id) {
		if (!enabled) {
			return;
		}
		if (!orderMap.containsKey(id)) {
			throw new IllegalArgumentException("Could not find orderMap id: " + id);
		}
		if (!timeMap.containsKey(id)) {
			throw new IllegalArgumentException("Could not find timer id: " + id);
		}
		timeMap.get(id).stop();
	}

	public long getElapsed(String id) {
		if (!enabled) {
			return 0;
		}
		if (!orderMap.containsKey(id)) {
			throw new IllegalArgumentException("Could not find orderMap id: " + id);
		}
		if (!timeMap.containsKey(id)) {
			throw new IllegalArgumentException("Could not find timer id: " + id);
		}
		return timeMap.get(id).getElapsed();
	}

	public void reset(String id) {
		if (!orderMap.containsKey(id)) {
			throw new IllegalArgumentException("Could not find orderMap id: " + id);
		}
		timeMap.remove(id);
	}

	public void clear() {
		orderMap.clear();
		timeMap.clear();
	}

	@Override
	public String toString() {
		StringBuffer builder = new StringBuffer(100);

		builder.append("Timer ").append(name).append(" (").append(orderMap.size()).append(" elements)\n");

		List<String> idSet = new ArrayList<String>(timeMap.keySet());
		Collections.sort(idSet, new Comparator<String>() {
			public int compare(String arg0, String arg1) {
				return orderMap.get(arg0) - orderMap.get(arg1);
			}
		});
		for (String id : idSet) {
			long elapsed = timeMap.get(id).getElapsed();
			if (elapsed < threshold) {
				continue;
			}
			builder.append(String.format("  %3d.  %6d ms  %s\n", orderMap.get(id), elapsed, id));
			//			builder.append("\t").append(orderMap.get(id)).append(". ").append(id).append(": ").append(timer.getElapsed()).append(" ms\n");
		}
		return builder.toString();
	}

	private static class Timer {
		long elapsed;
		long start = -1;

		public void start() {
			start = System.currentTimeMillis();
		}

		public void stop() {
			elapsed += (System.currentTimeMillis() - start);
			start = -1;
		}

		public long getElapsed() {
			long time = elapsed;
			if (start > 0) {
				time += (System.currentTimeMillis() - start);
			}
			return time;
		}
	}
}
