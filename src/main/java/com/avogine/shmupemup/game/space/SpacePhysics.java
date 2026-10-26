package com.avogine.shmupemup.game.space;

import java.util.*;

import org.joml.*;
import org.joml.primitives.AABBf;

import com.avogine.logging.AvoLog;
import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.SpaceScene.EntityType;
import com.avogine.shmupemup.scene.entities.SpaceEntity;
import com.avogine.shmupemup.scene.entities.physics.data.DestroyedBody;

/**
 *
 */
public class SpacePhysics {

	private final Set<EntityType> destroyableTypes = EnumSet.of(EntityType.ENEMY, EntityType.SCENERY);
	private final Set<EntityType> movableTypes = EnumSet.of(EntityType.ENEMY, EntityType.PROJECTILE);
	
	private final List<Collision> collisions;
	
	private final Vector3f scaledVelocity;
	
	private final RayAabIntersection rayIntersection;
	
	private final List<SpaceEntity> pList = new ArrayList<>();
	
	/**
	 * 
	 */
	public SpacePhysics() {
		collisions = new ArrayList<>();
		
		scaledVelocity = new Vector3f();
		
		rayIntersection = new RayAabIntersection();
	}
	
	/**
	 * @param scene
	 * @param delta
	 */
	public void onUpdate(SpaceScene scene, float delta) {
		handleRayCollisions(scene);
		
		handleTimestep(scene, delta);
	}
	
	private void handleRayCollisions(SpaceScene scene) {
		/*
		 * Using plain for-each loops generally outperforms streams by about 3x. The collision detection operation doesn't seem intensive enough to even benefit from parallel streams, although
		 * manual parallelization may have some degree of improvement, but even with 5000 entities the more likely optimization would be spatial culling via an OctTree or something.
		 */
		scene.getEntities(EntityType.PROJECTILE).forEach(projectile -> {
			collisions.clear();
			
			Vector3f origin = projectile.getPosition();
			Vector3f direction = projectile.getBody().getVelocity();
			rayIntersection.set(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z);
			
			destroyableTypes.forEach(type -> scene.getEntities(type).forEach(target -> {
				if (testRayIntersection(projectile, target)) {
					collisions.add(new Collision(projectile, target));
				}
			}));
			
			if (collisions.isEmpty()) {
				return;
			}
			collisions.sort(Collision.BY_SHORTEST_DISTANCE);
			collide(collisions.getFirst());
		});
		
		destroyableTypes.forEach(type -> scene.getEntities(type)
				.removeIf(entity -> entity.getBody().getBodyData() instanceof DestroyedBody));
	}
	
	private boolean testRayIntersection(SpaceEntity source, SpaceEntity target) {
		// Test body bit collision first
		if ((source.getBody().getCollisionBitMask() & target.getBody().getCategoryBitMask()) == 0) {
			return false;
		}
		
		AABBf collider = target.getWorldCollider();
		return rayIntersection.test(collider.minX, collider.minY, collider.minZ, collider.maxX, collider.maxY, collider.maxZ);
	}
	
	private void collide(Collision collision) {
		var sourceBody = collision.source.getBody();
		var targetBody = collision.target.getBody();
		
		AvoLog.log().debug("Colliding! {} {}", collision.source.getModelId(), collision.target.getModelId());
		// TODO Determine whether to destroy the bodies based on the body data
//		sourceBody.setBodyData(new DestroyedBody());
		targetBody.setBodyData(new DestroyedBody());
	}
	
	private void handleTimestep(SpaceScene scene, float delta) {
		movableTypes.forEach(type -> scene.getEntities(type).forEach(entity -> {
			if (entity.getBody().getVelocity().lengthSquared() > 0) {
				entity.getPosition().add(entity.getBody().getVelocity().mul(delta, scaledVelocity));
				entity.updateModelMatrix();
			}
		}));
	}
	
	private record Collision(SpaceEntity source, SpaceEntity target) {
		
		/**
		 * Comparator for sorting collisions by the shortest distance between source and target.
		 */
		public static final Comparator<Collision> BY_SHORTEST_DISTANCE = (collision1, collision2) -> {
			float distanceSquared1 = collision1.source.getPosition().distanceSquared(collision1.target.getPosition());
			float distanceSquared2 = collision2.source.getPosition().distanceSquared(collision2.target.getPosition());
			
			return Float.compare(distanceSquared1, distanceSquared2);
		};
		
		@Override
		public final boolean equals(Object other) {
			if (this == other) return true;
			if (other == null) return false;
			if (!(other instanceof Collision(SpaceEntity otherSource, SpaceEntity otherTarget))) {
				return false;
			}
			return (this.source == otherSource && this.target == otherTarget)
					|| (this.source == otherTarget && this.target == otherSource); 
		}
	}
	
}
