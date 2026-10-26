package com.avogine.shmupemup.game.space.shots;

import java.util.Optional;
import java.util.function.Supplier;

import org.joml.*;
import org.joml.primitives.AABBf;

import com.avogine.audio.data.*;
import com.avogine.render.opengl.model.StaticModel;
import com.avogine.shmupemup.scene.entities.SpaceEntity;
import com.avogine.shmupemup.scene.entities.physics.*;
import com.avogine.shmupemup.scene.entities.physics.data.TimeToLive;

/**
 *
 */
public abstract sealed class ShootMode<T> permits SingleShot, DoubleShot, HelixShot {

	protected SpaceEntity shooter;

	/**
	 * In meters per second where 1 meter is 1 unit of size in the game world.
	 */
	private float shotSpeed;
	
	private float shotTTL;
	private float shotDelay;
	
	/**
	 * @param shooter 
	 */
	protected ShootMode(SpaceEntity shooter) {
		this.shooter = shooter;
		shotSpeed = 400f;
		shotTTL = 0.75f;
		shotDelay = 1f;
	}
	
	/**
	 * @param bulletModel 
	 * @param bulletSoundBuffer 
	 * @param soundSourceSupplier 
	 * @return 
	 */
	public abstract T shoot(StaticModel bulletModel, SoundBuffer bulletSoundBuffer, Supplier<Optional<SoundSource>> soundSourceSupplier);
	
	protected SpaceEntity createBullet(Vector3f position, AxisAngle4f orientation, String modelId, AABBf bulletAABB) {
		var bulletBody = new Body(
				new BodyConstraints(
						bulletAABB,
						new Quaternionf(orientation).transformPositiveZ(new Vector3f()).mul(shotSpeed),
						0f,
						shooter.getBody().getCategoryBitMask(),
						0x11 ^ shooter.getBody().getCategoryBitMask()));
		bulletBody.setBodyData(new TimeToLive(getShotTTL()));
		return new SpaceEntity(position, orientation, 1f, modelId, bulletBody);
	}

	/**
	 * @return the shooter
	 */
	public SpaceEntity getShooter() {
		return shooter;
	}
	
	/**
	 * @param shooter
	 */
	public void setShooter(SpaceEntity shooter) {
		this.shooter = shooter;
	}

	/**
	 * @return the shotTTL
	 */
	public float getShotTTL() {
		return shotTTL;
	}
	
	/**
	 * @param shotTTL the shotTTL to set
	 */
	public void setShotTTL(float shotTTL) {
		this.shotTTL = shotTTL;
	}
	
	/**
	 * @return the shotDelay
	 */
	public float getShotDelay() {
		return shotDelay;
	}
	
	/**
	 * @param shotDelay the shotDelay to set
	 */
	public void setShotDelay(float shotDelay) {
		this.shotDelay = shotDelay;
	}
	
}
