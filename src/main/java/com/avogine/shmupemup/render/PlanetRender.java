package com.avogine.shmupemup.render;

import org.joml.*;

import com.avogine.render.opengl.*;
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
	private final Matrix3f normalMatrix;
	
	/**
	 * 
	 */
	public PlanetRender() {
		projectionView = new Matrix4f();
		modelMatrix = new Matrix4f();
		normalMatrix = new Matrix3f();
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
//			scene.getView().mul(modelMat, new Matrix4f()).get3x3(normalMatrix);
			// These aren't actually being used but this should be a proper normalmatrix calculation
			modelMatrix.get3x3(normalMatrix);
			normalMatrix.invert().transpose();
			planetShader.normalMatrix.loadMatrix(normalMatrix);
			
			planet.getModel().getBlinnPhongMaterials().forEach(material -> {
				Texture diffuseTexture = scene.getTextureCache().getCubemap(material.getDiffuseTexturePath(), "png");
				diffuseTexture.activate(0);
				
				material.getStaticMeshes().forEach(Mesh::render);
			});
//			planet.getModel().getMaterials().forEach(material -> {
//				glActiveTexture(GL_TEXTURE0);
//				material.getDiffuseTexture().bind();
//				
//				planet.getModel().getMeshes().stream()
//				.filter(mesh -> mesh.getMaterialIndex() == planet.getModel().getMaterials().indexOf(material))
//				.forEach(mesh -> {
//					glBindVertexArray(mesh.getVaoId());
//					glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
//				});
//			});
		});
		
		VAO.unbind();
		
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
