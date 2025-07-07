package com.avogine.shmupemup.scene.entities;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.joml.*;

/**
 *
 */
public abstract sealed class Entity permits Planet, SpaceEntity {

	private static final AtomicInteger ENTITY_ID_COUNTER = new AtomicInteger();
	
	protected static final Supplier<Integer> ENTITY_ID_SUPPLIER = ENTITY_ID_COUNTER::getAndIncrement;
	
	protected final Integer id;
	
	protected Transform transform;
	
	protected final Matrix4f modelMatrix;
	
	protected Entity(int id) {
		this.id = id;
		modelMatrix = new Matrix4f();
	}
	/**
	 * 
	 */
	public void updateModelMatrix() {
		modelMatrix.translationRotateScale(transform.position(), transform.orientation(), transform.scale());
	}
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @return the transform
	 */
	public Transform getTransform() {
		return transform;
	}
	
	/**
	 * @return the modelMatrix
	 */
	public Matrix4f getModelMatrix() {
		return modelMatrix;
	}
	
	/**
	 * @return the position
	 */
	public Vector3f getPosition() {
		return transform.position();
	}
	
	/**
	 * @return the orientation
	 */
	public Quaternionf getOrientation() {
		return transform.orientation();
	}
	
	/**
	 * @return the scale
	 */
	public Vector3f getScale() {
		return transform.scale();
	}
	
	/**
	 * @return the x parameter of scale
	 */
	public float getScale1f() {
		return transform.scale().x();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Entity e)) {
			return false;
		} else {
			return this.id == e.id;
		}
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + id;
		return hash;
	}
	
}
