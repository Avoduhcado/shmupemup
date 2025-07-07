package com.avogine.shmupemup.render.shaders;

import com.avogine.render.shader.ShaderProgram;
import com.avogine.render.shader.uniform.UniformMat4;
import com.avogine.util.resource.ResourceConstants;

public class SceneDepthShader extends ShaderProgram {
	
	public final UniformMat4 projectionMatrix = new UniformMat4();
	public final UniformMat4 viewMatrix = new UniformMat4();
	public final UniformMat4 modelMatrix = new UniformMat4();
	
	/**
	 * 
	 */
	public SceneDepthShader() {
		super(ResourceConstants.SHADERS.with("sceneDepthVertex.glsl"), ResourceConstants.SHADERS.with("sceneDepthFragment.glsl"));
		storeAllUniformLocations(projectionMatrix, viewMatrix, modelMatrix);
	}
}