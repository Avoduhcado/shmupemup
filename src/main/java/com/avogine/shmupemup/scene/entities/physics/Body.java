package com.avogine.shmupemup.scene.entities.physics;

import org.joml.Vector3f;
import org.joml.primitives.AABBf;

/**
 *
 */
public class Body {

	private final AABBf collider;
	private final AABBf transformedCollider;
	
	private final Vector3f velocity;
	private final Vector3f acceleration;
	private float mass;
	
	private int categoryBitMask;
	private int collisionBitMask;
	
	private Object bodyData;
	
	/**
	 * @param collider
	 * @param velocity
	 * @param acceleration
	 * @param mass
	 * @param collisionBitMask
	 */
	public Body(AABBf collider, Vector3f velocity, Vector3f acceleration, float mass, int categoryBitMask, int collisionBitMask) {
		this.collider = collider;
		transformedCollider = new AABBf();
		this.velocity = velocity;
		this.acceleration = acceleration;
		this.mass = mass;
		this.categoryBitMask = categoryBitMask;
		this.collisionBitMask = collisionBitMask;
	}
	
	/**
	 * @param constraints
	 */
	public Body(BodyConstraints constraints) {
		this(constraints.collider(), constraints.velocity(), constraints.acceleration(), constraints.mass(), constraints.categoryBitMask(), constraints.collisionBitMask());
	}
	
	/**
	 * @return the collider
	 */
	public AABBf getCollider() {
		return collider;
	}
	
	/**
	 * @return the transformedCollider
	 */
	public AABBf getTransformedCollider() {
		return transformedCollider;
	}
	
	/**
	 * @return the velocity
	 */
	public Vector3f getVelocity() {
		return velocity;
	}
	
	/**
	 * @return the acceleration
	 */
	public Vector3f getAcceleration() {
		return acceleration;
	}
	
	/**
	 * @return the mass
	 */
	public float getMass() {
		return mass;
	}
	
	/**
	 * @param mass the mass to set
	 */
	public void setMass(float mass) {
		this.mass = mass;
	}
	
	/**
	 * @return the categoryBitMask
	 */
	public int getCategoryBitMask() {
		return categoryBitMask;
	}
	
	/**
	 * @param categoryBitMask the categoryBitMask to set
	 */
	public void setCategoryBitMask(int categoryBitMask) {
		this.categoryBitMask = categoryBitMask;
	}
	
	/**
	 * @return the collisionBitMask
	 */
	public int getCollisionBitMask() {
		return collisionBitMask;
	}
	
	/**
	 * @param collisionBitMask the collisionBitMask to set
	 */
	public void setCollisionBitMask(int collisionBitMask) {
		this.collisionBitMask = collisionBitMask;
	}
	
	/**
	 * @return the bodyData
	 */
	public Object getBodyData() {
		return bodyData;
	}
	
	/**
	 * @param bodyData the bodyData to set
	 */
	public void setBodyData(Object bodyData) {
		this.bodyData = bodyData;
	}
	
}
