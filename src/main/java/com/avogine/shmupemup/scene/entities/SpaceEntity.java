package com.avogine.shmupemup.scene.entities;

import org.joml.*;
import org.joml.primitives.AABBf;

import com.avogine.entity.AnimationData;
import com.avogine.shmupemup.scene.entities.physics.Body;

/**
 *
 */
public final class SpaceEntity extends Entity {

	private final String modelId;
	
	private final Body body;
	private final AnimationData animationData;
	
	/**
	 * @param position 
	 * @param orientation 
	 * @param scale 
	 * @param modelId 
	 * @param body 
	 * @param animationData 
	 */
	public SpaceEntity(Vector3f position, AxisAngle4f orientation, float scale, String modelId, Body body, AnimationData animationData) {
		super(ENTITY_ID_SUPPLIER.get());
		this.body = body;
		this.transform = new Transform(position, orientation, scale);
		this.modelId = modelId;
		this.animationData = animationData;
		
		updateModelMatrix();
	}
	
	/**
	 * @param position
	 * @param orientation
	 * @param scale
	 * @param modelId
	 * @param body
	 */
	public SpaceEntity(Vector3f position, AxisAngle4f orientation, float scale, String modelId, Body body) {
		this(position, orientation, scale, modelId, body, null);
	}
	
	/**
	 * 
	 */
	@Override
	public void updateModelMatrix() {
		super.updateModelMatrix();
		body.update(modelMatrix);
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	/**
	 * @return the modelId
	 */
	public String getModelId() {
		return modelId;
	}
	
	/**
	 * @return the body
	 */
	public Body getBody() {
		return body;
	}
	
	/**
	 * @return the body's AABB transformed by the entity's transform model matrix.
	 */
	public AABBf getWorldCollider() {
		return body.getTransformedCollider();
	}
	
	/**
	 * @return the animationData
	 */
	public AnimationData getAnimationData() {
		return animationData;
	}
	
}
