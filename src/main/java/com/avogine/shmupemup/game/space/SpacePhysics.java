package com.avogine.shmupemup.game.space;

import java.util.Objects;

import org.joml.*;
import org.joml.primitives.Intersectionf;

import com.avogine.logging.AvoLog;
import com.avogine.shmupemup.scene.SpaceScene;
import com.avogine.shmupemup.scene.entities.SpaceEntity;
import com.avogine.shmupemup.scene.entities.physics.data.DestroyedBody;

/**
 *
 */
public class SpacePhysics {

	private float bulletSpeed;
	
	private final Vector3f scaledVelocity;
	
	private final Vector2f intersection;
	
	/**
	 * 
	 */
	public SpacePhysics() {
		scaledVelocity = new Vector3f();
		intersection = new Vector2f();
		bulletSpeed = 100;
	}
	
	/**
	 * @param scene
	 * @param delta
	 */
	public void onUpdate(SpaceScene scene, float delta) {
		handleCollisions(scene, delta);
		
		handleTimestep(scene, delta);
	}
	
	private void handleCollisions(SpaceScene scene, float delta) {
		scene.getSpaceEntities()
		.filter(entity -> !entity.getBody().getVelocity().equals(0, 0, 0) || Objects.equals(entity.getId(), scene.getPlayer().getId()))
		.flatMap(source -> scene.getSpaceEntities()
				.filter(target -> source.getPosition().distance(target.getPosition()) < bulletSpeed * delta && isColliding(source, target, delta))
				.map(collisionTarget -> new Collision(source, collisionTarget))
				.findFirst()
				.stream())
		.distinct()
		.forEach(this::collide);
		
		scene.getEntities().removeIf(entity -> entity instanceof SpaceEntity s && s.getBody().getBodyData() instanceof DestroyedBody);
	}
	
	private boolean isColliding(SpaceEntity source, SpaceEntity target, float delta) {
		float deltaVelocity = delta;//bulletSpeed * delta;
		return (source.getBody().getCollisionBitMask() & target.getBody().getCategoryBitMask()) != 0
				&& Intersectionf.intersectLineSegmentAab(source.getPosition().x, source.getPosition().y, source.getPosition().z,
						source.getPosition().x + (source.getBody().getVelocity().x * deltaVelocity),
						source.getPosition().y + (source.getBody().getVelocity().y * deltaVelocity),
						source.getPosition().z + (source.getBody().getVelocity().z * deltaVelocity),
						target.getWorldCollider().minX, target.getWorldCollider().minY, target.getWorldCollider().minZ,
						target.getWorldCollider().maxX, target.getWorldCollider().maxY, target.getWorldCollider().maxZ,
						intersection) >= Intersectionf.ONE_INTERSECTION;
	}
	
	private void collide(Collision collision) {
		var sourceBody = collision.source.getBody();
		var targetBody = collision.target.getBody();
		
		AvoLog.log().debug("Colliding! {} {}", collision.source.getModelId(), collision.target.getModelId());
		sourceBody.setBodyData(new DestroyedBody());
		targetBody.setBodyData(new DestroyedBody());
	}
	
	private void handleTimestep(SpaceScene scene, float delta) {
		scene.getSpaceEntities()
		.filter(entity -> entity.getBody().getVelocity().lengthSquared() > 0)
//		.filter(entity -> !entity.getBody().getVelocity().equals(0, 0, 0))
		.forEach(entity -> {
			if (entity.getId().equals(scene.getPlayer().getId())) {
				return;
			}
			entity.getPosition().add(entity.getBody().getVelocity().mul(delta, scaledVelocity));
			entity.updateModelMatrix();
		});
	}
	
	private record Collision(SpaceEntity source, SpaceEntity target) {
		
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
