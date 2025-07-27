package com.avogine.shmupemup.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.nio.*;
import java.util.*;

import org.joml.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import com.avogine.entity.AnimationData;
import com.avogine.render.opengl.*;
import com.avogine.render.opengl.model.mesh.Mesh;
import com.avogine.render.opengl.model.util.ModelLoader;
import com.avogine.render.shader.*;
import com.avogine.shmupemup.render.data.EmissiveMaterial;
import com.avogine.shmupemup.render.shaders.*;
import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.entities.SpaceEntity;

/**
 *
 */
public class SpaceEntityRender {
	
	private static final Matrix4f[] DEFAULT_BONES_MATRICES = new Matrix4f[ModelLoader.MAX_BONES];

	static {
		Matrix4f zeroMatrix = new Matrix4f().zero();
		Arrays.fill(DEFAULT_BONES_MATRICES, zeroMatrix);
	}

	private SimpleShader shader;
	private AnimatedShader animShader;
	private NeonShader neonShader;
	private SimpleInstanceShader instanceShader;
	private ParticleShader particleShader;
	
	private SceneDepthShader depthShader;
	private Texture depthTexture;
	private FBO depthFBO;
	
	private final Matrix4f modelViewMatrix;
	private final Matrix4f normalMatrix;
	private final Vector3f viewMatrixPosition;
	private final Vector3f lightPosition;
	
	/**
	 * 
	 */
	public SpaceEntityRender() {
		modelViewMatrix = new Matrix4f();
		normalMatrix = new Matrix4f();
		viewMatrixPosition = new Vector3f();
		lightPosition = new Vector3f();
	}
	
	/**
	 * 
	 */
	public void init() {
		shader = new SimpleShader();
		animShader = new AnimatedShader();
		neonShader = new NeonShader();
		instanceShader = new SimpleInstanceShader();
		particleShader = new ParticleShader();
		
		depthShader = new SceneDepthShader();
		setupDepthBuffer();
	}
	
	private void setupDepthBuffer() {
		try {
			depthFBO = FBO.gen().bind();

			depthTexture = Texture.gen().bind()
					.filterLinear()
					.texImage2D(GL14.GL_DEPTH_COMPONENT32, 1280, 720, GL11.GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
			Texture.unbind();

			depthFBO.attachTexture2D(GL30.GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture.id(), 0)
			.validate();

//			int rbo = GL30.glGenRenderbuffers();
//			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rbo);
//			GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT32F, 1280, 720);
//			GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, rbo);
		} finally {
			FBO.unbind();
		}
	}
	
	/**
	 * @param scene
	 */
	public void render(SpaceScene scene) {
//		renderToDepth(scene);
		
		shader.bind();
		
		shader.projectionMatrix.loadMatrix(scene.getProjectionMatrix());
		shader.viewMatrix.loadMatrix(scene.getViewMatrix());
		scene.getCamera().getInvertedView().getTranslation(viewMatrixPosition);
		shader.viewPosition.loadVec3(viewMatrixPosition);
		
		shader.lightColor.loadVec3(0.5f, 0.5f, 0.5f);
		scene.getViewMatrix().transformPosition(1000, 1200, -3500, lightPosition);
		shader.lightPosition.loadVec3(lightPosition);
		
		scene.getStaticModels().stream()
		.forEach(model -> {
			List<SpaceEntity> entities = scene.getSpaceEntities()
					.filter(entity -> Objects.equals(entity.getModelId(), model.getId()))
					.toList();
			
			model.getMaterials().forEach(material -> {
				if (material instanceof EmissiveMaterial) {
					return;
				}
				shader.objectColor.loadVec3(material.getDiffuseColor().x(), material.getDiffuseColor().y(), material.getDiffuseColor().z());
				if (material.getDiffuseTexturePath() != null) {
					shader.hasTexture.loadBoolean(true);
					glActiveTexture(GL_TEXTURE0);
					Texture diffuseTexture = scene.getTextureCache().getTexture(material.getDiffuseTexturePath());
					diffuseTexture.bind();
				} else {
					shader.hasTexture.loadBoolean(false);
				}
				
				material.getMeshes().forEach(mesh -> {
					mesh.bind();
					
					entities.forEach(entity -> {
						shader.modelMatrix.loadMatrix(entity.getModelMatrix());
						entity.getModelMatrix().mul(scene.getViewMatrix(), modelViewMatrix);
						modelViewMatrix.invert().transpose(normalMatrix);
						shader.normalMatrix.loadMatrix(normalMatrix);
						
						mesh.draw();
					});
				});
			});
		});

		VAO.unbind();
		
		shader.unbind();
	}
	
	/**
	 * @param scene
	 */
	public void renderAnimations(SpaceScene scene) {
		animShader.bind();
		
		animShader.projectionMatrix.loadMatrix(scene.getProjectionMatrix());
		animShader.viewMatrix.loadMatrix(scene.getViewMatrix());
		scene.getCamera().getInvertedView().getTranslation(viewMatrixPosition);
		animShader.viewPosition.loadVec3(viewMatrixPosition);
		
		animShader.lightColor.loadVec3(0.5f, 0.5f, 0.5f);
		scene.getViewMatrix().transformPosition(1000, 1200, -3500, lightPosition);
		animShader.lightPosition.loadVec3(lightPosition);
		
		scene.getAnimatedModels().stream()
		.forEach(model -> {
			List<SpaceEntity> entities = scene.getSpaceEntities()
					.filter(entity -> Objects.equals(entity.getModelId(), model.getId()))
					.toList();
			
			model.getMaterials().forEach(material -> {
				if (material instanceof EmissiveMaterial) {
					return;
				}
				animShader.objectColor.loadVec3(material.getDiffuseColor().x(), material.getDiffuseColor().y(), material.getDiffuseColor().z());
				if (material.getDiffuseTexturePath() != null) {
					animShader.hasTexture.loadBoolean(true);
					glActiveTexture(GL_TEXTURE0);
					Texture diffuseTexture = scene.getTextureCache().getTexture(material.getDiffuseTexturePath());
					diffuseTexture.bind();
				} else {
					animShader.hasTexture.loadBoolean(false);
				}
				
				material.getMeshes().forEach(mesh -> {
					mesh.bind();
					
					entities.forEach(entity -> {
						AnimationData animationData = entity.getAnimationData();
						if (animationData == null) {
							animShader.boneMatrices.loadMatrixArray(DEFAULT_BONES_MATRICES);
						} else {
							animShader.boneMatrices.loadMatrixArray(animationData.getCurrentFrame().boneMatrices());
						}
						
						animShader.modelMatrix.loadMatrix(entity.getModelMatrix());
						entity.getModelMatrix().mul(scene.getViewMatrix(), modelViewMatrix);
						modelViewMatrix.invert().transpose(normalMatrix);
						animShader.normalMatrix.loadMatrix(normalMatrix);
						
						mesh.draw();
					});
				});
			});
		});

		VAO.unbind();
		
		animShader.unbind();
	}
	
	private void renderToDepth(SpaceScene scene) {
		depthShader.bind();
		depthFBO.bind();

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		depthShader.projectionMatrix.loadMatrix(scene.getProjectionMatrix());
		depthShader.viewMatrix.loadMatrix(scene.getViewMatrix());

		scene.getStaticModels().forEach(model -> {
			List<SpaceEntity> entities = scene.getSpaceEntities()
					.filter(entity -> Objects.equals(entity.getModelId(), model.getId()))
					.toList();

			model.getMaterials().forEach(material -> material.getMeshes().forEach(mesh -> {
				mesh.bind();

				entities.forEach(entity -> {
					depthShader.modelMatrix.loadMatrix(entity.getModelMatrix());
					mesh.draw();
				});
			}));
		});

		VAO.unbind();
		FBO.unbind();
		depthShader.unbind();
	}
	
	/**
	 * @param scene
	 */
	public void renderInstances(SpaceScene scene) {
		instanceShader.bind();
		
		instanceShader.projectionMatrix.loadMatrix(scene.getProjectionMatrix());
		instanceShader.viewMatrix.loadMatrix(scene.getViewMatrix());
		scene.getCamera().getInvertedView().getTranslation(viewMatrixPosition);
		instanceShader.viewPosition.loadVec3(viewMatrixPosition);
		
		instanceShader.lightColor.loadVec3(0.5f, 0.5f, 0.5f);
		scene.getViewMatrix().transformPosition(1000, 1200, -3500, lightPosition);
		instanceShader.lightPosition.loadVec3(lightPosition);

		instanceShader.hasTexture.loadBoolean(false);
//		glActiveTexture(GL_TEXTURE0);
//		glBindTexture(GL_TEXTURE_2D, 0);
		instanceShader.objectColor.loadVec3(0.5f, 0.5f, 0.5f);
		
		scene.getStaticInstancedModels().forEach(model -> {
			List<SpaceEntity> instanceEntities = scene.getSpaceEntities()
					.filter(entity -> Objects.equals(entity.getModelId(), model.getId()))
					.toList();
			
			model.getMaterials().forEach(material -> material.getMeshes().forEach(Mesh::draw));
		});
		
		VAO.unbind();
		
		instanceShader.unbind();
	}
	
	/**
	 * @param scene
	 */
	public void renderLasers(SpaceScene scene) {
		neonShader.bind();
		
		neonShader.projectionMatrix.loadMatrix(scene.getProjectionMatrix());
		neonShader.viewMatrix.loadMatrix(scene.getViewMatrix());

		scene.getStaticModels().forEach(model -> {
			List<SpaceEntity> laserEntities = scene.getSpaceEntities()
					.filter(entity -> Objects.equals(entity.getModelId(), model.getId()))
					.toList();

			model.getMaterials().forEach(material -> {
				if (material instanceof EmissiveMaterial) {
					neonShader.objectColor.loadVec4(material.getDiffuseColor());

					material.getMeshes().forEach(mesh -> {
						mesh.bind();

						laserEntities.forEach(laser -> {
							neonShader.modelMatrix.loadMatrix(laser.getModelMatrix());

							mesh.draw();
						});
					});
				}
			});
		});
		
		VAO.unbind();
		
		neonShader.unbind();
	}
	
	/**
	 * @param scene
	 */
	public void renderParticles(SpaceScene scene) {
		if (scene.getSpaceshipParticleEmitter().getParticles().isEmpty()) {
			return;
		}
		var particleEmitter = scene.getSpaceshipParticleEmitter();
		
		glEnable(GL_BLEND);
		particleShader.bind();
		
		particleShader.projection.loadMatrix(scene.getProjectionMatrix());
		particleShader.view.loadMatrix(scene.getViewMatrix());
		
		particleShader.cameraRightWorldspace.loadVec3(scene.getViewMatrix().m00(), scene.getViewMatrix().m10(), scene.getViewMatrix().m20());
		particleShader.cameraUpWorldspace.loadVec3(scene.getViewMatrix().m01(), scene.getViewMatrix().m11(), scene.getViewMatrix().m21());
		
		glActiveTexture(GL_TEXTURE0);
		particleEmitter.getParticleTexture().bind();
		
		FloatBuffer positions = MemoryUtil.memAllocFloat(particleEmitter.getParticles().size() * 4);
		ByteBuffer colors = MemoryUtil.memAlloc(particleEmitter.getParticles().size() * 4);
		try {
			for (var p : particleEmitter.getParticles()) {
				positions.put(p.position().x).put(p.position().y).put(p.position().z).put(p.size());
				colors.put(p.r()).put(p.g()).put(p.b()).put(p.a());
			}
			positions.flip();
			colors.flip();
			
			particleEmitter.getParticleMesh().update(positions, colors);
		} finally {
			MemoryUtil.memFree(positions);
			MemoryUtil.memFree(colors);
		}
		
		particleEmitter.getParticleMesh().draw();
		VAO.unbind();
		
		particleShader.unbind();
		glDisable(GL_BLEND);
	}
	
	/**
	 * 
	 */
	public void cleanup() {
		if (shader != null) {
			shader.cleanup();
		}
		if (animShader != null) {
			animShader.cleanup();
		}
		if (neonShader != null) {
			neonShader.cleanup();
		}
		if (particleShader != null) {
			particleShader.cleanup();
		}
		if (instanceShader != null) {
			instanceShader.cleanup();
		}
		if (depthShader != null) {
			depthShader.cleanup();
		}
	}
	
}
