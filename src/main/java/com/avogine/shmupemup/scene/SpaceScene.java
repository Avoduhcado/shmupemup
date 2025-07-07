package com.avogine.shmupemup.scene;

import static com.avogine.util.resource.ResourceConstants.*;
import static java.lang.Math.toRadians;
import static org.lwjgl.system.MemoryUtil.*;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.Random;
import java.util.stream.Stream;

import org.joml.*;
import org.joml.Math;
import org.joml.primitives.AABBf;
import org.lwjgl.system.*;
import org.lwjgl.util.par.*;

import com.avogine.audio.data.*;
import com.avogine.entity.AnimationData;
import com.avogine.game.scene.*;
import com.avogine.io.Window;
import com.avogine.logging.AvoLog;
import com.avogine.render.model.mesh.data.*;
import com.avogine.render.model.util.ParShapesBuilder;
import com.avogine.render.opengl.*;
import com.avogine.render.opengl.image.util.TextureCache;
import com.avogine.render.opengl.model.*;
import com.avogine.render.opengl.model.mesh.*;
import com.avogine.render.opengl.model.mesh.data.*;
import com.avogine.render.opengl.model.util.*;
import com.avogine.shmupemup.game.scene.particles.SpaceshipParticleEmitter;
import com.avogine.shmupemup.render.data.EmissiveMaterial;
import com.avogine.shmupemup.scene.entities.*;
import com.avogine.shmupemup.scene.entities.physics.*;
import com.avogine.util.ShapeUtils;

/**
 *
 */
public class SpaceScene extends Scene {

	private static final float FOV = Math.toRadians(90f);
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 15000.0f;
	private static final int WIDTH = 16;
	private static final int HEIGHT = 9;
	
	private final List<Entity> entities;
	private SpaceEntity player;
	public SpaceEntity bob;
	private SpaceshipParticleEmitter spaceshipParticleEmitter;
	private final Set<SoundSource> playerSoundSources;
	private final SoundListener soundListener;
	
	private final List<Model> staticModels;
	private final List<Model> instancedModels;
	private final List<Model> animatedModels;
	private final TextureCache textureCache;
	private final Map<String, SoundBuffer> soundBufferCache;
	private final Map<SpaceEntity, SoundSource> entitySoundSources;
	
	private final Random random;
	
	/**
	 * 
	 */
	public SpaceScene() {
		super(new Projection(FOV, WIDTH, HEIGHT, NEAR_PLANE, FAR_PLANE), new Camera());
		entities = new ArrayList<>();
		soundListener = new SoundListener();
		playerSoundSources = new HashSet<>();
		
		staticModels = new ArrayList<>();
		instancedModels = new ArrayList<>();
		animatedModels = new ArrayList<>();
		textureCache = new TextureCache();
		soundBufferCache = new HashMap<>();
		entitySoundSources = new HashMap<>();
		
		random = new Random();
	}
	
	/**
	 * Initialize Scene parameters such as projection and camera-view matrices.
	 * @param window The {@link Window} this Scene is being displayed in.
	 */
	public void init(Window window) {
		projection.setAspectRatio(window.getFbWidth(), window.getFbHeight());
		
		initPlanets();
		initPlayer();
		initLasers();
		initRocks();
		initAliens();
		
//		StaticModel bobModel = ModelLoader.loadModel("bob", MODELS.with("dancing_vampire.dae"), textureCache);
		Model bobModel = new AvoModelLoader().loadModel("bob", MODELS.with("dancing_vampire.dae"), textureCache, true);
		animatedModels.add(bobModel);
		
		AnimationData bobAnimation = new AnimationData(bobModel.getAnimations().getFirst());
		bob = new SpaceEntity(new Vector3f(0, 0, -15), new AxisAngle4f(), 1.5f, bobModel.getId(), new Body(new BodyConstraints(new AABBf(), 0x0, 0x0)), bobAnimation);
		addEntity(bob);
	}
	
	private void initPlanets() {
		String kuromiTexturePath = TEXTURES.with("kuromi");
		textureCache.getCubemap(kuromiTexturePath, "png");
		var gasGiantMaterial = new Material(kuromiTexturePath);

		String kuromiPlanetModelId = "kuromiPlanet";
		Mesh planetMesh = ParShapesLoader.loadFromBuilder(builder -> builder.createSphere(4).build(ParShapesLoader.STATIC_MESH_BUILDER));
		gasGiantMaterial.getMeshes().add(planetMesh);
		Model planetModel = new Model(kuromiPlanetModelId, gasGiantMaterial);

		var planetPosition = new Vector3f(1000, 1200, -3500);
		var axisAngle = new AxisAngle4f(Math.toRadians(-75), 1, 0, 0);
		
		var planet = new Planet(planetPosition, axisAngle, 500f, planetModel);
		addEntity(planet);
		
//		planetPosition = new Vector3f(-1000, 600, -3500);
//		axisAngle = new AxisAngle4f(Math.toRadians(-100), 1, 0, 0);
//		planet = new Planet(planetPosition, axisAngle, 500f, planetModel);
//		addEntity(planet);
		
		initAsteroidBelt(planet);
	}
	
	private void initAsteroidBelt(Planet planet) {
		var instancedRockMesh = new ParShapesBuilder().createRock(random.nextInt(), 2).build(parMesh -> {
			int instanceCount = 10000;
			FloatBuffer instanceMatrices = MemoryUtil.memAllocFloat(instanceCount * 16);
			FloatBuffer instanceNormals = MemoryUtil.memAllocFloat(instanceCount * 16);

			float radius = planet.getScale1f() * 3f;
			float offset = planet.getScale1f() / 1.2f;
			
			var rockModelMatrix = new Matrix4f();
			var rockModelViewMatrix = new Matrix4f();
			var rockNormalMatrix = new Matrix4f();
			var rockWorldPosition = new Vector3f();
			var rotation = new Quaternionf();
			
			for (int i = 0; i < instanceCount; i++) {
				rotation.setAngleAxis(Math.toRadians(random.nextFloat(0.0f, 360.0f)), random.nextFloat(), random.nextFloat(), random.nextFloat());
				float displacement = radius + random.nextFloat(-offset, offset);
				float theta = random.nextFloat() * Math.PI_TIMES_2_f;
				rockWorldPosition.x = displacement * Math.cos(theta);
				rockWorldPosition.y = (random.nextFloat(-50, 50) * 0.4f);
				rockWorldPosition.z = displacement * Math.sin(theta);
				
				rockModelMatrix.identity().translationRotateScale(
						rockWorldPosition.rotateX(Math.toRadians(205)).add(planet.getPosition()),
						rotation, 
						random.nextFloat(1.0f, 3.0f));
				rockModelMatrix.get(i * 16, instanceMatrices);
				
				rockModelMatrix.mul(getViewMatrix(), rockModelViewMatrix);
				rockModelViewMatrix.invert().transpose(rockNormalMatrix);
				rockNormalMatrix.get(i * 16, instanceNormals);
			}
			
			int vertexCount = parMesh.npoints();
			int vert3 = vertexCount * 3;
			int vert2 = vertexCount * 2;
			var vertexBuffers = new VertexBuffers(memAllocFloat(vert3).put(parMesh.points(vert3)).flip(),
					(!parMesh.isNull(ParShapesMesh.NORMALS) ? memAllocFloat(vert3).put(parMesh.normals(vert3)).flip() : memCallocFloat(vert3)),
					memCallocFloat(vert3),
					memCallocFloat(vert3),
					(!parMesh.isNull(ParShapesMesh.TCOORDS) ? memAllocFloat(vert2).put(parMesh.tcoords(vert2)).flip() : memCallocFloat(vert2)),
					memAllocInt(parMesh.ntriangles() * 3).put(parMesh.triangles(parMesh.ntriangles() * 3)).flip());
			var instancedBuffers = new InstancedBuffers(instanceMatrices, instanceNormals);
			try {
				return new InstancedMesh(new MeshData(vertexBuffers, new AABBf(), 1, instancedBuffers), instanceCount);
			} finally {
				ParShapes.par_shapes_free_mesh(parMesh);
			}
		});
		var rockMaterial = new Material(new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
		rockMaterial.getMeshes().add(instancedRockMesh);
		var instancedRockModel = new Model(planet.getModel().getId() + "-ring", rockMaterial);
		instancedModels.add(instancedRockModel);
	}
	
	private void initPlayer() {
//		var playerMaterial = new Material(new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
//		var playerMesh = ParShapesLoader.loadCubemap(2);
//		var playerModel = new Model("player", playerMesh, playerMaterial);
		var playerModel = new AvoModelLoader().loadModel("player", MODELS.with("backpack", "backpack.obj"), textureCache, false);
//		Model playerModel = StaticModelLoader.loadModel("player", ResourceConstants.MODEL_PATH + "bunny.obj");
		staticModels.add(playerModel);
		AABBf playerAABB = playerModel.getAabb();
		var playerEntity = new SpaceEntity(
				new Vector3f(0, 0f, 0f),
				new AxisAngle4f(Math.toRadians(180), 0, 1, 0),
				0.333f,
				playerModel.getId(),
				new Body(new BodyConstraints(playerAABB, 0x0, 0x0)));
		addEntity(playerEntity);
		setPlayer(playerEntity);
		// TODO Set the SoundListener transform to the Camera instead of the player
		soundListener.setPosition(playerEntity.getPosition());
		soundListener.setOrientation(playerEntity.getOrientation());
		soundListener.setVelocity(playerEntity.getBody().getVelocity());
		
		int maxParticleCount = 10000;
		Texture particleTexture = textureCache.getTexture(TEXTURES.with("particle.png"));
		
		var positions = ShapeUtils.mallocCircleVertices(0.5f, 18);
		var particleMesh = new ParticleMesh(new ParticleMeshData(positions, maxParticleCount));
		spaceshipParticleEmitter = new SpaceshipParticleEmitter(player, particleMesh, particleTexture);
	}
	
	private void initLasers() {
		String laserModelId = "laser";
		var neonMaterial = new EmissiveMaterial(new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
		Mesh bulletMesh = ParShapesLoader.loadFromBuilder(builder -> builder
				.createCapsule()
				.scale(0.2f, 0.2f, 0.2f)
				.rotate((float) toRadians(90), new float[] {1, 0, 0})
				.build(ParShapesLoader.STATIC_MESH_BUILDER));
		neonMaterial.getMeshes().add(bulletMesh);
		var bulletModel = new Model(laserModelId, neonMaterial);
		staticModels.add(bulletModel);
		
		SoundBuffer buffer = new SoundBuffer(SOUNDS.with("blaster-2.ogg"));
		addSoundBuffer(laserModelId, buffer);
	}
	
	private void initRocks() {
		float asteroidFieldMin = -1500f;
		float asteroidFieldMax = 1500f;
		
		for (int i = 0; i < 5; i++) {
			var rockMaterial = new Material(new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
			String instanceRockId = "irock" + i;
			InstancedMesh instanceRockMesh = ParShapesLoader.loadInstancedBuilder(new ParShapesBuilder().createRock(random.nextInt(), 1), 100);
			rockMaterial.getMeshes().add(instanceRockMesh);
			var instanceRockModel = new Model(instanceRockId, rockMaterial);
			instancedModels.add(instanceRockModel);
			//var instanceRockBody =  new BodyConstraints(new AABBf(instanceRockMesh.getAabbMin(), instanceRockMesh.getAabbMax()), 0x0, 0x0); // TODO Do instanced rocks need accurate AABBs?
			var instanceRockBody =  new BodyConstraints(new AABBf(), 0x0, 0x0);
			
			var rockModelViewMatrix = new Matrix4f();
			var rockNormalMatrix = new Matrix4f();

			instanceRockMesh.bind();
			for (int j = 0; j < 1000; j++) {
				var rock = new SpaceEntity(
						new Vector3f(random.nextFloat(asteroidFieldMin, asteroidFieldMax), random.nextFloat(asteroidFieldMin, asteroidFieldMax), random.nextFloat(asteroidFieldMin, asteroidFieldMax)),
						new AxisAngle4f(Math.toRadians(random.nextFloat() * 360), random.nextFloat(), random.nextFloat(), random.nextFloat()), 
						(random.nextFloat() > 0.8f ? random.nextInt(5, 10) : 1),
						instanceRockId,
						new Body(instanceRockBody));
				addEntity(rock);

				try (MemoryStack stack = MemoryStack.stackPush()) {
					FloatBuffer instanceMatrix = stack.mallocFloat(16);
					FloatBuffer instanceNormal = stack.mallocFloat(16);
					rock.getModelMatrix().get(instanceMatrix);

					rock.getModelMatrix().mul(getViewMatrix(), rockModelViewMatrix);
					rockModelViewMatrix.invert().transpose(rockNormalMatrix);
					rockNormalMatrix.get(instanceNormal);

					instanceRockMesh.updateInstanceBuffer(5, j * 16L * Float.BYTES, instanceMatrix);
					instanceRockMesh.updateInstanceBuffer(6, j * 16L * Float.BYTES, instanceNormal);
				}
			}
			VAO.unbind();
		}
	}
	
	private void initAliens() {
		String alienModelId = "alien";
//		var alienMaterial = new Material(new Vector4f(0.0f, 0.8f, 0.0f, 1.0f));
//		Mesh alienMesh = new ParShapesBuilder().createSphere(2).scale(0.25f, 0.25f, 0.25f).build();
//		var alienModel = new Model(alienModelId, alienMesh, alienMaterial);
		var alienModel = new AvoModelLoader().loadModel(alienModelId, MODELS.with("bunny.obj"), textureCache, false);
		addModel(alienModel);
		var alienBodyAABB = alienModel.getAabb();
		var alienBodyConstraints = new BodyConstraints(alienBodyAABB, 0x10, 0x0);
		
		for (int i = 0; i < 100; i++) {
			var enemy = new SpaceEntity(
					new Vector3f(random.nextFloat(-1, 1), random.nextFloat(-1, 1), random.nextFloat(-1, 1)).normalize(random.nextInt(30, 100)),
					new AxisAngle4f(), 
					0.5f,
					alienModelId,
					new Body(alienBodyConstraints));
			addEntity(enemy);
		}
	}
	
	/**
	 * 
	 */
	public void cleanup() {
		staticModels.forEach(Model::cleanup);
		instancedModels.forEach(Model::cleanup);
		animatedModels.forEach(Model::cleanup);
		getPlanets().forEach(Planet::cleanup);
		playerSoundSources.forEach(SoundSource::cleanup);
		soundBufferCache.values().forEach(SoundBuffer::cleanup);
	}
	
	/**
	 * @return staticModels
	 */
	public List<Model> getStaticModels() {
		return staticModels;
	}
	
	/**
	 * @return
	 */
	public List<Model> getAnimatedModels() {
		return animatedModels;
	}
	
	/**
	 * @return
	 */
	public List<Model> getStaticInstancedModels() {
		return instancedModels;
	}
	
	/**
	 * @param model
	 */
	public void addModel(Model model) {
		staticModels.add(model);
	}
	
	/**
	 * @return the textureCache
	 */
	public TextureCache getTextureCache() {
		return textureCache;
	}
	
	/**
	 * @return the soundBufferCache
	 */
	public Map<String, SoundBuffer> getSoundBufferCache() {
		return soundBufferCache;
	}
	
	/**
	 * @param soundId 
	 * @param buffer
	 */
	public void addSoundBuffer(String soundId, SoundBuffer buffer) {
		soundBufferCache.put(soundId, buffer);
	}
	
	/**
	 * @param entity 
	 * @return a {@link SoundSource} tied to the given entity's position.
	 */
	public SoundSource getSoundSource(SpaceEntity entity) {
		return entitySoundSources.computeIfAbsent(entity, k -> {
			var source = new SoundSource(false, false);
			source.setPosition(k.getPosition());
			return source;
		});
	}
	
	/**
	 * @return the entities
	 */
	public List<Entity> getEntities() {
		return entities;
	}
	
	/**
	 * @param entity
	 */
	public void addEntity(Entity entity) {
		entities.add(entity);
	}
	
	/**
	 * @return a stream of {@link SpaceEntity} that contains all planets in the scene.
	 */
	public Stream<Planet> getPlanets() {
		return entities.stream()
				.filter(Planet.class::isInstance)
				.map(Planet.class::cast);
	}
	
	/**
	 * @return a stream of {@link SpaceEntity} that contains all space entities in the scene.
	 */
	public Stream<SpaceEntity> getSpaceEntities() {
		return entities.stream()
				.filter(SpaceEntity.class::isInstance)
				.map(SpaceEntity.class::cast);
	}
	
	/**
	 * @return the player
	 */
	public SpaceEntity getPlayer() {
		return player;
	}
	
	/**
	 * @param player
	 */
	public void setPlayer(SpaceEntity player) {
		this.player = player;
	}
	
	/**
	 * Retrieve the first available {@link SoundSource} for the player that's not currently playing any sounds.
	 * @return an Optional containing a {@link SoundSource} tied to the player's position that is currently not playing anything, or if
	 * all available {@code SoundSource}s are being used, an empty Optional.
	 */
	public Optional<SoundSource> getAvailablePlayerSoundSource() {
		return playerSoundSources.stream()
				.dropWhile(s -> s.isPlaying())
				.findFirst()
				.or(() -> {
					if (playerSoundSources.size() < 32) {
						var playerSoundSource = new SoundSource(false, false);
						playerSoundSource.setPosition(player.getPosition());
						playerSoundSources.add(playerSoundSource);
						AvoLog.log().debug("Player sound source size: {}", playerSoundSources.size());
						return Optional.of(playerSoundSource);
					} else {
						return Optional.empty();
					}
				});
	}
	
	/**
	 * @return the soundListener
	 */
	public SoundListener getSoundListener() {
		return soundListener;
	}
	
	/**
	 * @return the spaceshipParticleEmitter
	 */
	public SpaceshipParticleEmitter getSpaceshipParticleEmitter() {
		return spaceshipParticleEmitter;
	}
	
}
