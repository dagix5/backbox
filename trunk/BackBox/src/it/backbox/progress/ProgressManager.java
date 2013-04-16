package it.backbox.progress;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;

public class ProgressManager {
	
	public static final String DOWNLOAD_ID = "download";
	public static final String UPLOAD_ID = "upload";
	
	private static ProgressManager istanza;
	
	private Map<String, TokenBucket> bucket;
	private Map<String, Integer> speed;
	
	private Map<String, ProgressListener> progressMap;
	
	private ProgressManager() {
		progressMap = new HashMap<>();
		bucket = new HashMap<>();
		speed = new HashMap<>();
	}
	
	public static ProgressManager getInstance() {
		if (istanza == null)
			istanza = new ProgressManager();
		return istanza;
	}
	
	public ProgressListener getListener(String id) {
		if (id != null)
			return progressMap.get(id);
		return null;
	}
	
	public void setListener(String id, ProgressListener listener) {
		progressMap.put(id, listener);
	}
	
	private TokenBucket getBucket(String id) {
		if (!bucket.containsKey(id))
			bucket.put(id, TokenBuckets.newFixedIntervalRefill(getSpeed(id), getSpeed(id), 1, TimeUnit.SECONDS));
		return bucket.get(id);
	}
	
	public void consume(String id, long tokens) {
		if (getSpeed(id) > 0)
			getBucket(id).consume(tokens);
	}
	
	public int getSpeed(String id) {
		if ((id != null) && speed.containsKey(id))
			return speed.get(id);
		return 0;
	}

	public void setSpeed(String id, int speed) {
		if (id == null)
			return;
		this.speed.put(id, speed);
		bucket.put(id, TokenBuckets.newFixedIntervalRefill(getSpeed(id), getSpeed(id), 1, TimeUnit.SECONDS));
	}
}
