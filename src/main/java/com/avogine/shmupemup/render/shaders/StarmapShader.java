package com.avogine.shmupemup.render.shaders;

import static com.avogine.util.resource.ResourceConstants.SHADERS;

import com.avogine.render.shader.ShaderProgram;
import com.avogine.render.shader.uniform.*;

/**
 *
 */
public class StarmapShader extends ShaderProgram {

	public final UniformMat4 projectionView = new UniformMat4();
	public final UniformSampler starmap = new UniformSampler();
	
	/**
	 * 
	 */
	public StarmapShader() {
		super(SHADERS.with("starmapVertex.glsl"), SHADERS.with("starmapFragment.glsl"));
		storeAllUniformLocations(projectionView, starmap);
		loadTexUnit();
	}
	
	private void loadTexUnit() {
		bind();
		starmap.loadTexUnit(0);
		unbind();
	}
	
}
