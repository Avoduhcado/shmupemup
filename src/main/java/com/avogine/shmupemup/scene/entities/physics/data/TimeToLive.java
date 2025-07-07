package com.avogine.shmupemup.scene.entities.physics.data;

/**
 *
 */
public class TimeToLive {

	private float ttl;
	
	/**
	 * 
	 */
	public TimeToLive(float ttl) {
		this.ttl = ttl;
	}
	
	/**
	 * @return the ttl
	 */
	public float getTtl() {
		return ttl;
	}
	
	/**
	 * @param delta
	 */
	public void decrease(float delta) {
		ttl = Math.clamp(ttl - delta, 0, ttl);
	}
	
}
