package com.avogine.shmupemup.game.space;

import java.util.*;

import org.joml.Vector3f;

import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.entities.SpaceEntity;

/**
 *
 */
public class FlightController {
	
	/**
	 * @param start 
	 * @param control1 
	 * @param control2 
	 * @param end 
	 *
	 */
	private record Path(Vector3f start, Vector3f control1, Vector3f control2, Vector3f end) {}
	
	private class Flight {
		final Path path;
		final float duration;
		float time;
		
		public Flight(Path path, float duration) {
			this.path = path;
			this.duration = duration;
		}
		
		public Vector3f moveTo() {
			float t = time / duration;
			
			float u = 1 - t;
			float tt = t * t;
			float uu = u * u;
			float uuu = uu * u;
			float ttt = tt * t;
			
			Vector3f p = new Vector3f(path.start.x * uuu, path.start.y * uuu, path.start.z * uuu);
			p.x += 3 * uu * t * path.control1.x;
			p.y += 3 * uu * t * path.control1.y;
			p.z += 3 * uu * t * path.control1.z;
			p.x += 3 * u * tt * path.control2.x;
			p.y += 3 * u * tt * path.control2.y;
			p.z += 3 * u * tt * path.control2.z;
			p.x += ttt * path.end.x;
			p.y += ttt * path.end.y;
			p.z += ttt * path.end.z;
			
			return p;
		}
	}

	private final Random randy;
	private final Map<SpaceEntity, Flight> entityFlightPaths;
	
	/**
	 * 
	 */
	public FlightController() {
		entityFlightPaths = new HashMap<>();
		randy = new Random();
	}
	
	/**
	 * @param scene
	 */
	public void setupData(SpaceScene scene) {
		scene.getSpaceEntities().filter(entity -> entity.getModelId().equals("alien")).forEach(entity -> entityFlightPaths.put(entity, null));
	}
	
	/**
	 * @param scene
	 * @param delta
	 */
	public void update(SpaceScene scene, float delta) {
		scene.getSpaceEntities()
		.filter(entityFlightPaths::containsKey)
		.forEach(entity -> {
			if (entityFlightPaths.get(entity) == null) {
				entityFlightPaths.put(
						entity,
						new Flight(
								new Path(
										new Vector3f(entity.getPosition()),
										entity.getPosition().add(randy.nextFloat(-5, 5), randy.nextFloat(-5, 5), randy.nextFloat(-5, 5), new Vector3f()),
										entity.getPosition().add(randy.nextFloat(-5, 5), randy.nextFloat(-5, 5), randy.nextFloat(-5, 5), new Vector3f()),
										entity.getPosition().add(randy.nextFloat(-10, 10), randy.nextFloat(-10, 10), randy.nextFloat(-10, 10), new Vector3f())),
								randy.nextFloat(2, 5)));
			}
			var flight = entityFlightPaths.get(entity);
			
			entity.getPosition().set(flight.moveTo());
			entity.updateModelMatrix();
			
			flight.time = Math.clamp(flight.time + delta, 0, flight.duration);
			if (flight.time == flight.duration) {
				entityFlightPaths.put(entity, null);
			}
		});
	}

}
