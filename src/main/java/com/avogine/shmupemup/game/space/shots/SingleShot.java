package com.avogine.shmupemup.game.space.shots;

import java.util.Optional;
import java.util.function.Supplier;

import org.joml.*;
import org.joml.Math;

import com.avogine.audio.data.*;
import com.avogine.render.opengl.model.Model;
import com.avogine.shmupemup.scene.entities.SpaceEntity;

/**
 *
 */
public final class SingleShot extends ShootMode<SpaceEntity> {

	/**
	 * @param shooter 
	 */
	public SingleShot(SpaceEntity shooter) {
		super(shooter);
		setShotDelay(0.45f);
	}
	
	@Override
	public SpaceEntity shoot(Model bulletModel, SoundBuffer bulletSoundBuffer, Supplier<Optional<SoundSource>> soundSourceSupplier) {
		soundSourceSupplier.get().ifPresent(soundSource -> {
			soundSource.setBuffer(bulletSoundBuffer.getBufferID());
			soundSource.setGain(0.15f + (float) (Math.random() * 0.05f));
			soundSource.play();
		});
		
		return createBullet(
				new Vector3f().set(shooter.getPosition()),
				new Quaternionf().set(shooter.getOrientation()).get(new AxisAngle4f()),
				bulletModel.getId(),
				bulletModel.getAabb());
	}
	
}
