package com.avogine.shmupemup.game.space;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector3f;
import org.joml.primitives.Rectanglef;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import com.avogine.audio.data.SoundBuffer;
import com.avogine.game.scene.*;
import com.avogine.game.scene.controller.*;
import com.avogine.game.util.*;
import com.avogine.io.*;
import com.avogine.io.event.KeyEvent;
import com.avogine.io.event.MouseEvent.MouseButtonEvent;
import com.avogine.io.listener.InputAdapter;
import com.avogine.render.opengl.model.Model;
import com.avogine.shmupemup.game.scene.controller.FollowCamController;
import com.avogine.shmupemup.game.space.shots.*;
import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.entities.*;
import com.avogine.shmupemup.scene.entities.physics.data.TimeToLive;

/**
 *
 */
public class SpaceController extends InputAdapter implements Terminable {

	// TODO Source from window
	private final int width = 640;
	private final int height = 480;
	
	// Cursor params
	private long crosshairCursor;
	private Rectanglef cursorBounds;
	private final Vector3f mouseRay = new Vector3f();
	
	// Camera (view) params
	private float cameraMoveSpeed;
	private float cameraRotateSpeed;
	
	private final FirstPersonCameraController fpsCam;
	private final FreeCamController freeCam;
	private final FollowCamController followCam;
	private boolean flightControls;
	
	// Player shoot controls
	private boolean shooting;
	private float shootCooldown;
	private float shootDelay;
	private ShootMode<?> shootMode;
	
//	private final Vector3f playerVelocity = new Vector3f(0f);
	private final Vector3f playerFriction = new Vector3f();
	private final Vector3f playerRotationalVelocity = new Vector3f();
	private final Vector3f playerRotationalFriction = new Vector3f();
	private final Vector3f playerOrientedX = new Vector3f();
	
	private float playerMoveSpeed;
	private float playerRotateSpeed;
	
	private final float friction;
	private final float rotationalFriction;
	
	private static final Vector3f ZERO = new Vector3f(0);
	
	/**
	 * @param camera 
	 */
	public SpaceController(Camera camera) {
		cursorBounds = new Rectanglef(-0.35f, -0.25f, 0.35f, 0.25f);
		
		cameraMoveSpeed = 2.5f;
		cameraRotateSpeed = 1.75f;
		playerMoveSpeed = 2.5f;
		playerRotateSpeed = 1.75f;
		
		friction = 1.2f;
		rotationalFriction = 1.75f;
		
		shootDelay = 0.15f;
		shootCooldown = shootDelay;
		
		fpsCam = new FirstPersonCameraController(camera);
		freeCam = new FreeCamController(camera);
		followCam = new FollowCamController(null, camera);
	}
	
	/**
	 * @param scene
	 */
	public void setupData(SpaceScene scene) {
		SpaceEntity player = scene.getPlayer();
		
		followCam.setFollowTarget(player.getTransform());
		followCam.updateViewMatrix();
		
		shootMode = new SingleShot(player);
		flightControls = true;
	}
	
	/**
	 * @param window 
	 */
	@Override
	public void init(Window window) {
		crosshairCursor = glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR);
		glfwSetCursor(window.getId(), crosshairCursor);
		captureCursor(window);
		
		window.addInputListener(this);
	}
	
	@Override
	public Registerable terminate() {
		return window -> {
			releaseCursor(window);
			glfwSetCursor(window.getId(), MemoryUtil.NULL);
			glfwDestroyCursor(crosshairCursor);
			
			glfwSetCursorPos(window.getId(), 0, 0);
		};
	}
	
	/**
	 * @param window
	 * @param scene 
	 */
	public void input(Window window, SpaceScene scene) {
		if (!isCursorCaptured(window)) {
			return;
		}
		
		handleKeyboardInput(window.getKeyboard(), scene);
		
		handleMouseInput(window.getMouse());
	}
	
	private void handleKeyboardInput(Keyboard keyboard, SpaceScene scene) {
		var playerAcceleration = scene.getPlayer().getBody().getAcceleration();
		playerAcceleration.zero();
		if (keyboard.isKeyDownWithMod(GLFW_KEY_W, GLFW_MOD_SHIFT)) {
			playerAcceleration.z += 10f;// * (playerAcceleration.z < 350f ? 1 : 0);
		} else if (keyboard.isKeyDown(GLFW_KEY_W)) {
			playerAcceleration.z += 1f;// * (playerAcceleration.z < 35f ? 1 : 0);
		}
		if (keyboard.isKeyDown(GLFW_KEY_S) && scene.getPlayer().getBody().getVelocity().z > 0) {
			playerAcceleration.z += -1f;//Math.clamp(playerAcceleration.z - 0.25f, 0, playerAcceleration.z);
		}
		
		if (keyboard.isKeyDown(GLFW_KEY_A)) {
			playerRotationalVelocity.z += cameraRotateSpeed;
		}
		if (keyboard.isKeyDown(GLFW_KEY_D)) {
			playerRotationalVelocity.z -= cameraRotateSpeed;
		}
		
		if (keyboard.isKeyDown(GLFW.GLFW_KEY_1) && !(getShootMode() instanceof SingleShot)) {
			setShootMode(new SingleShot(scene.getPlayer()));
		}
		if (keyboard.isKeyDown(GLFW.GLFW_KEY_2) && !(getShootMode() instanceof DoubleShot)) {
			setShootMode(new DoubleShot(scene.getPlayer()));
		}
		if (keyboard.isKeyDown(GLFW.GLFW_KEY_3) && !(getShootMode() instanceof HelixShot)) {
			var helixMode = new HelixShot(scene.getPlayer());
//			helixMode.setShotBloom(5f);
//			helixMode.setWarmUpDuration(1.6f);
			setShootMode(helixMode);
		}
		if (keyboard.isKeyDown(GLFW.GLFW_KEY_H)) {
			setFlightControls(true);
		}
		if (keyboard.isKeyDown(GLFW.GLFW_KEY_J)) {
			setFlightControls(false);
		}
	}
	
	private void handleMouseInput(Mouse mouse) {
		if (mouse.isMoved()) {
			double yawDelta = mouse.getDelta().x();
			double pitchDelta = mouse.getDelta().y() * -1; // Reversed since y-coordinates go from bottom to top
			
			float sensitivity = 2.4f;
			playerRotationalVelocity.x += pitchDelta * sensitivity;
			playerRotationalVelocity.y += yawDelta * sensitivity;
		}
		
		shooting = mouse.isButtonDown(GLFW_MOUSE_BUTTON_1);
		
		if (mouse.isScrolled()) {
			followCam.setFollowOffset(followCam.getFollowOffset().x(), followCam.getFollowOffset().y(), followCam.getFollowOffset().z() + (mouse.getScroll().y() * 0.3f));
		}
	}
	
	/**
	 * @param scene
	 * @param delta
	 */
	public void update(SpaceScene scene, float delta) {
//		calculateMouseRay(scene.getProjection(), scene.getCamera());
		
		SpaceEntity player = scene.getPlayer();
		movePlayer(player, delta);
		
//		orientCamera(delta);
		
		updateBullets(scene, delta);
		playerShoot(scene, delta);
		
//		processSecretWormhole(scene, playerTransform);
	}
	
	private void calculateMouseRay(Projection projection, Camera camera) {
//		mouseRay.set((2.0f * mouseX.get(0)) / width - 1.0f, 1.0f - (2.0f * mouseY.get(0)) / height, -1.0f)
//		.mulPosition(projection.invert())
//		.set(mouseRay.x, mouseRay.y, -1.0f)
////		.mulDirection(camera.invert())
//		.normalize();
	}
	
	// TODO This should be in SpacePhysics
	private void movePlayer(SpaceEntity player, float delta) {
		if (playerRotationalVelocity.lengthSquared() > 0) {
			rotate(player, playerRotationalVelocity.x * delta, playerRotationalVelocity.y * delta, playerRotationalVelocity.z * delta);
			followCam.updateViewMatrix();
			
			if (flightControls) {
				playerRotationalVelocity.sub(playerRotationalVelocity.mul(rotationalFriction * delta, playerRotationalFriction));
				if (playerRotationalVelocity.lengthSquared() < 0.001f) {
					playerRotationalVelocity.zero();
				}
			}
		}

		var playerVelocity = player.getBody().getVelocity();
		var playerAcceleration = player.getBody().getAcceleration();
		if (playerAcceleration.lengthSquared() > 0) {
			playerVelocity.add(playerAcceleration.mul(delta, new Vector3f()));
		}
		
		if (playerVelocity.lengthSquared() > 0) {
//			AvoLog.log().info("Speed: {}", playerVelocity.z);
//			playerVelocity.normalize(playerMoveSpeed);
			if (playerVelocity.x > 0) {
				strafeRight(player, playerVelocity.x);
			} else if (playerVelocity.x < 0) {
				strafeLeft(player, -playerVelocity.x);
			}
			if (playerVelocity.y > 0) {
				moveUp(player, playerVelocity.y);
			} else if (playerVelocity.y < 0) {
				moveDown(player, -playerVelocity.y);
			}
			if (playerVelocity.z > 0) {
				moveForwards(player, playerVelocity.z);
			} else if (playerVelocity.z < 0) {
				moveBackwards(player, -playerVelocity.z);
			}
			followCam.updateViewMatrix();
			
			if (flightControls) {
				// TODO This currently means you slow down at a constant rate, which isn't really how friction works
				playerVelocity.sub(playerVelocity.mul(friction * delta, playerFriction));
				if (playerVelocity.length() < delta * 0.1f) {
					playerVelocity.zero();
				}
			}
		}
	}
	
	private void updateBullets(SpaceScene scene, float delta) {
		scene.getSpaceEntities()
		.map(entity -> entity.getBody().getBodyData())
		.filter(TimeToLive.class::isInstance)
		.map(TimeToLive.class::cast)
		.forEach(ttl -> ttl.decrease(delta));
		
		var entitiesToRemove = scene.getSpaceEntities()
				.filter(entity -> entity.getBody().getBodyData() instanceof TimeToLive ttl && ttl.getTtl() <= 0f)
				.toList();
		
		scene.getEntities().removeAll(entitiesToRemove);
	}
	
	private void playerShoot(SpaceScene scene, float delta) {
		if (shooting && shootCooldown == 0) {
			shootCooldown = shootMode.getShotDelay();
			
			Model laserModel = scene.getStaticModels().stream().filter(model -> model.getId().matches("laser")).findFirst().orElse(null);
			SoundBuffer laserSoundBuffer = scene.getSoundBufferCache().get("laser");
			
			switch (shootMode) {
				case SingleShot shot -> scene.addEntity(shot.shoot(laserModel, laserSoundBuffer, scene::getAvailablePlayerSoundSource));
				case DoubleShot shot -> shot.shoot(laserModel, laserSoundBuffer, scene::getAvailablePlayerSoundSource)
				.forEach(scene::addEntity);
				case HelixShot shot -> {
					shot.warmUpShotDelay();
					scene.addEntity(shot.shoot(laserModel, laserSoundBuffer, scene::getAvailablePlayerSoundSource));
				}
			}
		} else if (!shooting && shootMode instanceof HelixShot helix) {
			helix.windDownShotDelay(delta);
		}
		shootCooldown = Math.clamp(shootCooldown - delta, 0, shootCooldown);
	}
	
	private void processSecretWormhole(SpaceScene scene, Transform playerTransform) {
		scene.getSpaceEntities()
		.filter(entity -> entity.getPosition().z > playerTransform.position().z + 5)
		.forEach(entity -> {
			entity.getPosition().sub(0, 0, 100f * entity.getScale1f());
			entity.updateModelMatrix();
		});
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	private void rotate(SpaceEntity entity, float x, float y, float z) {
		//Use modulus to fix values to below 360 then convert values to radians
		float newX = (float) Math.toRadians(x % 360);
		float newY = (float) Math.toRadians(y % 360);
		float newZ = (float) Math.toRadians(z % 360);

		//Create a quaternion with the delta rotation values and conjugate it
		entity.getTransform().rotation().rotationXYZ(newX, newY, newZ).conjugate();
		
		//Multiply this transform by the inverse rotation delta quaternion
		entity.getTransform().orientation().mul(entity.getTransform().rotation());
		
		entity.updateModelMatrix();
	}
	
	/**
	 * @param delta
	 */
	private void moveForwards(SpaceEntity entity, float delta) {
		entity.getTransform().orientation().transformPositiveZ(entity.getTransform().direction()).mul(delta);
		entity.getTransform().position().add(entity.getTransform().direction());
		entity.updateModelMatrix();
	}

	/**
	 * @param delta
	 */
	private void moveBackwards(SpaceEntity entity, float delta) {
		entity.getTransform().orientation().transformPositiveZ(entity.getTransform().direction()).mul(delta);
		entity.getTransform().position().sub(entity.getTransform().direction());
		entity.updateModelMatrix();
	}

	/**
	 * @param delta
	 */
	private void strafeRight(SpaceEntity entity, float delta) {
		entity.getTransform().orientation().transformPositiveX(entity.getTransform().right()).negate().mul(delta);
		entity.getTransform().position().add(entity.getTransform().right());
		entity.updateModelMatrix();
	}

	/**
	 * @param delta
	 */
	private void strafeLeft(SpaceEntity entity, float delta) {
		entity.getTransform().orientation().transformPositiveX(entity.getTransform().right()).negate().mul(delta);
		entity.getTransform().position().sub(entity.getTransform().right());
		entity.updateModelMatrix();
	}
	
	/**
	 * @param delta
	 */
	private void moveUp(SpaceEntity entity, float delta) {
		entity.getTransform().orientation().transformPositiveY(entity.getTransform().up()).mul(delta);
		entity.getTransform().position().add(entity.getTransform().up());
		entity.updateModelMatrix();
	}
	
	/**
	 * @param delta
	 */
	private void moveDown(SpaceEntity entity, float delta) {
		entity.getTransform().orientation().transformPositiveY(entity.getTransform().up()).mul(delta);
		entity.getTransform().position().sub(entity.getTransform().up());
		entity.updateModelMatrix();
	}
	
	
	/**
	 * @return the shootMode
	 */
	public ShootMode<?> getShootMode() {
		return shootMode;
	}
	
	/**
	 * @param shootMode the shootMode to set
	 */
	public void setShootMode(ShootMode<?> shootMode) {
		this.shootMode = shootMode;
	}
	
	/**
	 * @return the flightControls
	 */
	public boolean isFlightControls() {
		return flightControls;
	}
	
	/**
	 * @param flightControls the flightControls to set
	 */
	public void setFlightControls(boolean flightControls) {
		this.flightControls = flightControls;
	}
	
	private boolean isCursorCaptured(Window window) {
		return window.getMouse().isCaptured();
	}
	
	private void captureCursor(Window window) {
		window.setCursorInput(GLFW_CURSOR_DISABLED);
	}
	
	private void releaseCursor(Window window) {
		window.setCursorInput(GLFW_CURSOR_NORMAL);
	}
	
	@Override
	public void keyPressed(KeyEvent event) {
		if (event.key() != GLFW_KEY_ESCAPE) {
			return;
		}
		
		if (isCursorCaptured(event.window())) {
			releaseCursor(event.window());
		} else {
			// TODO Expose setShouldClose() method in Window
			glfwSetWindowShouldClose(event.window().getId(), true);
		}
	}
	
	@Override
	public void mouseClicked(MouseButtonEvent event) {
		if (event.button() != GLFW_MOUSE_BUTTON_1) {
			return;
		}
		if (!isCursorCaptured(event.window())) {
			captureCursor(event.window());
		}
	}

}
