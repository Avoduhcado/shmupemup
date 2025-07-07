package com.avogine.shmupemup.game.space.shots;

import java.util.*;
import java.util.function.Supplier;

import org.joml.*;
import org.joml.Math;

import com.avogine.audio.data.*;
import com.avogine.render.opengl.model.Model;
import com.avogine.shmupemup.scene.entities.SpaceEntity;

/**
 *
 */
public final class DoubleShot extends ShootMode<List<SpaceEntity>> {
	
	private final Vector3f leftOffset;
	private final Vector3f rightOffset;
	
	/**
	 * @param shooter 
	 */
	public DoubleShot(SpaceEntity shooter) {
		super(shooter);
		setShotDelay(0.25f);
		leftOffset = new Vector3f();
		rightOffset = new Vector3f();
	}
	
	@Override
	public List<SpaceEntity> shoot(Model bulletModel, SoundBuffer bulletSoundBuffer, Supplier<Optional<SoundSource>> soundSourceSupplier) {
		var bulletEntityLeft = createBullet(
				new Vector3f().set(shooter.getOrientation().transform(-0.5f, 0, 0, leftOffset).add(shooter.getPosition())),
				new Quaternionf().set(shooter.getOrientation()).get(new AxisAngle4f()),
				bulletModel.getId(),
				bulletModel.getAabb());
		var bulletEntityRight = createBullet(
				new Vector3f().set(shooter.getOrientation().transform(0.5f, 0, 0, rightOffset).add(shooter.getPosition())),
				new Quaternionf().set(shooter.getOrientation()).get(new AxisAngle4f()),
				bulletModel.getId(),
				bulletModel.getAabb());
		
		soundSourceSupplier.get().ifPresent(soundSource -> {
			soundSource.setBuffer(bulletSoundBuffer.getBufferID());
			soundSource.setGain(0.15f + (float) (Math.random() * 0.05f));
			soundSource.play();
		});
		
		return List.of(bulletEntityLeft, bulletEntityRight);
	}
}
