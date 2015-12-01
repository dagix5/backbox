package it.backbox.progress;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.isomorphism.util.TokenBucket;
import org.isomorphism.util.TokenBuckets;

public class ProgressManager {
	
	/** Default download operation stream id */
	public static final String DOWNLOAD_ID = "download";
	/** Default upload operation stream id */
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
	
	/**
	 * Get the instance of Progress Manager
	 * 
	 * @return The instance
	 */
	public static ProgressManager getInstance() {
		if (istanza == null)
			istanza = new ProgressManager();
		return istanza;
	}
	
	/**
	 * Get the ProgressListener instance of the stream with id <i>id</i>
	 * 
	 * @param id
	 *            Stream ID
	 * @return ProgressListener instance
	 */
	public ProgressListener getListener(String id) {
		if (id != null)
			return progressMap.get(id);
		return null;
	}
	
	/**
	 * Set the ProgressListener instance of the stream with id <i>id</i>
	 * 
	 * @param id
	 *            Stream ID
	 * @param ProgressListener
	 *            instance
	 */
	public void setListener(String id, ProgressListener listener) {
		progressMap.put(id, listener);
	}
	
	/**
	 * Get the Bucket to control the speed
	 * 
	 * @param id
	 *            Stream ID
	 * @return Token Bucket
	 */
	private TokenBucket getBucket(String id) {
		if (!bucket.containsKey(id))
			bucket.put(id, TokenBuckets.newFixedIntervalRefill(getSpeed(id), getSpeed(id), 1, TimeUnit.SECONDS));
		return bucket.get(id);
	}
	
	/**
	 * Consume <i>tokens</i> token from the Bucket with stream ID <i>id</i>
	 * 
	 * @param id
	 *            Stream ID
	 * @param tokens
	 *            Tokens to consume
	 */
	public void consume(String id, long tokens) {
		if ((getSpeed(id) > 0) && (tokens > 0))
			getBucket(id).consume(tokens);
	}
	
	/**
	 * Get the speed of the stream with ID <i>id</i>
	 * 
	 * @param id
	 *            Stream ID
	 * @return Stream speed
	 */
	public int getSpeed(String id) {
		if ((id != null) && speed.containsKey(id))
			return speed.get(id).intValue();
		return 0;
	}

	/**
	 * Set the speed of the stream with ID <i>id</i>
	 * 
	 * @param id
	 *            Stream ID
	 * @param Stream
	 *            speed
	 */
	public void setSpeed(String id, int speed) {
		if (id == null)
			return;
		this.speed.put(id, Integer.valueOf(speed));
		bucket.put(id, TokenBuckets.newFixedIntervalRefill(getSpeed(id), getSpeed(id), 1, TimeUnit.SECONDS));
	}
}
