package com.avogine.shmupemup.game;

import java.util.*;
import java.util.function.Predicate;

import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.avogine.audio.Audio;
import com.avogine.audio.data.SoundBuffer;
import com.avogine.io.Window;
import com.avogine.render.opengl.model.Model;
import com.avogine.shmupemup.game.space.*;
import com.avogine.shmupemup.game.space.shots.SingleShot;
import com.avogine.shmupemup.render.SpaceSceneRender;
import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.entities.SpaceEntity;

/**
 *
 */
public final class MainGame extends SpaceGameState<SpaceScene, SpaceSceneRender> {

	private final Audio audio;
	
	private SpaceController controller;
	private SpacePhysics physics;
	private SingleShot enemyShooter;
	private FlightController flightController;
	private final Map<SpaceEntity, Float> shotCooldowns;
	
	private boolean revertToTitle;
	
	private final Predicate<SpaceEntity> isEnemy;
	
	/**
	 * @param audio 
	 * 
	 */
	public MainGame(Audio audio) {
		this.audio = audio;
		
		scene = new SpaceScene();
		render = new SpaceSceneRender();
		controller = new SpaceController(scene.getCamera());
		physics = new SpacePhysics();
		flightController = new FlightController();
		
		shotCooldowns = new HashMap<>();
		
		isEnemy = entity -> entity.getModelId().equals("alien");
	}

	@Override
	public void init(Window window) {
		render.init(window);
		scene.init(window);

		render.setupData();
		controller.setupData(scene);
		flightController.setupData(scene);
		
		enemyShooter = new SingleShot(null);
		scene.getSpaceEntities().filter(entity -> entity.getModelId().equals("alien")).forEach(alien -> shotCooldowns.put(alien, (float) ((Math.random() * 10) + 3)));
		
		audio.setListener(scene.getSoundListener());
		
		window.addRegisterable(controller);
	}

	@Override
	public void cleanup() {
		render.cleanup();
		scene.cleanup();
	}
	
	@Override
	public void prepareForSwap(Window window) {
		window.removeInputListener(controller);
		window.addRegisterable(controller.terminate());
	}
	
	@Override
	public void update(float delta) {
		controller.update(getScene(), delta);
//		getScene().getView().translate(0, 0, 25f * delta).rotateLocal((Math.toRadians(5) * delta), 0, 0.025f, 1);
		
		physics.onUpdate(scene, delta);
	
		getScene().getPlanets().forEach(planet -> planet.getOrientation().rotateLocalY(Math.toRadians(2.5f) * delta));
		
		getScene().getSpaceshipParticleEmitter().update(delta);
		
		shotCooldowns.forEach((enemy, cooldown) -> shotCooldowns.replace(enemy, cooldown - delta));
		
		flightController.update(scene, delta);
		
		scene.getSpaceEntities()
		.filter(entity -> isEnemy.test(entity) && scene.getPlayer().getPosition().distance(entity.getPosition()) < 100f)
		.forEach(enemy -> {
			enemy.getOrientation().rotationTo(new Vector3f(0, 0, 1), scene.getPlayer().getPosition().sub(enemy.getPosition(), new Vector3f()));
			enemy.updateModelMatrix();

			if (Math.random() > 0.9 && shotCooldowns.getOrDefault(enemy, 0.0f) <= 0.0f) {
				enemyShooter.setShooter(enemy);
			}
		});
		
		if (enemyShooter.getShooter() != null 
				&& shotCooldowns.getOrDefault(enemyShooter.getShooter(), 0.0f) <= 0.0f) {
			Model laserModel = scene.getStaticModels().stream().filter(model -> model.getId().matches("laser")).findFirst().orElse(null);
			SoundBuffer laserSoundBuffer = scene.getSoundBufferCache().get("laser");
			scene.addEntity(enemyShooter.shoot(laserModel, laserSoundBuffer, () -> Optional.of(scene.getSoundSource(enemyShooter.getShooter()))));
			shotCooldowns.put(enemyShooter.getShooter(), (float) ((Math.random() * 10) + 3));
		}
		
		if (scene.bob.getAnimationData() != null) {
			scene.bob.getAnimationData().nextFrame();
		}
	}
	
	/**
	 * @param window
	 */
	public void input(Window window) {
		controller.input(window, scene);
		
		if (window.getKeyboard().isKeyDown(GLFW.GLFW_KEY_T)) {
			revertToTitle = true;
		}
	}
	
	/**
	 * @return true if the game should return to the {@link Title}.
	 */
	public boolean shouldRevertToTitle() {
		return revertToTitle;
	}
	
}
