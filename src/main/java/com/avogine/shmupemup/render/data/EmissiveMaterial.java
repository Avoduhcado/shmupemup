package com.avogine.shmupemup.render.data;

import org.joml.Vector4f;

import com.avogine.render.opengl.model.Material;

/**
 *
 */
public class EmissiveMaterial extends Material {

	/**
	 * @param emission 
	 */
	public EmissiveMaterial(Vector4f emission) {
		super(emission);
	}

}
