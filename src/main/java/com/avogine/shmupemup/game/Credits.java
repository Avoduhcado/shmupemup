package com.avogine.shmupemup.game;

import com.avogine.game.scene.Scene;
import com.avogine.io.Window;
import com.avogine.render.Render;
import com.avogine.shmupemup.game.Credits.*;

/**
 *
 */
public record Credits(CreditsScene scene, CreditsRender render) implements ShmupGameState<CreditsScene, CreditsRender> {

	public static class CreditsScene extends Scene {
		/**
		 * @param width
		 * @param height
		 */
		protected CreditsScene() {
			super(1, 1);
		}

		@Override
		public void init(Window window) {
		}

		@Override
		public void cleanup() {
		}

		@Override
		public void resize(int width, int height) {
		}
	}
	
	public static class CreditsRender implements Render<CreditsScene> {

		@Override
		public void init(Window window) {
		}

		@Override
		public void cleanup() {
		}

		@Override
		public void render(Window window, CreditsScene scene) {
		}

		@Override
		public void resize(int width, int height) {
		}
		
	}
	
	/**
	 * 
	 */
	public Credits() {
		this(new CreditsScene(), new CreditsRender());
	}
}
