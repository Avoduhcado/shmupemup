package com.avogine.shmupemup.scene.entities;

import org.joml.*;

/**
 * @param position 
 * @param orientation 
 * @param scale 
 * @param direction 
 * @param right 
 * @param up 
 * @param rotation 
 */
public record Transform(Vector3f position, Quaternionf orientation, Vector3f scale, Vector3f direction, Vector3f right, Vector3f up, Quaternionf rotation) {

	/**
	 * @param position 
	 * @param orientation 
	 * @param scale 
	 */
	public Transform(Vector3f position, AxisAngle4f orientation, float scale) {
		this(new Vector3f().set(position), new Quaternionf().set(orientation), new Vector3f(scale), new Vector3f(), new Vector3f(), new Vector3f(), new Quaternionf());
	}
	
}
