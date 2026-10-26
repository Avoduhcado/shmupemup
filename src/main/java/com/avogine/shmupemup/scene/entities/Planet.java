package com.avogine.shmupemup.scene.entities;

import org.joml.*;

import com.avogine.render.opengl.model.StaticModel;

/**
 * A special {@link Entity} type that does not cache it's model as there should only be one per entity.
 */
public final class Planet extends Entity {

	private StaticModel model;
	
	/**
	 * @param position 
	 * @param orientation 
	 * @param scale 
	 * @param model
	 */
	public Planet(Vector3f position, AxisAngle4f orientation, float scale, StaticModel model) {
		super(ENTITY_ID_SUPPLIER.get());
		this.transform = new Transform(position, orientation, scale);
		this.model = model;
		
		updateModelMatrix();
	}
	
	/**
	 * 
	 */
	public void cleanup() {
		model.cleanup();
	}
	
	/**
	 * @return the model
	 */
	public StaticModel getModel() {
		return model;
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
}
