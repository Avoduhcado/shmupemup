package com.avogine.shmupemup;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11C.glViewport;

import java.util.List;

import org.lwjgl.openal.AL11;

import com.avogine.Avogine;
import com.avogine.audio.Audio;
import com.avogine.game.Game;
import com.avogine.game.state.StateSwappable;
import com.avogine.game.ui.NuklearGUI;
import com.avogine.io.Window;
import com.avogine.io.config.WindowPreferences;
import com.avogine.io.listener.WindowResizeListener;
import com.avogine.render.font.FontCache;
import com.avogine.render.opengl.font.Font;
import com.avogine.render.opengl.ui.*;
import com.avogine.shmupemup.game.*;

/**
 * 
 */
public class ShmupGame implements Game, StateSwappable<SpaceGameState<?, ?>>, WindowResizeListener {

	private final FontCache fontCache;
	private final TextRender textRender;
	
	private final NuklearRender nuklearRender;
	
	private final Audio audio;
	private final NuklearGUI gui;
	
	private final GameStateQueue<SpaceGameState<?, ?>> gameStateQueue;
	
	private final String ipsum = """
			Lorem ipsum odor amet, consectetuer adipiscing elit. Condimentum
			congue tincidunt per ultrices litora torquent nunc ipsum est. 
			Vel nec mauris luctus in, eu erat? Fames id bibendum mus etiam; 
			nisl non. Mus lobortis parturient sagittis posuere sem nam conva-
			llis. Neque vel egestas tristique vulputate maecenas. Luctus vol-
			utpat euismod mattis porttitor nec. Fringilla aenean tincidunt 
			odio ad phasellus. Mus interdum netus, himenaeos class pulvinar 
			ipsum. Lectus primis aptent porta dapibus vestibulum netus bibendum?

			Facilisi dolor interdum dignissim urna justo. Molestie ullamcorper 
			vehicula neque; litora facilisi quis erat. Viverra feugiat praesent 
			finibus commodo lacinia fermentum tempus leo. Duis amet ultricies 
			auctor auctor praesent phasellus? Vivamus nullam netus efficitur 
			sit dis. Suscipit magna sagittis turpis mattis parturient aptent 
			sodales.
			""".stripIndent();
	
	private final List<String> ipsumLines = ipsum.lines().toList();
	
	private Font testFont;
	
	/**
	 * 
	 */
	public ShmupGame() {
		fontCache = new FontCache();
		textRender = new TextRender();
		nuklearRender = new NuklearRender();
		
		audio = new Audio();
		
		gui = new NuklearGUI();
		
		gameStateQueue = new GameStateQueue<>(new Title(audio, gui));
	}
	
	@Override
	public void init(Window window) {
		gui.init(window);
		initAudio();
		currentState().init(window);
		
		textRender.init(window.getFbWidth(), window.getFbHeight(), fontCache);
		nuklearRender.init(window);
		
		window.addResizeListener(this);
	}
	
	private void initAudio() {
		audio.init();
		audio.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
		audio.setListenerVolume(0.1f);
	}

	@Override
	public void input(Window window) {
		gui.input(window);
		gui.layoutElements(window);
		
		switch (currentState()) {
			case Title title when title.shouldStartGame() -> gameStateQueue.queueState(title.startGame());
			case Title title -> {
				// Nothing to do
			}
			case MainGame mainGame when mainGame.shouldRevertToTitle() -> gameStateQueue.queueState(() -> new Title(audio, gui));
			case MainGame mainGame -> mainGame.input(window);
			case Credits credits -> {
				// Not implemented
			}
		}
		
		if (hasQueuedGameState()) {
			swapGameState(window);
		}
	}

	@Override
	public void update(float interval) {
		switch (currentState()) {
			case Title title -> {
				// Wait for user to navigate title menu
			}
			case MainGame mainGame -> mainGame.update(interval);
			case Credits credits -> {
				// Not implemented
			}
		}
	}

	@Override
	public void render(Window window) {
		window.makeCurrent();
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glViewport(0, 0, window.getFbWidth(), window.getFbHeight());
		
		switch (currentState()) {
			case Title title -> title.getRender().render(window, title.getScene());
			case MainGame mainGame -> mainGame.getRender().render(window, mainGame.getScene());
			case Credits credits -> {
				// Not implemented
			}
		}
		
		// TODO Should GUI and Text rendering be abstracted to the Avogine/Window level?
		nuklearRender.render(gui);

		textRender.renderText(0, 0, "FPS: " + window.getFps());
//		float nextLine = 24.0f;
//		for (int i = 0; i < ipsumLines.size(); i++) {
//			float size = switch (i % 3) {
//				case 0 -> 8.0f;
//				case 1 -> 14.0f;
//				default -> 24.0f;
//			};
//			textRender.renderText(0, nextLine, testFont, size, ipsumLines.get(i));
//			nextLine += testFont.getScaledVAdvance(size);
//		}
	}
	
	@Override
	public void cleanup() {
		currentState().cleanup();
		
		textRender.cleanup();
		fontCache.cleanup();
		nuklearRender.cleanup();
		
		audio.cleanup();
		
		gui.cleanup();
	}
	
	@Override
	public SpaceGameState<?, ?> currentState() {
		return gameStateQueue.getCurrentState();
	}
	
	@Override
	public boolean hasQueuedGameState() {
		return gameStateQueue.hasQueuedState();
	}
	
	@Override
	public void swapGameState(Window window) {
		gameStateQueue.swapState(window);
	}
	
	@Override
	public void windowFramebufferResized(int width, int height) {
		textRender.resize(width, height);
		nuklearRender.resize(width, height);
		
		switch (currentState()) {
			case Title title -> title.resize(width, height);
			case MainGame mainGame -> {
				// TODO Calculate the resolution offsets globally for ShmupGame here?
			}
			case Credits credits -> {
				// Not implemented
			}
		}
	}
	
	@Override
	public int getTargetUps() {
		return 30;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		var window = new Window("Shmup-em-up", new WindowPreferences(1280, 720, false, 0, false, 144, 15));
		var game = new ShmupGame();
		
		var avogine = new Avogine(window, game);
		avogine.start();
	}

}
