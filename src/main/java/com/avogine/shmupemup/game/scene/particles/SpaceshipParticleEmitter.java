package com.avogine.shmupemup.game.scene.particles;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.joml.Random;
import org.joml.Vector3f;

import com.avogine.game.scene.particles.ParticleEmitter;
import com.avogine.render.opengl.Texture;
import com.avogine.render.opengl.particle.ParticleMesh;
import com.avogine.shmupemup.scene.entities.SpaceEntity;

/**
 *
 */
public class SpaceshipParticleEmitter implements ParticleEmitter {
	
	/**
	 * @param position 
	 * @param speed 
	 * @param r 
	 * @param g 
	 * @param b 
	 * @param a 
	 * @param size 
	 * @param angle 
	 * @param weight 
	 * @param life 
	 */
	public record SpaceParticle(Vector3f position, Vector3f speed,
			byte r, byte g, byte b, byte a,
			float size, float angle, float weight,
			AtomicInteger life) {

	}
	
	private final SpaceEntity spaceship;

	private final List<SpaceParticle> particles;
	
	private final ParticleMesh particleMesh;
	private final Texture particleTexture;
	
	private final Vector3f particleVelocity;
	
	/**
	 * @param spaceship 
	 * @param particleMesh 
	 * @param particleTexture 
	 */
	public SpaceshipParticleEmitter(SpaceEntity spaceship, ParticleMesh particleMesh, Texture particleTexture) {
		this.spaceship = spaceship;
		this.particleMesh = particleMesh;
		this.particleTexture = particleTexture;
		particles = new ArrayList<>();
		particleVelocity = new Vector3f();
	}
	
	@Override
	public void cleanup() {
		particleMesh.cleanup();
		particleTexture.cleanup();
	}
	
	@Override
	public void update(float delta) {
		particles.removeIf(p -> p.life().get() <= 0);
		
		// XXX(Avoroutines) This could be handled in a coroutine to start up/slow down
		if (spaceship.getBody().getAcceleration().lengthSquared() > 0) {
			emitParticles(10);
		}
		
		for (var p : particles) {
			// Delta should currently be in thousandths of a second, so multiply by 1000 and subtract
			if (p.life().addAndGet((int) (-1000 * delta)) <= 0) {
				continue;
			}
			
			p.position().set(spaceship.getPosition());
			
			p.speed().add(p.speed().mul(delta * 9.81f, particleVelocity));
			p.position().add(p.speed().mul(delta, particleVelocity));
		}
		
		getParticleMesh().setCurrentInstances(getParticles().size());
	}
	
	@Override
	public void emitParticles(int count) {
		if (particles.size() < particleMesh.getMaxInstances() - count) {
			Random randy = new Random();
			float spread = 0.5f;
			Vector3f mainDir = spaceship.getOrientation().transformPositiveZ(new Vector3f()).negate();
			for (int i = 0; i < count; i++) {
				var randomDir = new Vector3f(randy.nextFloat() - 0.5f, randy.nextFloat() - 0.5f, randy.nextFloat() - 0.5f).mul(spread);
				particles.add(new SpaceParticle(
						new Vector3f(),
						mainDir.add(randomDir, new Vector3f()),
						(byte) (randy.nextInt(128) + 127), (byte) randy.nextInt(100), (byte) randy.nextInt(30), (byte) randy.nextInt(255),
						randy.nextFloat() * 0.5f, randy.nextFloat(), randy.nextFloat(),
						new AtomicInteger(500)
						));
			}
		}
	}
	
	/**
	 * @return the particles
	 */
	public List<SpaceParticle> getParticles() {
		return particles;
	}
	
	/**
	 * @return the particleMesh
	 */
	public ParticleMesh getParticleMesh() {
		return particleMesh;
	}
	
	/**
	 * @return the particleTexture
	 */
	public Texture getParticleTexture() {
		return particleTexture;
	}
	
}
