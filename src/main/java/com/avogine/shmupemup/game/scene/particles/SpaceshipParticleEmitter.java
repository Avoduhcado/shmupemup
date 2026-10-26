package com.avogine.shmupemup.game.scene.particles;

import java.nio.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.joml.Random;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import com.avogine.game.scene.particles.ParticleEmitter;
import com.avogine.render.opengl.particle.ParticleMesh;
import com.avogine.render.opengl.texture.Texture;
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
		// Delta should currently be in thousandth of a second, so multiply by 1000 and subtract
		particles.removeIf(p -> p.life().addAndGet((int) (-1000 * delta)) <= 0);
		
		// XXX(Avoroutines) This could be handled in a coroutine to start up/slow down
		if (spaceship.getBody().getAcceleration().lengthSquared() > 0) {
			emitParticles(10);
		}
		
		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer positionsBuffer = stack.mallocFloat(particles.size() * 4);
			for (var p : particles) {
				p.position().set(spaceship.getPosition());

				p.speed().add(p.speed().mul(delta * 9.81f, particleVelocity));
				p.position().add(p.speed().mul(delta, particleVelocity));
				
				positionsBuffer.put(p.position.x).put(p.position.y).put(p.position.z).put(p.size);
			}
			
			particleMesh.updatePositions(positionsBuffer.flip());
		}
		
		getParticleMesh().setCurrentInstances(getParticles().size());
	}
	
	@Override
	public void emitParticles(int count) {
		if (particles.size() < particleMesh.getMaxInstances() - count) {
			Random randy = new Random();
			float spread = 0.5f;
			Vector3f mainDir = spaceship.getOrientation().transformPositiveZ(new Vector3f()).negate();
			try (MemoryStack stack = MemoryStack.stackPush()) {
				ByteBuffer colorsBuffer = stack.malloc(count * 4);
				
				for (int i = 0; i < count; i++) {
					var randomDir = new Vector3f(randy.nextFloat() - 0.5f, randy.nextFloat() - 0.5f, randy.nextFloat() - 0.5f).mul(spread);
					var randomColor = new byte[] {
							(byte) (randy.nextInt(128) + 127), (byte) randy.nextInt(100), (byte) randy.nextInt(30), (byte) randy.nextInt(255)
					};
					particles.add(new SpaceParticle(
							new Vector3f(),
							mainDir.add(randomDir, new Vector3f()),
							randomColor[0], randomColor[1], randomColor[2], randomColor[3],
							randy.nextFloat() * 0.35f, randy.nextFloat(), randy.nextFloat(),
							new AtomicInteger(500)));
					colorsBuffer.put(randomColor);
				}
				particleMesh.updateColors((particles.size() - count) * 4L, colorsBuffer.flip());
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
