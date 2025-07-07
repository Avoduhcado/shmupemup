package com.avogine.shmupemup.render.shaders;

import static com.avogine.util.resource.ResourceConstants.SHADERS;

import org.lwjgl.opengl.*;

import com.avogine.render.shader.ShaderProgram;
import com.avogine.render.shader.uniform.UniformMat4;

/**
 *
 */
public class SpaceShader extends ShaderProgram {
	
	public final UniformMat4 projection = new UniformMat4();
	public final UniformMat4 view = new UniformMat4();
	
	/**
	 * 
	 */
	public SpaceShader() {
		super(
				new ShaderModuleData(SHADERS.with("spaceVertex.glsl"), GL20.GL_VERTEX_SHADER),
				new ShaderModuleData(SHADERS.with("spaceGeometry.glsl"), GL32.GL_GEOMETRY_SHADER),
				new ShaderModuleData(SHADERS.with("spaceFragment.glsl"), GL20.GL_FRAGMENT_SHADER));
		storeAllUniformLocations(projection, view);
	}
	
	@Override
	public void bind() {
		super.bind();
		GL11.glEnable(GL32.GL_PROGRAM_POINT_SIZE);
	}
	
	@Override
	public void unbind() {
		super.unbind();
		GL11.glDisable(GL32.GL_PROGRAM_POINT_SIZE);
	}

}
