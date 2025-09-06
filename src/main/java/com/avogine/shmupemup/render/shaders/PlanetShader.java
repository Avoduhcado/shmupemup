package com.avogine.shmupemup.render.shaders;

import static com.avogine.util.resource.ResourceConstants.SHADERS;

import com.avogine.render.opengl.shader.ShaderProgram;
import com.avogine.render.opengl.shader.uniform.*;

/**
 *
 */
public class PlanetShader extends ShaderProgram {

	public final UniformMat4 projection = new UniformMat4();
	public final UniformMat4 view = new UniformMat4();
	public final UniformMat4 model = new UniformMat4();
	
	public final UniformMat3 normalMatrix = new UniformMat3();
	
	public final UniformSampler Texture0 = new UniformSampler();
	
	public PlanetShader() {
		super(SHADERS.with("planetVertex.glsl"), SHADERS.with("planetFragment.glsl"));
		storeAllUniformLocations(projection, view, model, normalMatrix, Texture0);
		loadTexUnit();
	}
	
	private void loadTexUnit() {
		bind();
		Texture0.loadTexUnit(0);
		unbind();
	}

}
