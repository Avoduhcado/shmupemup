package com.avogine.shmupemup.render.shaders;

import static com.avogine.util.resource.ResourceConstants.SHADERS;

import com.avogine.render.shader.ShaderProgram;
import com.avogine.render.shader.uniform.*;

/**
 *
 */
public class NeonShader extends ShaderProgram {

	public final UniformMat4 projectionMatrix = new UniformMat4();
	public final UniformMat4 viewMatrix = new UniformMat4();
	public final UniformMat4 modelMatrix = new UniformMat4();
	
	public final UniformVec4 objectColor = new UniformVec4();
	
	/**
	 * 
	 */
	public NeonShader() {
		super(SHADERS.with("neonVertex.glsl"), SHADERS.with("neonFragment.glsl"));
		storeAllUniformLocations(projectionMatrix, viewMatrix, modelMatrix, objectColor);
	}
	
}

