package com.avogine.shmupemup;

import java.util.List;

import com.avogine.Avogine;
import com.avogine.game.AvoGame;
import com.avogine.game.util.Stateful;
import com.avogine.io.Window;
import com.avogine.io.config.WindowPreferences;
import com.avogine.render.opengl.font.Font;
import com.avogine.render.opengl.font.util.FontCache;
import com.avogine.render.opengl.ui.*;
import com.avogine.shmupemup.game.*;

/**
 * 
 */
public class ShmupGame extends AvoGame<ShmupGameState<?, ?>> implements Stateful<ShmupGameState<?, ?>> {
	
	private final FontCache fontCache;
	private final TextRender textRender;
	private final NuklearRender nuklearRender;
	
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
		super();
		fontCache = new FontCache();
		textRender = new TextRender();
		nuklearRender = new NuklearRender();
	}
	
	@Override
	protected void onInit(Window window) {
		textRender.init(window.getWidth(), window.getHeight(), fontCache);
		nuklearRender.init(window);
		
		transitionTo(new Title(gui), window);
	}
	
	@Override
	protected void onCleanup() {
		state.cleanup();
		
		textRender.cleanup();
		fontCache.cleanup();
		nuklearRender.cleanup();
	}
	
	@Override
	protected void onInput(Window window) {
		switch (state) {
			case Title title when title.shouldStartGame() -> transitionTo(new MainGame(), window);
			case MainGame mainGame when mainGame.shouldRevertToTitle() -> transitionTo(new Title(gui), window);
			case MainGame mainGame -> mainGame.input(window);
			default -> {}
		}
	}

	@Override
	public void update(float interval) {
		state.update(interval);
	}

	@Override
	protected void onRender(Window window) {
		state.render(window);
		
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
	public void resize(int width, int height) {
		textRender.resize(width, height);
		nuklearRender.resize(width, height);
		
		switch (state) {
			case Title title -> title.resize(width, height);
			default -> {}
		}
	}

	@Override
	public void setState(ShmupGameState<?, ?> state) {
		this.state = state;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		var game = new ShmupGame();
		
		var avogine = new Avogine("Shmup-em-up", new WindowPreferences(1280, 720, false, 0, false, 144, 15), game);
		avogine.start();
	}

}
