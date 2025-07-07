package com.avogine.shmupemup.render;

import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.GL_BLEND;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11C.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11C.glBlendFunc;
import static org.lwjgl.opengl.GL11C.glViewport;

import com.avogine.io.Window;
import com.avogine.render.SceneRender;
import com.avogine.shmupemup.scene.SpaceScene;

/**
 *
 */
public class SpaceSceneRender implements SceneRender<SpaceScene> {

	private final StarmapRender starmapRender;
	private final PlanetRender planetRender;
	private final SpaceEntityRender spaceEntityRender;
	private final DebugRender debugRender;
	
	/**
	 * 
	 */
	public SpaceSceneRender() {
		starmapRender = new StarmapRender();
		planetRender = new PlanetRender();
		spaceEntityRender = new SpaceEntityRender();
		debugRender = new DebugRender();
	}
	
	@Override
	public void init(Window window) {
		glEnable(GL_BLEND);
//		glBlendEquation(GL_FUNC_ADD);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_DEPTH_TEST);
	}
	
	/**
	 * 
	 */
	public void setupData() {
		starmapRender.init();
		planetRender.init();
		spaceEntityRender.init();
		debugRender.init();
	}
	
	@Override
	public void render(Window window, SpaceScene scene) {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		int windowWidth = window.getWidth();
		int windowHeight = window.getHeight();
		float windowAspect = (float) windowWidth / windowHeight;
		int projectionWidth = scene.getProjection().getWidth();
		int projectionHeight = scene.getProjection().getHeight();
		float projectionAspect = (float) projectionWidth / projectionHeight;
		
		if (windowAspect >= projectionAspect) {
			int adjustedWidth = (int) (projectionAspect * windowHeight);
			int xOffset = (windowWidth - adjustedWidth) / 2;
			glViewport(xOffset, 0, adjustedWidth, windowHeight);
		} else {
			int adjustedHeight = (int) (windowWidth / projectionAspect);
			int yOffset = (windowHeight - adjustedHeight) / 2;
			glViewport(0, yOffset, windowWidth, adjustedHeight);
		}
		
		planetRender.render(scene);
		starmapRender.render(scene);
		spaceEntityRender.render(scene);
		spaceEntityRender.renderAnimations(scene);
		spaceEntityRender.renderInstances(scene);
		spaceEntityRender.renderLasers(scene);
		spaceEntityRender.renderParticles(scene);
		if (window.isDebugMode()) {
			debugRender.renderAABB(scene);
		}
	}

	@Override
	public void cleanup() {
		starmapRender.cleanup();
		planetRender.cleanup();
		spaceEntityRender.cleanup();
		debugRender.cleanup();
	}
	
}
