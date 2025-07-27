package com.avogine.shmupemup.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL30.*;

import java.lang.Math;
import java.nio.FloatBuffer;

import org.joml.*;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;

import com.avogine.game.scene.Scene;
import com.avogine.render.image.ImageData;
import com.avogine.render.image.data.PixelBuffer;
import com.avogine.render.opengl.*;
import com.avogine.render.opengl.Texture.*;
import com.avogine.render.opengl.VertexAttrib.Pointer;
import com.avogine.render.opengl.model.mesh.Mesh;
import com.avogine.render.opengl.model.util.ParShapesLoader;
import com.avogine.shmupemup.render.shaders.*;

/**
 * Renderer for drawing stars in space.
 * 
 * TODO Draw a default star map to a cubemap texture but dynamically render stars on every update loop so that they remain properly rotated relative to the camera.
 */
public class StarmapRender {

	private StarmapShader starmapShader;
	
	private Texture starCubemap;
	private Mesh starCube;
	
	private final Matrix4f projectionView;
	private final Matrix4f noTranslationView;
	
	/**
	 * Renderer for drawing stars in space.
	 */
	public StarmapRender() {
		projectionView = new Matrix4f();
		noTranslationView = new Matrix4f();
	}
	
	/**
	 * 
	 */
	public void init() {
		SpaceShader spaceShader = new SpaceShader();
		starmapShader = new StarmapShader();
		
		float starHalf = 100;
		float starDistance = starHalf * 2;
		int starsToRender = 1500;
		
		FloatBuffer vertexData = MemoryUtil.memAllocFloat(starsToRender * 3);
		for (int i = 0; i < starsToRender; i++) {
			// Load up random XYZ coordinate for each star
			vertexData.put((float) ((Math.random() * starDistance) - starHalf))
			.put((float) ((Math.random() * starDistance) - starHalf))
			.put((float) ((Math.random() * starDistance) - starHalf));
		}
		vertexData.flip();
		
		VAO starVAO = VAO.gen(() -> {
			var vbo = VBO.gen().bind().bufferData(vertexData, GL_STREAM_DRAW);
			VertexAttrib.array(0).pointer(Pointer.tightlyPackedUnnormalizedFloat(3)).enable();
			return vbo;
		});
		MemoryUtil.memFree(vertexData);

		int cubemapSize = 4096;
		var cubemapImage2D = new ImageData(cubemapSize, cubemapSize, GL_RGBA, new PixelBuffer(null));
		starCubemap = Texture.gen(GL13.GL_TEXTURE_CUBE_MAP).bind()
				.filterLinear()
				.wrap3DClampEdge()
				.tex(Image2DTarget.of(GL_TEXTURE_CUBE_MAP_POSITIVE_X, Image2D.from(cubemapImage2D)),
						Image2DTarget.of(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, Image2D.from(cubemapImage2D)),
						Image2DTarget.of(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, Image2D.from(cubemapImage2D)),
						Image2DTarget.of(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, Image2D.from(cubemapImage2D)),
						Image2DTarget.of(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, Image2D.from(cubemapImage2D)),
						Image2DTarget.of(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, Image2D.from(cubemapImage2D)));
		
		Matrix4f projection = new Matrix4f().setPerspective((float) Math.toRadians(90f), 1f, 0.1f, starDistance);
		Matrix4f view = new Matrix4f();
		
		int[] viewport = new int[4];
		glGetIntegerv(GL_VIEWPORT, viewport);
		glViewport(0, 0, cubemapSize, cubemapSize);
		
		float[] clearColor = new float[4];
		glGetFloatv(GL_COLOR_CLEAR_VALUE, clearColor);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		FBO starFBO = FBO.gen().bind();
		
		spaceShader.bind();
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_CULL_FACE);
		spaceShader.projection.loadMatrix(projection);
		for (int i = 0; i < 6; i++) {
			starFBO.attachTexture2D(GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, starCubemap.id(), 0);

			glClear(GL_COLOR_BUFFER_BIT);

			orientView(view, i);
			spaceShader.view.loadMatrix(view);

			glDrawArrays(GL_POINTS, 0, starsToRender);
		}

		FBO.unbind();
		VAO.unbind();
		// TODO Wrap this in some Render/Window call so it can poll properties
		glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
		spaceShader.unbind();

		glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
		glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
		
		spaceShader.cleanup();
		starVAO.cleanup();
		starFBO.cleanup();
		
		starCube = ParShapesLoader.loadCube(2);
	}
	
	/**
	 *  GL_TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515,
        GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516,
        GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517,
        GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518,
        GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519,
        GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A;
	 */
	private void orientView(Matrix4f view, int direction) {
		view.setLookAlong(new Vector3f(0, 0, 1), new Vector3f(0, 1, 0));
		switch (direction) {
			case 0 -> view.rotateY((float) Math.toRadians(90));
			case 1 -> view.rotateY((float) Math.toRadians(-90));
			case 2 -> view.rotateX((float) Math.toRadians(-90));
			case 3 -> view.rotateX((float) Math.toRadians(90));
			case 4 -> { /* Direction defaults to 4 (Positive Z) */}
			case 5 -> view.rotateY((float) Math.toRadians(180));
			default -> throw new IllegalArgumentException("Cannot orient view in direction: " + direction);
		}
	}
	
	/**
	 * @param scene 
	 */
	public void render(Scene scene) {
		starmapShader.bind();
		glCullFace(GL_FRONT);
		glDepthFunc(GL_LEQUAL);
		
		noTranslationView.set3x3(scene.getViewMatrix());
		projectionView.set(scene.getProjectionMatrix()).mul3x3(noTranslationView.m00(), noTranslationView.m01(), noTranslationView.m02(), 
				noTranslationView.m10(), noTranslationView.m11(), noTranslationView.m12(), 
				noTranslationView.m20(), noTranslationView.m21(), noTranslationView.m22());
		starmapShader.projectionView.loadMatrix(projectionView);
		
		glActiveTexture(GL_TEXTURE0);
		starCubemap.bind();

		starCube.bind();
		starCube.draw();
		glBindVertexArray(0);
		
		glDepthFunc(GL_LESS);
		glCullFace(GL_BACK);
		starmapShader.unbind();
	}
	
	/**
	 * Free up memory of allocated objects.
	 */
	public void cleanup() {
		if (starmapShader != null) {
			starmapShader.cleanup();
		}
		if (starCubemap != null) {
			starCubemap.cleanup();
		}
		if (starCube != null) {
			starCube.cleanup();
		}
	}
}
