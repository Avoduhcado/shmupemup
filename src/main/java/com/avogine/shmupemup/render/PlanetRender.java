package com.avogine.shmupemup.render;

import org.joml.Matrix4f;

import com.avogine.render.opengl.model.mesh.Mesh;
import com.avogine.render.opengl.texture.Texture;
import com.avogine.shmupemup.render.shaders.PlanetShader;
import com.avogine.shmupemup.scene.SpaceScene;

/**
 *
 */
public class PlanetRender {

	private PlanetShader planetShader;
	
	private final Matrix4f projectionView;
	private final Matrix4f modelMatrix;
	
	/**
	 * 
	 */
	public PlanetRender() {
		projectionView = new Matrix4f();
		modelMatrix = new Matrix4f();
	}
	
	/**
	 * 
	 */
	public void init() {
		planetShader = new PlanetShader();
	}
	
	/**
	 * @param scene
	 */
	public void render(SpaceScene scene) {
		planetShader.bind();
		
		planetShader.projection.loadMatrix(scene.getProjectionMatrix());
		planetShader.view.loadMatrix(scene.getViewMatrix());
		
		scene.getProjectionMatrix().mul(scene.getViewMatrix(), projectionView);
		
		scene.getPlanets().forEach(planet -> {
			modelMatrix.translationRotateScale(planet.getPosition(), planet.getOrientation(), planet.getScale());
			planetShader.model.loadMatrix(modelMatrix);
			
			planet.getModel().getMaterialMap().forEach((material, meshList) -> {
				Texture diffuseTexture = scene.getTextureCache().getCubemap(material.getDiffuseTexturePath(), "png");
				diffuseTexture.activate(0);
				
				meshList.forEach(Mesh::render);
			});
		});
		
		planetShader.unbind();
	}
	
	/**
	 * Free up allocated memory.
	 */
	public void cleanup() {
		if (planetShader != null) {
			planetShader.cleanup();
		}
	}
	
}
