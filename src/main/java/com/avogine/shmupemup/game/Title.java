package com.avogine.shmupemup.game;

import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL14C.*;

import java.util.function.Supplier;

import com.avogine.audio.Audio;
import com.avogine.audio.data.*;
import com.avogine.game.scene.Scene;
import com.avogine.game.ui.NuklearGUI;
import com.avogine.io.Window;
import com.avogine.render.SceneRender;
import com.avogine.shmupemup.game.Title.*;
import com.avogine.shmupemup.game.title.TitleMenu;
import com.avogine.util.resource.ResourceConstants;

/**
 *
 */
public final class Title extends SpaceGameState<TitleScene, TitleRender> {

	/**
	 *
	 */
	public static class TitleScene extends Scene {
		
		private SoundBuffer bgmBuffer;
		private SoundSource bgmSource;
		
		private boolean startGame;
//		private SaveGame saveToLoad;
		
		/**
		 * @param width 
		 * @param height 
		 */
		public TitleScene(int width, int height) {
			super(width, height);
		}
		
		/**
		 * 
		 */
		public void init() {
			bgmBuffer = new SoundBuffer(ResourceConstants.SOUNDS.with("Menu.ogg"));
			bgmSource = new SoundSource(true, false);
			bgmSource.setBuffer(bgmBuffer.getBufferID());
		}
		
		/**
		 * 
		 */
		public void cleanup() {
			if (bgmSource != null) {
				bgmSource.cleanup();
			}
			if (bgmBuffer != null) {
				bgmBuffer.cleanup();
			}
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
	
	/**
	 *
	 */
	public static class TitleRender implements SceneRender<TitleScene> {
		
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
		
	}

	private final Audio audio;
	private final NuklearGUI nuklearContext;
	
	private TitleMenu titleMenu;
	
	/**
	 * @param audio 
	 * @param nuklearContext 
	 * 
	 */
	public Title(Audio audio, NuklearGUI nuklearContext) {
		this.audio = audio;
		this.nuklearContext = nuklearContext;
		
		scene = new TitleScene(640, 360);
		render = new TitleRender();
	}

	@Override
	public void init(Window window) {
		render.init(window);
		scene.init();
		
		initAudio();
		titleMenu = new TitleMenu(nuklearContext, scene);
		titleMenu.resize(window.getWidth(), window.getHeight());
	}
	
	private void initAudio() {
		audio.setListener(new SoundListener());
		audio.addSoundBuffer(scene.getBgmBuffer());
		audio.addSoundSource("BGM", scene.getBgmSource());
		
		audio.playSoundSource("BGM");
	}
	
	@Override
	public void cleanup() {
		render.cleanup();
		scene.cleanup();
		
		audio.removeSoundSource("BGM");
		audio.getSoundBuffers().remove(scene.getBgmBuffer());
		
		titleMenu.cleanup();
		nuklearContext.removeUIElement(titleMenu);
	}
	
	/**
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		titleMenu.resize(width, height);
	}
	
	/**
	 * Return true if the player selected to start a game.
	 * </p>
	 * This should be used to signal a transition from {@link Title} to the next {@link SpaceGameState} containing
	 * the necessary logic to "play" the game.
	 * @return true if the player selected to start a game.
	 */
	public boolean shouldStartGame() {
		return scene.isStartGame();
	}
	
	/**
	 * @return a {@link Supplier} that will produce a new, un-initialized {@link MainGame} state.
	 */
	public Supplier<MainGame> startGame() {
//		if (scene.getSaveToLoad() != null) {
//			return () -> new MainGame(audio, scene.getSaveToLoad());
//		}
		return () -> new MainGame(audio);
	}
	
}
