package com.avogine.shmupemup.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.*;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import com.avogine.shmupemup.render.shaders.WireframeShader;
import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.entities.SpaceEntity;

/**
 *
 */
public class DebugRender {

	private WireframeShader shader;
	
	private final Matrix4f projectionViewMatrix;
	private int aabbVao;
	private int aabbVbo;
	private int aabbEbo;
	private int aabbVertexCount;
	
	/**
	 * 
	 */
	public DebugRender() {
		projectionViewMatrix = new Matrix4f();
	}
	
	/**
	 * 
	 */
	public void init() {
		shader = new WireframeShader();
		
		var aabbPositions = new float[] {
				-1f, -1f,  1f, //0
				 1f, -1f,  1f, //1
				-1f,  1f,  1f, //2
				 1f,  1f,  1f, //3
				-1f, -1f, -1f, //4
				 1f, -1f, -1f, //5
				-1f,  1f, -1f, //6
				 1f,  1f, -1f  //7
		};
		
		var indices = new int[] {
				//Top
				2, 6, 7,
				2, 3, 7,

				//Bottom
				0, 4, 5,
				0, 1, 5,

				//Left
				0, 2, 6,
				0, 4, 6,

				//Right
				1, 3, 7,
				1, 5, 7,

				//Front
				0, 2, 3,
				0, 1, 3,

				//Back
				4, 6, 7,
				4, 5, 7
		};
		aabbVertexCount = indices.length;
		
		// TODO Replace with VAO type
		aabbVao = glGenVertexArrays();
		glBindVertexArray(aabbVao);
		
		aabbVbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, aabbVbo);
		glBufferData(GL_ARRAY_BUFFER, aabbPositions, GL_DYNAMIC_DRAW);
		
		aabbEbo = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, aabbEbo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		
		glBindVertexArray(0);
	}
	
	/**
	 * 
	 */
	public void cleanup() {
		if (shader != null) {
			shader.cleanup();
		}
		glDeleteVertexArrays(aabbVao);
		glDeleteBuffers(aabbVbo);
		glDeleteBuffers(aabbEbo);
	}
	
	/**
	 * @param scene
	 */
	public void renderAABB(SpaceScene scene) {
		shader.bind();
		
		shader.projectionViewMatrix.loadMatrix(scene.getProjectionMatrix().mul(scene.getViewMatrix(), projectionViewMatrix));

		glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		
		glBindVertexArray(aabbVao);
		glBindBuffer(GL_ARRAY_BUFFER, aabbVbo);
		
		scene.getStaticModels().stream()
		.forEach(model -> {
			List<SpaceEntity> entities = scene.getSpaceEntities()
					.filter(entity -> Objects.equals(entity.getModelId(), model.getId()))
					.toList();
			
			model.getMaterials().forEach(material -> {
				shader.wireframeColor.loadVec4(1.0f, 1.0f, 0.0f, 1.0f);
				
				material.getMeshes().forEach(mesh -> entities.forEach(entity -> {
					var aabb = entity.getWorldCollider();
					try (MemoryStack stack = MemoryStack.stackPush()) {
						FloatBuffer positionsBuffer = stack.mallocFloat(8 * 3);
						positionsBuffer
						.put(aabb.minX).put(aabb.minY).put(aabb.maxZ)
						.put(aabb.maxX).put(aabb.minY).put(aabb.maxZ)
						.put(aabb.minX).put(aabb.maxY).put(aabb.maxZ)
						.put(aabb.maxX).put(aabb.maxY).put(aabb.maxZ)
						.put(aabb.minX).put(aabb.minY).put(aabb.minZ)
						.put(aabb.maxX).put(aabb.minY).put(aabb.minZ)
						.put(aabb.minX).put(aabb.maxY).put(aabb.minZ)
						.put(aabb.maxX).put(aabb.maxY).put(aabb.minZ);
						positionsBuffer.flip();
						glBufferSubData(GL_ARRAY_BUFFER, 0, positionsBuffer);
					}
					
					glDrawElements(GL_TRIANGLES, aabbVertexCount, GL_UNSIGNED_INT, 0);
				}));
			});
			
//			model.getMaterials().forEach(material -> {
//				shader.wireframeColor.loadVec4(1.0f, 1.0f, 0.0f, 1.0f);
//				
//				model.getMeshes().stream()
//				.filter(mesh -> mesh.getMaterialIndex() == model.getMaterials().indexOf(material))
//				.forEach(mesh -> 
//					entities.forEach(entity -> {
//						var aabb = entity.getWorldCollider();
//						try (MemoryStack stack = MemoryStack.stackPush()) {
//							FloatBuffer positionsBuffer = stack.mallocFloat(8 * 3);
//							positionsBuffer
//							.put(aabb.minX).put(aabb.minY).put(aabb.maxZ)
//							.put(aabb.maxX).put(aabb.minY).put(aabb.maxZ)
//							.put(aabb.minX).put(aabb.maxY).put(aabb.maxZ)
//							.put(aabb.maxX).put(aabb.maxY).put(aabb.maxZ)
//							.put(aabb.minX).put(aabb.minY).put(aabb.minZ)
//							.put(aabb.maxX).put(aabb.minY).put(aabb.minZ)
//							.put(aabb.minX).put(aabb.maxY).put(aabb.minZ)
//							.put(aabb.maxX).put(aabb.maxY).put(aabb.minZ);
//							positionsBuffer.flip();
//							glBufferSubData(GL_ARRAY_BUFFER, 0, positionsBuffer);
//						}
//						
//						glDrawElements(GL_TRIANGLES, aabbVertexCount, GL_UNSIGNED_INT, 0);
//					})
//				);
//			});
		});

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glEnable(GL_CULL_FACE);
		
		shader.unbind();
	}
	
}
