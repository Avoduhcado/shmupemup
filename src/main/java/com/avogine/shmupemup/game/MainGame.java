package com.avogine.shmupemup.game;

import java.util.*;
import java.util.function.Predicate;

import org.joml.*;
import org.joml.Math;
import org.lwjgl.glfw.GLFW;

import com.avogine.audio.data.SoundBuffer;
import com.avogine.io.Window;
import com.avogine.render.opengl.model.StaticModel;
import com.avogine.shmupemup.game.space.*;
import com.avogine.shmupemup.game.space.shots.SingleShot;
import com.avogine.shmupemup.render.SpaceRender;
import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.SpaceScene.EntityType;
import com.avogine.shmupemup.scene.entities.SpaceEntity;

/**
 *
 */
public final class MainGame implements ShmupGameState<SpaceScene, SpaceRender> {
	
	private final SpaceScene scene;
	private final SpaceRender render;
	
	private SpaceController controller;
	private SpacePhysics physics;
	private SingleShot enemyShooter;
	private FlightController flightController;
	private final Map<SpaceEntity, Float> shotCooldowns;
	
	private boolean revertToTitle;
	
	private final Set<EntityType> animatableTypes = EnumSet.of(EntityType.PLAYER, EntityType.ENEMY, EntityType.NPC);
	
	private final Predicate<SpaceEntity> isEnemy;
	
	private static boolean shootingEnabled = false;
	
	/**
	 * 
	 */
	public MainGame() {
		scene = new SpaceScene();
		render = new SpaceRender();
		
		controller = new SpaceController(scene.getCamera());
		physics = new SpacePhysics();
		flightController = new FlightController();
		
		shotCooldowns = new HashMap<>();
		
		isEnemy = entity -> entity.getModelId().equals("alien");
	}

	@Override
	public void init(Window window) {
		ShmupGameState.super.init(window);
		render.setupData();
		controller.setupData(scene);
		flightController.setupData(scene);
		
		enemyShooter = new SingleShot(null);
		scene.getEntities(EntityType.ENEMY).forEach(alien -> shotCooldowns.put(alien, (float) ((Math.random() * 10) + 3)));
		
		window.addRegisterable(controller);
	}
	
	@Override
	public void cleanup() {
		ShmupGameState.super.cleanup();
		controller.cleanup();
	}
	
	@Override
	public void input(Window window) {
		controller.input(window, scene);
		
		if (window.getKeyboard().isKeyDown(GLFW.GLFW_KEY_T)) {
			revertToTitle = true;
		}
	}
	
	@Override
	public void update(float delta) {
		controller.update(scene, delta);
//		getScene().getView().translate(0, 0, 25f * delta).rotateLocal((Math.toRadians(5) * delta), 0, 0.025f, 1);
		
		physics.onUpdate(scene, delta);
		
		scene.getSpaceshipParticleEmitter().update(delta);
		
		shotCooldowns.forEach((enemy, cooldown) -> shotCooldowns.replace(enemy, cooldown - delta));
		
		flightController.update(scene, delta);
		
		scene.getEntities(EntityType.ENEMY).forEach(alien -> {
			if (scene.getPlayer().getPosition().distance(alien.getPosition()) < 100.0f) {
				Vector3f dir = new Vector3f(scene.getPlayer().getPosition()).sub(alien.getPosition()).normalize();
				Quaternionf targetRotation = new Quaternionf().rotateTo(new Vector3f(0f, 0f, 1f), dir);
				float rotatationSpeed = 3.5f;
				alien.getOrientation().slerp(targetRotation, rotatationSpeed * delta);
				
				alien.updateModelMatrix();
				
				if (Math.random() > 0.9 && shotCooldowns.getOrDefault(alien, 0.0f) <= 0.0f && shootingEnabled) {
					enemyShooter.setShooter(alien);
				}
			}
		});
		
		scene.getPlanets().forEach(planet -> planet.getOrientation().rotateY(Math.toRadians(2.5f) * delta));
		
		animatableTypes.forEach(type -> scene.getEntities(type).forEach(entity -> {
			if (Objects.nonNull(entity.getAnimationData())) {
					entity.getAnimationData().nextFrame();
				}
		}));
		
		if (enemyShooter.getShooter() != null && shotCooldowns.getOrDefault(enemyShooter.getShooter(), 0.0f) <= 0.0f) {
			StaticModel laserModel = scene.getStaticModels().stream()
					.filter(model -> model.getId().matches("laser"))
					.findFirst()
					.orElse(null);
			SoundBuffer laserSoundBuffer = scene.getSoundBufferCache().get("laser");
			scene.addEntity(EntityType.PROJECTILE, enemyShooter.shoot(laserModel, laserSoundBuffer, () -> Optional.of(scene.getSoundSource(enemyShooter.getShooter()))));
			shotCooldowns.put(enemyShooter.getShooter(), (float) ((Math.random() * 10) + 3));
		}
	}
	
	/**
	 * @return true if the game should return to the {@link Title}.
	 */
	public boolean shouldRevertToTitle() {
		return revertToTitle;
	}
	
	@Override
	public SpaceScene scene() {
		return scene;
	}

	@Override
	public SpaceRender render() {
		return render;
	}

}
