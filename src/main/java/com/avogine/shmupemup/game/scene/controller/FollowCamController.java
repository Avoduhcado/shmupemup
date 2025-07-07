package com.avogine.shmupemup.game.scene.controller;

import java.util.Objects;

import org.joml.Vector3f;

import com.avogine.game.scene.Camera;
import com.avogine.shmupemup.scene.entities.Transform;

/**
 *
 */
public class FollowCamController {

	private Transform followTarget;
	private Camera camera;
	
	private final Vector3f targetOffset;
	private final Vector3f center;
	
	private final Vector3f followOffset;
	private final Vector3f eye;
	
	private final Vector3f worldUp;
	
	
	/**
	 * @param followTarget
	 * @param camera
	 * @param targetOffset 
	 * @param followOffset 
	 */
	public FollowCamController(Transform followTarget, Camera camera, Vector3f targetOffset, Vector3f followOffset) {
		this.followTarget = followTarget;
		this.camera = camera;
		
		this.targetOffset = targetOffset;
		center = new Vector3f();
		
		this.followOffset = followOffset;
		eye = new Vector3f();
		
		worldUp = new Vector3f(0, 1, 0);
	}
	
	/**
	 * @param followTarget 
	 * @param camera 
	 */
	public FollowCamController(Transform followTarget, Camera camera) {
		this(followTarget, camera, new Vector3f(0, 0, 1.5f), new Vector3f(0, 1.2f, -3f));
	}
	
	/**
	 * 
	 */
	public void updateViewMatrix() {
		Objects.requireNonNull(followTarget);
		
		followTarget.orientation().transform(targetOffset, center).add(followTarget.position());
		followTarget.orientation().transform(followOffset, eye).add(followTarget.position());
		followTarget.orientation().transformPositiveY(worldUp);
		
		camera.getView().setLookAt(eye, center, worldUp);
	}
	
	/**
	 * @param followTarget the followTarget to set
	 */
	public void setFollowTarget(Transform followTarget) {
		this.followTarget = followTarget;
	}
	
	/**
	 * @return the followOffset
	 */
	public Vector3f getFollowOffset() {
		return followOffset;
	}
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public void setFollowOffset(float x, float y, float z) {
		followOffset.set(x, y, z);
	}
	
}
