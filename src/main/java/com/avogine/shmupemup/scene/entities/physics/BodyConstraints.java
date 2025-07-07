package com.avogine.shmupemup.scene.entities.physics;

import org.joml.Vector3f;
import org.joml.primitives.AABBf;

/**
 * @param collider 
 * @param velocity 
 * @param acceleration 
 * @param mass 
 * @param categoryBitMask 
 * @param collisionBitMask 
 */
public record BodyConstraints(AABBf collider, Vector3f velocity, Vector3f acceleration, float mass, int categoryBitMask, int collisionBitMask) {
	
	/**
	 * @param collider
	 * @param velocity
	 * @param mass
	 * @param categoryBitMask 
	 * @param collisionBitMask 
	 */
	public BodyConstraints(AABBf collider, Vector3f velocity, float mass, int categoryBitMask, int collisionBitMask) {
		this(collider, velocity, new Vector3f(), mass, categoryBitMask, collisionBitMask);
	}
	
	/**
	 * @param collider
	 * @param mass
	 * @param categoryBitMask 
	 * @param collisionBitMask 
	 */
	public BodyConstraints(AABBf collider, float mass, int categoryBitMask, int collisionBitMask) {
		this(collider, new Vector3f(), mass, categoryBitMask, collisionBitMask);
	}
	
	/**
	 * @param collider
	 * @param categoryBitMask 
	 * @param collisionBitMask 
	 */
	public BodyConstraints(AABBf collider, int categoryBitMask, int collisionBitMask) {
		this(collider, 1f, categoryBitMask, collisionBitMask);
	}
	
}
