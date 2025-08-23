package com.avogine.shmupemup.render.data;

import java.util.ArrayList;

import org.joml.Vector4f;

import com.avogine.render.opengl.model.material.CustomMaterial;

/**
 *
 */
public final class EmissiveMaterial extends CustomMaterial {
	
	private Vector4f emissive;

	/**
	 * @param emissive 
	 */
	public EmissiveMaterial(Vector4f emissive) {
		super(new ArrayList<>());
		this.emissive = emissive;
	}
	
	/**
	 * @return the emissive
	 */
	public Vector4f getEmissive() {
		return emissive;
	}
	
	/**
	 * @param emissive the emissive to set
	 */
	public void setEmissive(Vector4f emissive) {
		this.emissive = emissive;
	}

}
