package com.avogine.shmupemup.game;

import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.*;

import com.avogine.audio.data.*;
import com.avogine.game.scene.Scene;
import com.avogine.game.ui.NuklearGUI;
import com.avogine.io.Window;
import com.avogine.render.Render;
import com.avogine.shmupemup.game.Title.*;
import com.avogine.shmupemup.game.title.TitleMenu;
import com.avogine.util.resource.ResourceConstants;

/**
 * @param scene 
 * @param render 
 */
public record Title(TitleScene scene, TitleRender render) implements ShmupGameState<TitleScene, TitleRender> {

	public static class TitleScene extends Scene {
		
		private final NuklearGUI gui;
		
		private SoundBuffer bgmBuffer;
		private SoundSource bgmSource;
		
		private TitleMenu titleMenu;
		
		private boolean startGame;
//		private SaveGame saveToLoad;

		/**
		 * @param width 
		 * @param height 
		 */
		public TitleScene(int width, int height, NuklearGUI gui) {
			super(width, height);
			this.gui = gui;
		}
		
		@Override
		public void init(Window window) {
			new SoundListener();
			
			bgmBuffer = new SoundBuffer(ResourceConstants.SOUNDS.with("Menu.ogg"));
			bgmSource = new SoundSource(true, false);
			bgmSource.setBuffer(bgmBuffer.getBufferID());
			bgmSource.play();
			
			titleMenu = new TitleMenu(gui, window.getWidth(), window.getHeight(), this::setStartGame, saveGamesExist());
		}
		
		@Override
		public void cleanup() {
			if (bgmSource != null) {
				bgmSource.cleanup();
			}
			if (bgmBuffer != null) {
				bgmBuffer.cleanup();
			}
			if (titleMenu != null) {
				titleMenu.cleanup();
			}
		}
		
		@Override
		public void resize(int width, int height) {
			titleMenu.resize(width, height);
		}
		
		/**
		 * @return the {@link SoundBuffer} to play the title background track from.
		 */
		public SoundBuffer getBgmBuffer() {
			return bgmBuffer;
		}
		
		/**
		 * @return the {@link SoundSource} to play the title background track on.
		 */
		public SoundSource getBgmSource() {
			return bgmSource;
		}
		
		/**
		 * @return the startGame
		 */
		public boolean isStartGame() {
			return startGame;
		}
		
		/**
		 * @param startGame the startGame to set
		 */
		public void setStartGame(boolean startGame) {
			this.startGame = startGame;
		}
		
		/**
		 * @return true if there are any save games available to load from.
		 */
		public boolean saveGamesExist() {
			// TODO Add save games
			return false;
		}
		
		/**
		 * @return the save file to load.
		 */
		public Object getSaveToLoad() {
			return null;
		}
		
		/**
		 * TODO Implement loading save games
		 */
		public void setSaveToLoad() {
			// Not implemented
		}

	}
	
	public static class TitleRender implements Render<TitleScene> {
		
		@Override
		public void init(Window window) {
			glClearColor(75f / 255f, 0.0f, 130f / 255f, 1.0f);
			
			glEnable(GL_BLEND);
			glBlendEquation(GL_FUNC_ADD);
			glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		}
		
		@Override
		public void render(Window window, TitleScene scene) {
			// Nothing to render
		}

		@Override
		public void cleanup() {
			// No cleanup necessary
		}

		@Override
		public void resize(int width, int height) {
			// Nothing to resize
		}
	}
	
	/**
	 * @param gui 
	 */
	public Title(NuklearGUI gui) {
		this(new TitleScene(640, 480, gui), new TitleRender());
	}
	
	/**
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		scene.resize(width, height);
		render.resize(width, height);
	}
	
	/**
	 * Return true if the player selected to start a game.
	 * </p>
	 * This should be used to signal a transition from {@link Title} to the next {@link ShmupGameState} containing
	 * the necessary logic to "play" the game.
	 * @return true if the player selected to start a game.
	 */
	public boolean shouldStartGame() {
		return scene.isStartGame();
	}
}
