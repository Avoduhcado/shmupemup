package com.avogine.shmupemup.game;

import com.avogine.game.scene.Scene;
import com.avogine.game.state.GameState;
import com.avogine.render.SceneRender;
import com.avogine.shmupemup.ShmupGame;

/**
 * Custom sealed implementation of {@link GameState} to facilitate easy state switching in {@link ShmupGame}.
 */
public abstract sealed class SpaceGameState<T extends Scene, U extends SceneRender<T>> extends GameState<T, U> permits Title, MainGame, Credits {
	
	/**
	 * @param delta
	 */
	public void update(float delta) {
		// Sub-types can implement as necessary
	}
	
}
