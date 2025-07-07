package com.avogine.shmupemup.render.shaders;

import static com.avogine.util.resource.ResourceConstants.SHADERS;

import com.avogine.render.shader.ShaderProgram;
import com.avogine.render.shader.uniform.*;

/**
 *
 */
public class WireframeShader extends ShaderProgram {

	public final UniformMat4 projectionViewMatrix = new UniformMat4();
	
	public final UniformVec4 wireframeColor = new UniformVec4();
	
	/**
	 * 
	 */
	public WireframeShader() {
		super(SHADERS.with("wireframeVertex.glsl"), SHADERS.with("wireframeFragment.glsl"));
		storeAllUniformLocations(projectionViewMatrix, wireframeColor);
	}
	
}
