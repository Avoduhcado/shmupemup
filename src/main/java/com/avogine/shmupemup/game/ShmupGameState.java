package com.avogine.shmupemup.game;

import com.avogine.game.scene.Scene;
import com.avogine.game.state.GameState;
import com.avogine.io.Window;
import com.avogine.render.Render;

/**
 *
 */
public sealed interface ShmupGameState<S extends Scene, R extends Render<S>> extends GameState<S, R> permits Title, MainGame, Credits {
	/**
	 * @param window
	 */
	public default void input(Window window) {
		
	}
	
	/**
	 * @param interval
	 */
	public default void update(float interval) {
		
	}
	
	/**
	 * @param window
	 */
	public default void render(Window window) {
		render().render(window, scene());
	}
}
