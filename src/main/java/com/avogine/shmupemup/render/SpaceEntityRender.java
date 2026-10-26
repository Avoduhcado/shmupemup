package com.avogine.shmupemup.render;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.GL_DEPTH_COMPONENT24;

import java.nio.ByteBuffer;
import java.util.*;

import org.joml.*;

import com.avogine.entity.AnimationData;
import com.avogine.render.model.mesh.Instanceable;
import com.avogine.render.opengl.FBO;
import com.avogine.render.opengl.FBO.FramebufferTexture;
import com.avogine.render.opengl.model.mesh.StaticMesh;
import com.avogine.render.opengl.model.util.*;
import com.avogine.render.opengl.shader.*;
import com.avogine.render.opengl.texture.Texture;
import com.avogine.shmupemup.game.scene.particles.SpaceshipParticleEmitter;
import com.avogine.shmupemup.render.shaders.*;
import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.SpaceScene.EntityType;
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
	
	private BasicShader basicShader;
	
	private SceneDepthShader depthShader;
	private Texture depthTexture;
	private FBO depth;
	private StaticMesh depthCube;
	private final Matrix4f depthMatrix;
	
	private final Set<EntityType> simpleStaticTypes = EnumSet.of(EntityType.PLAYER, EntityType.ENEMY, EntityType.SCENERY);
	private final Set<EntityType> physicalTypes = EnumSet.of(EntityType.PLAYER, EntityType.ENEMY, EntityType.SCENERY, EntityType.NPC);
	
	/**
	 * Rendering is synchronous, so this list is not concurrently modifiable.
	 */
	private final List<SpaceEntity> modelEntityList;
	
	private final Matrix4f modelViewMatrix;
	private final Matrix4f normalMatrix;
	private final Vector3f viewMatrixPosition;
	private final Vector3f lightPosition;
	
	/**
	 * 
	 */
	public SpaceEntityRender() {
		modelEntityList = new ArrayList<>();
		
		modelViewMatrix = new Matrix4f();
		normalMatrix = new Matrix4f();
		viewMatrixPosition = new Vector3f();
		lightPosition = new Vector3f();
		
		depthMatrix = new Matrix4f();
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
		
		basicShader = new BasicShader();
		
		depthShader = new SceneDepthShader();
		setupDepthBuffer();
	}
	
	private void setupDepthBuffer() {
		depthTexture = Texture.gen2D(depthTex -> depthTex
				.minFilter().nearest()
				.magFilter().nearest()
				.wrapST().clampToEdge()
				.image2D(0, GL_DEPTH_COMPONENT24, 1280, 720, GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null));

		depth = FBO.framebuffer(FramebufferTexture.depth(depthTexture));
		
		depth.bind();
		glDrawBuffer(GL_NONE);
		glReadBuffer(GL_NONE);
		depth.unbind();
		
		depthCube = ParShapesLoader.loadPlane(15.0f);
		
		depthMatrix.translate(0, -5, -5);
	}
	
	private void fillEntityList(String modelID, Set<EntityType> types, SpaceScene scene) {
		modelEntityList.clear();
		types.forEach(type -> scene.getEntities(type).forEach(entity -> {
			if (Objects.equals(entity.getModelId(), modelID)) {
				modelEntityList.add(entity);
			}
		}));
	}
	
	/**
	 * @param scene
	 */
	public void render(SpaceScene scene) {
//		renderToDepth(scene);
		
		shader.bind();
		
		shader.projection.loadMatrix(scene.getProjectionMatrix());
		shader.view.loadMatrix(scene.getViewMatrix());
		
		shader.lightColor.loadVec3(0.5f, 0.5f, 0.5f);
		scene.getViewMatrix().transformPosition(1000, 1200, -3500, lightPosition);
		shader.lightPosition.loadVec3(lightPosition);
		
		scene.getStaticModels().forEach(model -> {
			fillEntityList(model.getId(), simpleStaticTypes, scene);
			
			model.getMaterialMap().forEach((material, meshList) -> {
				scene.getTextureCache().getTextureOrDefault(material.getDiffuseTexturePath()).activate(0);
				scene.getTextureCache().getTextureOrDefault(material.getSpecularMapPath()).activate(1);
				shader.specularFactor.loadFloat(material.getSpecularFactor());

				meshList.forEach(mesh -> mesh.render(modelEntityList, entity -> shader.model.loadMatrix(entity.getModelMatrix())));
			});
		});
		
		shader.unbind();
		
//		basicShader.bind();
//
//		basicShader.projection.loadMatrix(scene.getProjectionMatrix());
//		basicShader.view.loadMatrix(scene.getViewMatrix());
//		
//		depthTexture.activate(0);
//		basicShader.model.loadMatrix(depthMatrix);
//		depthCube.render();
//		depthTexture.unbind();
//		
//		basicShader.unbind();
	}
	
	/**
	 * @param scene
	 */
	public void renderAnimations(SpaceScene scene) {
		animShader.bind();
		
		animShader.projection.loadMatrix(scene.getProjectionMatrix());
		animShader.view.loadMatrix(scene.getViewMatrix());
		
		animShader.lightColor.loadVec3(0.5f, 0.5f, 0.5f);
		scene.getViewMatrix().transformPosition(1000, 1200, -3500, lightPosition);
		animShader.lightPosition.loadVec3(lightPosition);
		
		scene.getAnimatedModels()
		.forEach(model -> {
			fillEntityList(model.getId(), EnumSet.of(EntityType.NPC), scene);
			
			model.getMaterialMap().forEach((material, meshList) -> {
				scene.getTextureCache().getTextureOrDefault(material.getDiffuseTexturePath()).activate(0);
				scene.getTextureCache().getTextureOrDefault(material.getSpecularMapPath()).activate(1);
				animShader.specularFactor.loadFloat(material.getSpecularFactor());
				
				meshList.forEach(mesh -> 
					mesh.render(modelEntityList, entity -> {
						AnimationData animationData = entity.getAnimationData();
						if (animationData == null) {
							animShader.boneMatrices.loadMatrixArray(DEFAULT_BONES_MATRICES);
						} else {
							animShader.boneMatrices.loadMatrixArray(animationData.getCurrentFrame().boneMatrices());
						}
						
						animShader.model.loadMatrix(entity.getModelMatrix());
					})
				);
			});
		});
		
		animShader.unbind();
	}
	
	private void renderToDepth(SpaceScene scene) {
		depth.bind();
		glClear(GL_DEPTH_BUFFER_BIT);
		glEnable(GL_DEPTH_TEST);
		
		depthShader.bind();
		
		depthShader.projectionMatrix.loadMatrix(scene.getProjectionMatrix());
		depthShader.viewMatrix.loadMatrix(scene.getViewMatrix());

		scene.getStaticModels().forEach(model -> {
			fillEntityList(model.getId(), physicalTypes, scene);

			model.getMaterialMap()
			.forEach((_, meshList) -> meshList
					.forEach(mesh -> mesh.render(modelEntityList, entity -> depthShader.modelMatrix.loadMatrix(entity.getModelMatrix()))));
		});
		
		depthShader.unbind();
		depth.unbind();
	}
	
	/**
	 * @param scene
	 */
	public void renderInstances(SpaceScene scene) {
		instanceShader.bind();

		instanceShader.projection.loadMatrix(scene.getProjectionMatrix());
		instanceShader.view.loadMatrix(scene.getViewMatrix());

		instanceShader.lightColor.loadVec3(0.5f, 0.5f, 0.5f);
		scene.getViewMatrix().transformPosition(1000, 1200, -3500, lightPosition);
		instanceShader.lightPosition.loadVec3(lightPosition);

		scene.getStaticInstancedModels()
		.forEach(model -> model.getMaterialMap()
				.forEach((material, meshList) -> meshList
						.forEach(mesh -> {
							if (mesh instanceof Instanceable) {
								scene.getTextureCache().getTextureOrDefault(material.getDiffuseTexturePath()).activate(0);
								scene.getTextureCache().getTextureOrDefault(material.getSpecularMapPath()).activate(1);
								instanceShader.specularFactor.loadFloat(material.getSpecularFactor());

								mesh.render();
							}
						})));

		instanceShader.unbind();
	}
	
	/**
	 * @param scene
	 */
	public void renderLasers(SpaceScene scene) {
		neonShader.bind();
		
		neonShader.projectionMatrix.loadMatrix(scene.getProjectionMatrix());
		neonShader.viewMatrix.loadMatrix(scene.getViewMatrix());

		scene.getEmissiveModels().forEach(model -> {
			fillEntityList(model.getId(), EnumSet.of(EntityType.PROJECTILE), scene);

			model.getMaterialMap().forEach((_, meshList) -> {
				// TODO Re-implement emissive materials
//				if (material instanceof EmissiveMaterial emissiveMaterial) {
					neonShader.objectColor.loadVec4(1.0f, 0.0f, 0.0f, 1.0f);

					meshList.forEach(mesh -> mesh.render(modelEntityList, laser -> neonShader.modelMatrix.loadMatrix(laser.getModelMatrix())));
//				}
			});
		});
		
		neonShader.unbind();
	}
	
	/**
	 * @param scene
	 */
	public void renderParticles(SpaceScene scene) {
		if (scene.getSpaceshipParticleEmitter().getParticles().isEmpty()) {
			return;
		}
		SpaceshipParticleEmitter particleEmitter = scene.getSpaceshipParticleEmitter();
		
		glEnable(GL_BLEND);
		particleShader.bind();
		
		particleShader.projection.loadMatrix(scene.getProjectionMatrix());
		particleShader.view.loadMatrix(scene.getViewMatrix());
		
		particleShader.cameraRightWorldspace.loadVec3(scene.getViewMatrix().m00(), scene.getViewMatrix().m10(), scene.getViewMatrix().m20());
		particleShader.cameraUpWorldspace.loadVec3(scene.getViewMatrix().m01(), scene.getViewMatrix().m11(), scene.getViewMatrix().m21());
		
		particleEmitter.getParticleTexture().activate(0);
		particleEmitter.getParticleMesh().render();
		
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
		if (depth != null) {
			depth.cleanup();
		}
	}
	
}
