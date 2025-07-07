package com.avogine.shmupemup.game.space.shots;

import java.util.Optional;
import java.util.function.Supplier;

import org.joml.*;
import org.joml.Math;
import org.lwjgl.glfw.GLFW;

import com.avogine.audio.data.*;
import com.avogine.render.opengl.model.Model;
import com.avogine.shmupemup.scene.entities.SpaceEntity;
import com.avogine.util.EasingUtils;

/**
 *
 */
public final class HelixShot extends ShootMode<SpaceEntity> {

	private final Vector3f shotOffset;
	
	private final float shotDelayMax;
	private final float shotDelayMin;
	
	private boolean warmingUp;
	private float warmUpTime;
	private float warmUpStart;
	private float warmUpDuration;
	
	private float shotBloom;
	
	/**
	 * @param shooter
	 */
	public HelixShot(SpaceEntity shooter) {
		super(shooter);
		shotDelayMax = 0.65f;
		shotDelayMin = 0.035f;
		setShotDelay(shotDelayMax);
		shotOffset = new Vector3f();
		
		warmingUp = false;
		warmUpDuration = 3.6f;
		
		shotBloom = 12f;
	}

	@Override
	public SpaceEntity shoot(Model bulletModel, SoundBuffer bulletSoundBuffer, Supplier<Optional<SoundSource>> soundSourceSupplier) {
		soundSourceSupplier.get().ifPresent(soundSource -> {
			soundSource.setBuffer(bulletSoundBuffer.getBufferID());
			soundSource.setGain(0.15f + (float) (Math.random() * 0.05f));
			soundSource.play();
		});
		
		double angleRads = Math.toRadians(360f * (GLFW.glfwGetTime() % 1));
		float x = (float) Math.cos(angleRads) * 0.5f;
		float y = (float) Math.sin(angleRads) * 0.5f;
		
		var bulletPosition = shooter.getOrientation().transform(x, y, 0, shotOffset).add(shooter.getPosition());
		return createBullet(
				bulletPosition,
				shooter.getOrientation().rotateAxis(Math.toRadians(shotBloom), x, y, 0, new Quaternionf()).get(new AxisAngle4f()),
				bulletModel.getId(),
				bulletModel.getAabb());
	}
	
	/**
	 * 
	 */
	public void warmUpShotDelay() {
		if (!warmingUp) {
			warmingUp = true;
			warmUpTime = 0f;
			warmUpStart = getShotDelay();
		}
		warmUpTime = Math.clamp(0, warmUpDuration, warmUpTime + getShotDelay());
		
		float easing = EasingUtils.easeInQuad(warmUpTime, warmUpStart, shotDelayMin - warmUpStart, warmUpDuration);
		setShotDelay(Math.clamp(easing, shotDelayMin, shotDelayMax));
	}
	
	/**
	 * @param delta
	 */
	public void windDownShotDelay(float delta) {
		if (warmingUp) {
			warmUpTime = Math.clamp(0, warmUpTime, warmUpTime - delta);
			
			float easing = EasingUtils.easeInQuad(warmUpTime, warmUpStart, shotDelayMin - warmUpStart, warmUpDuration);
			setShotDelay(Math.clamp(easing, shotDelayMin, shotDelayMax));
			
			if (warmUpTime == 0) {
				warmingUp = false;
			}
		}
	}
	
	/**
	 * @param warmUpDuration the warmUpDuration to set
	 */
	public void setWarmUpDuration(float warmUpDuration) {
		this.warmUpDuration = warmUpDuration;
	}
	
	/**
	 * @return the shotBloom
	 */
	public float getShotBloom() {
		return shotBloom;
	}
	
	/**
	 * @param shotBloom the shotBloom to set
	 */
	public void setShotBloom(float shotBloom) {
		this.shotBloom = shotBloom;
	}
	
}
