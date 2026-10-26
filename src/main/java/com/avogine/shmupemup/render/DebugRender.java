package com.avogine.shmupemup.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

import java.nio.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.joml.*;
import org.joml.primitives.AABBf;
import org.lwjgl.system.*;

import com.avogine.render.model.Model;
import com.avogine.render.opengl.*;
import com.avogine.render.opengl.model.mesh.data.Vertex.Attrib;
import com.avogine.shmupemup.render.shaders.WireframeShader;
import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.SpaceScene.EntityType;
import com.avogine.shmupemup.scene.entities.SpaceEntity;

/**
 *
 */
public class DebugRender {

	private static final int MAX_INSTANCES_PER_BATCH = 10000;
	private static final int INSTANCED_VERTEX_COUNT = 6;
	
	private WireframeShader shader;
	
	private final Set<EntityType> debugTypes = EnumSet.allOf(EntityType.class);
	
	private final List<SpaceEntity> modelEntityList;
	
	private final Matrix4f projectionViewMatrix;
	
	private VAO vao;
	private VBO vbo;
	private VBO instanceVbo;
	private VBO ebo;
	private int aabbVertexCount = 24;
	
	private final Vector3f aabbCenter;
	private final Vector3f aabbExtent;
	
	/**
	 * 
	 */
	public DebugRender() {
		modelEntityList = new ArrayList<>();
		
		projectionViewMatrix = new Matrix4f();
		
		aabbCenter = new Vector3f();
		aabbExtent = new Vector3f();
	}
	
	/**
	 * TODO Support instance chunks
	 */
	public void init() {
		shader = new WireframeShader();
		
		AABBf unitCube = new AABBf(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f);
		float[] vertices = new float[] {
				// Front face
				unitCube.minX, unitCube.minY, unitCube.maxZ, // 0
				unitCube.maxX, unitCube.minY, unitCube.maxZ, // 1
				unitCube.maxX, unitCube.maxY, unitCube.maxZ, // 2
				unitCube.minX, unitCube.maxY, unitCube.maxZ, // 3
				// Back face
				unitCube.minX, unitCube.minY, unitCube.minZ, // 4
				unitCube.maxX, unitCube.minY, unitCube.minZ, // 5
				unitCube.maxX, unitCube.maxY, unitCube.minZ, // 6
				unitCube.minX, unitCube.maxY, unitCube.minZ  // 7
		};
		FloatBuffer vertexData = MemoryUtil.memAllocFloat(vertices.length).put(0, vertices);
		
		int[] indices = new int[] {
				0, 1, 1, 2, 2, 3, 3, 0,
				4, 5, 5, 6, 6, 7, 7, 4,
				0, 4, 1, 5, 2, 6, 3, 7
		};
		IntBuffer indexData = MemoryUtil.memAllocInt(indices.length).put(0, indices);
		
		int vertexOffset = Float.BYTES * 3;
		int vertexStride = 2 * vertexOffset;
		
		try {
			vao = new VAO();
			
			vbo = VBO.arrayBuffer(vertexData);
			new Attrib(0, Attrib.POINTER_3F).enable();
			
			instanceVbo = VBO.arrayBuffer((long) MAX_INSTANCES_PER_BATCH * INSTANCED_VERTEX_COUNT * Float.BYTES, GL_DYNAMIC_DRAW);
			new Attrib(1, new Attrib.Pointer(3, GL_FLOAT, false, vertexStride, 0L), 1).enable();
			new Attrib(2, new Attrib.Pointer(3, GL_FLOAT, false, vertexStride, vertexOffset), 1).enable();
			
			ebo = VBO.elementArrayBuffer(indexData);
		} finally {
			MemoryUtil.memFree(vertexData);
			MemoryUtil.memFree(indexData);
		}
	}
	
	/**
	 * 
	 */
	public void cleanup() {
		if (shader != null) {
			shader.cleanup();
		}
		vbo.cleanup();
		instanceVbo.cleanup();
		ebo.cleanup();
		vao.cleanup();
	}
	
	/**
	 * @param scene
	 */
	public void renderAABB(SpaceScene scene) {
		shader.bind();
		
		shader.projectionViewMatrix.loadMatrix(scene.getProjectionMatrix().mul(scene.getViewMatrix(), projectionViewMatrix));
		shader.wireframeColor.loadVec4(1.0f, 1.0f, 0.0f, 1.0f);
		
		renderModels(scene.getStaticModels(), scene);
		renderModels(scene.getEmissiveModels(), scene);
		renderModels(scene.getAnimatedModels(), scene);
		renderModels(scene.getStaticInstancedModels(), scene);
		
		shader.unbind();
	}
	
	private <T extends Model<?, ?>> void renderModels(List<T> models, SpaceScene scene) {
		AtomicInteger instanceIndex = new AtomicInteger();
		vao.bind();
		
		instanceVbo.bind();
		models.forEach(model -> {
			fillEntityList(model.getId(), debugTypes, scene);

			model.getMaterialMap().forEach((_, meshList) -> meshList
					.forEach(_ -> modelEntityList
							.forEach(entity -> {
								AABBf aabb = entity.getWorldCollider();
								aabb.center(aabbCenter);
								aabb.extent(aabbExtent);
								
								try (MemoryStack stack = MemoryStack.stackPush()) {
									FloatBuffer instancedBuffer = stack.mallocFloat(INSTANCED_VERTEX_COUNT);
									instancedBuffer
									.put(aabbCenter.x).put(aabbCenter.y).put(aabbCenter.z)
									.put(aabbExtent.x).put(aabbExtent.y).put(aabbExtent.z)
									.flip();

									instanceVbo.bufferSubData((long) instanceIndex.getAndIncrement() * INSTANCED_VERTEX_COUNT * Float.BYTES, instancedBuffer);
								}
							}))
					);
		});
		instanceVbo.unbind();
		
		glDrawElementsInstanced(GL_LINES, aabbVertexCount, GL_UNSIGNED_INT, 0, instanceIndex.get());
	}
	
	private void fillEntityList(String modelID, Set<EntityType> types, SpaceScene scene) {
		modelEntityList.clear();
		types.forEach(type -> scene.getEntities(type).forEach(entity -> {
			if (Objects.equals(entity.getModelId(), modelID)) {
				modelEntityList.add(entity);
			}
		}));
	}
	
}
