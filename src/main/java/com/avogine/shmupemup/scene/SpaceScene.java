package com.avogine.shmupemup.scene;

import static com.avogine.util.resource.ResourceConstants.*;
import static org.lwjgl.util.par.ParShapes.par_shapes_free_mesh;

import java.nio.*;
import java.util.*;
import java.util.Random;
import java.util.stream.IntStream;

import org.joml.*;
import org.joml.Math;
import org.joml.primitives.AABBf;
import org.lwjgl.system.*;
import org.lwjgl.util.par.ParShapesMesh;

import com.avogine.audio.data.*;
import com.avogine.entity.AnimationData;
import com.avogine.game.scene.*;
import com.avogine.io.Window;
import com.avogine.logging.AvoLog;
import com.avogine.render.model.Model;
import com.avogine.render.model.material.SimpleMaterial;
import com.avogine.render.model.material.data.BlinnPhongData;
import com.avogine.render.model.mesh.data.*;
import com.avogine.render.opengl.model.*;
import com.avogine.render.opengl.model.mesh.StaticMesh.StaticInstancedMesh;
import com.avogine.render.opengl.model.util.*;
import com.avogine.render.opengl.particle.ParticleMesh;
import com.avogine.render.opengl.particle.ParticleMesh.ParticleData;
import com.avogine.render.opengl.texture.Texture;
import com.avogine.render.opengl.texture.util.*;
import com.avogine.render.util.ParShapesBuilder;
import com.avogine.shmupemup.game.scene.particles.SpaceshipParticleEmitter;
import com.avogine.shmupemup.scene.entities.*;
import com.avogine.shmupemup.scene.entities.physics.*;
import com.avogine.util.ShapeUtils;

/**
 * TODO Add loadLevel function or something similar to be able to change out enemies, planets, scenery, etc
 */
public class SpaceScene extends Scene {

	private static final float FOV = Math.toRadians(90f);
	private static final float NEAR_PLANE = 0.1f;
	private static final float FAR_PLANE = 15000.0f;
	private static final int WIDTH = 16;
	private static final int HEIGHT = 9;

	private final Map<EntityType, List<SpaceEntity>> entityMap;
	private final List<Planet> planets;
	
	private SpaceEntity player;
	private SpaceshipParticleEmitter spaceshipParticleEmitter;
	private final Set<SoundSource> playerSoundSources;
	private SoundListener soundListener;
	
	private final List<StaticModel> staticModels;
	private final List<StaticModel> emissiveModels;
	private final List<StaticModel> instancedModels;
	private final List<AnimatedModel> animatedModels;
	private final TextureCache textureCache;
	private final Map<String, SoundBuffer> soundBufferCache;
	private final Map<SpaceEntity, SoundSource> entitySoundSources;
	
	private final Random random;
	
	/**
	 *
	 */
	public enum EntityType {
		PLAYER,
		ENEMY,
		PROJECTILE,
		SCENERY,
		NPC
	}
	
	/**
	 * 
	 */
	public SpaceScene() {
		super(new Projection(FOV, WIDTH, HEIGHT, NEAR_PLANE, FAR_PLANE), new Camera());
		entityMap = new EnumMap<>(EntityType.class);
		
		planets = new ArrayList<>();
		
		playerSoundSources = new HashSet<>();
		
		staticModels = new ArrayList<>();
		emissiveModels = new ArrayList<>();
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
		projection.setAspectRatio(window.getWidth(), window.getHeight());
		for (var entityType : EntityType.values()) {
			entityMap.put(entityType, new ArrayList<>());
		}

		soundListener = new SoundListener();
		initPlanets();
		initPlayer();
		initLasers();
		initRocks();
		initAliens();
		
//		StaticModel bobModel = ModelLoader.loadModel("bob", MODELS.with("dancing_vampire.dae"), textureCache);
		AnimatedModel bobModel = ModelLoader.loadAnimatedModel("bob", MODELS.with("dancing_vampire.dae"), textureCache);
		animatedModels.add(bobModel);
		
		AnimationData bobAnimation = new AnimationData(bobModel.getAnimations().getFirst());
		var bob = new SpaceEntity(new Vector3f(0, 0, -15), new AxisAngle4f(), 0.015f, bobModel.getId(), new Body(new BodyConstraints(bobModel.getBoundingBox(), 0x0, 0x0)), bobAnimation);
		addEntity(EntityType.NPC, bob);
	}
	
	private void initPlanets() {
		String kuromiTexturePath = TEXTURES.with("kuromi");
		textureCache.getCubemap(kuromiTexturePath, "png");
		var gasGiantMaterial = new SimpleMaterial(new BlinnPhongData(kuromiTexturePath));

		String kuromiPlanetModelId = "kuromiPlanet";
		var planetMesh = ParShapesLoader.loadFromBuilder(builder -> builder.createSphere(4).build(ParShapesLoader.STATIC_MESH_BUILDER));
		StaticModel planetModel = new StaticModel(kuromiPlanetModelId, Map.of(gasGiantMaterial, List.of(planetMesh)));

		var planetPosition = new Vector3f(-1000, -1200, -6500);
		var axisAngle = new AxisAngle4f();
		
		var planet = new Planet(planetPosition, axisAngle, 1500f, planetModel);
		planet.getOrientation().rotateXYZ(Math.toRadians(-5.0f), 0, Math.toRadians(5.0f));
		planets.add(planet);
		
//		planetPosition = new Vector3f(-1000, 600, -3500);
//		axisAngle = new AxisAngle4f(Math.toRadians(-100), 1, 0, 0);
//		planet = new Planet(planetPosition, axisAngle, 500f, planetModel);
//		addEntity(planet);
		
		initAsteroidBelt(planet);
	}
	
	private void initAsteroidBelt(Planet planet) {
		var asteroidMesh = new ParShapesBuilder().createRock(random.nextInt(), 2).build(parMesh -> {
			int instanceCount = 50000;
			FloatBuffer instanceMatrices = MemoryUtil.memAllocFloat(instanceCount * 16);

			float radius = planet.getScale1f() * 3f;
			float offset = planet.getScale1f() / 1.2f;
			
			var rockModelMatrix = new Matrix4f();
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
						rockWorldPosition.rotate(planet.getOrientation()).add(planet.getPosition()),
						rotation, 
						random.nextFloat(1.0f, 3.0f));
				rockModelMatrix.get(i * 16, instanceMatrices);
			}
			
			int vertexCount = parMesh.npoints();
			int triangleCount = parMesh.ntriangles();
			
			FloatBuffer positions = parMesh.points(vertexCount * 3);
			FloatBuffer normals = parMesh.isNull(ParShapesMesh.NORMALS) ? MemoryUtil.memCallocFloat(vertexCount * 3) : parMesh.normals(vertexCount * 3);
			FloatBuffer tangents = MemoryUtil.memCallocFloat(vertexCount * 3);
			FloatBuffer bitangents = MemoryUtil.memCallocFloat(vertexCount * 3);
			FloatBuffer textureCoordinates = parMesh.isNull(ParShapesMesh.TCOORDS) ? MemoryUtil.memCallocFloat(vertexCount * 2) : parMesh.tcoords(vertexCount * 2);
			IntBuffer indices = parMesh.triangles(triangleCount * 3);

			var vertexData = new VertexData(normals, tangents, bitangents, textureCoordinates);
			var instanceData = new InstanceData(instanceMatrices, instanceCount);
			
			try {
				return new StaticInstancedMesh(positions, vertexData, indices, new AABBf(), instanceData);
			} finally {
				if (parMesh.isNull(ParShapesMesh.NORMALS)) {
					MemoryUtil.memFree(normals);
				}
				if (parMesh.isNull(ParShapesMesh.TCOORDS)) {
					MemoryUtil.memFree(textureCoordinates);
				}
				par_shapes_free_mesh(parMesh);
				MemoryUtil.memFree(tangents);
				MemoryUtil.memFree(bitangents);
				MemoryUtil.memFree(instanceMatrices);
			}
		});
		
		String asteroidTextureID = "asteroid";
		textureCache.putTexture(asteroidTextureID, TextureLoader.loadRawTexture(32, 32, (byte) 128, (byte) 128, (byte) 128, (byte) 255));
		String asteroidSpecularID = "asteroidSpec";
		textureCache.putTexture(asteroidSpecularID, TextureLoader.loadRawTexture(32, 32, (byte) 0, (byte) 0, (byte) 0, (byte) 255));
		var rockMaterial = new SimpleMaterial(asteroidTextureID, asteroidSpecularID, 2);
		
		var spaceRockModel = new StaticModel("space-rock", Map.of(rockMaterial, List.of(asteroidMesh)));
		instancedModels.add(spaceRockModel);
	}
	
	private void initPlayer() {
//		var playerMaterial = new Material(new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
//		var playerMesh = ParShapesLoader.loadCubemap(2);
//		var playerModel = new Model("player", playerMesh, playerMaterial);
		var playerModel = ModelLoader.loadModel("player", MODELS.with("backpack", "backpack.obj"), textureCache);
//		Model playerModel = StaticModelLoader.loadModel("player", ResourceConstants.MODEL_PATH + "bunny.obj");
		staticModels.add(playerModel);
		AABBf playerAABB = playerModel.getBoundingBox();
		var playerEntity = new SpaceEntity(
				new Vector3f(0, 0f, 0f),
				new AxisAngle4f(Math.toRadians(180), 0, 1, 0),
				0.333f,
				playerModel.getId(),
				new Body(new BodyConstraints(playerAABB, 0x0, 0x0)));
		addEntity(EntityType.PLAYER, playerEntity);
		// TODO Set the SoundListener transform to the Camera instead of the player
		soundListener.setPosition(playerEntity.getPosition());
		soundListener.setOrientation(playerEntity.getOrientation());
		soundListener.setVelocity(playerEntity.getBody().getVelocity());
		
		int maxParticleCount = 10000;
		Texture particleTexture = textureCache.getTexture(TEXTURES.with("particle.png"));
		
		try (var particleData = new ParticleData(ShapeUtils.generateCircleVertices(0.5f, 18), maxParticleCount)) {
			var particleMesh = new ParticleMesh(particleData);
			spaceshipParticleEmitter = new SpaceshipParticleEmitter(player, particleMesh, particleTexture);
		}
	}
	
	private void initLasers() {
		String laserModelId = "laser";
		var neonMaterial = new SimpleMaterial();
		float bulletScale = 40.0f;
		var bulletMesh = ParShapesLoader.loadFromBuilder(builder -> builder
				.createCapsule()
				.scale(0.2f, bulletScale, 0.2f)
				.rotate(Math.toRadians(90), new float[] {1, 0, 0})
				.translate(0.0f, 0.0f, bulletScale)
				.build(ParShapesLoader.STATIC_MESH_BUILDER));
		var bulletModel = new StaticModel(laserModelId, Map.of(neonMaterial, List.of(bulletMesh)));
		emissiveModels.add(bulletModel);
		
		SoundBuffer buffer = new SoundBuffer(SOUNDS.with("blaster-2.ogg"));
		addSoundBuffer(laserModelId, buffer);
	}
	
	private void initRocks() {
		float asteroidFieldMin = -1500f;
		float asteroidFieldMax = 1500f;

		for (int i = 0; i < 5; i++) {
			var rockMaterial = new SimpleMaterial("asteroid", "asteroidSpec", 2);
			String instanceRockId = "irock" + i;
			StaticInstancedMesh instanceRockMesh = ParShapesLoader.loadInstancedBuilder(new ParShapesBuilder().createRock(random.nextInt(), 1), 100);
			var instanceRockModel = new StaticModel(instanceRockId, Map.of(rockMaterial, List.of(instanceRockMesh)));
			instancedModels.add(instanceRockModel);
			var instanceRockBody = new BodyConstraints(instanceRockMesh.getBoundingBox(), 0x0, 0x0);

			IntStream.range(0, 100)
			.forEach(rockIndex -> {
				var rockEntity = new SpaceEntity(
						new Vector3f(random.nextFloat(asteroidFieldMin, asteroidFieldMax), random.nextFloat(asteroidFieldMin, asteroidFieldMax), random.nextFloat(asteroidFieldMin, asteroidFieldMax)),
						new AxisAngle4f(Math.toRadians(random.nextFloat() * 360), random.nextFloat(), random.nextFloat(), random.nextFloat()), 
						(random.nextFloat() > 0.8f ? random.nextInt(5, 10) : 1),
						instanceRockId,
						new Body(instanceRockBody));
				
				try (MemoryStack stack = MemoryStack.stackPush()) {
					FloatBuffer instanceMatrix = stack.mallocFloat(16);

					rockEntity.getModelMatrix().get(instanceMatrix);
					instanceRockMesh.updateInstanceBuffer(rockIndex * 16L * Float.BYTES, instanceMatrix);
				}
				addEntity(EntityType.SCENERY, rockEntity);
			});

		}
	}
	
	private void initAliens() {
		String alienModelId = "alien";
//		var alienMaterial = new Material(new Vector4f(0.0f, 0.8f, 0.0f, 1.0f));
//		Mesh alienMesh = new ParShapesBuilder().createSphere(2).scale(0.25f, 0.25f, 0.25f).build();
//		var alienModel = new Model(alienModelId, alienMesh, alienMaterial);
		StaticModel alienModel = ModelLoader.loadModel(alienModelId, MODELS.with("bunny.obj"), textureCache);
		addModel(alienModel);
		var alienBodyAABB = alienModel.getBoundingBox();
		var alienBodyConstraints = new BodyConstraints(alienBodyAABB, 0x10, 0x0);
		
		for (int i = 0; i < 100; i++) {
			var enemy = new SpaceEntity(
					new Vector3f(random.nextFloat(-1, 1), random.nextFloat(-1, 1), random.nextFloat(-1, 1)).normalize(random.nextInt(30, 100)),
					new AxisAngle4f(), 
					0.5f,
					alienModelId,
					new Body(alienBodyConstraints));
			addEntity(EntityType.ENEMY, enemy);
		}
	}
	
	/**
	 * 
	 */
	public void cleanup() {
		staticModels.forEach(StaticModel::cleanup);
		emissiveModels.forEach(StaticModel::cleanup);
		instancedModels.forEach(StaticModel::cleanup);
		animatedModels.forEach(AnimatedModel::cleanup);
		planets.forEach(Planet::cleanup);
		playerSoundSources.forEach(SoundSource::cleanup);
		soundBufferCache.values().forEach(SoundBuffer::cleanup);
		
		entityMap.clear();
		planets.clear();
		soundBufferCache.clear();
		playerSoundSources.clear();
		entitySoundSources.clear();
		
		staticModels.clear();
		emissiveModels.clear();
		instancedModels.clear();
		animatedModels.clear();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * @return staticModels
	 */
	public List<StaticModel> getStaticModels() {
		return staticModels;
	}
	
	/**
	 * @return animatedModels
	 */
	public List<AnimatedModel> getAnimatedModels() {
		return animatedModels;
	}
	
	/**
	 * @return emissiveModels
	 */
	public List<StaticModel> getEmissiveModels() {
		return emissiveModels;
	}
	
	/**
	 * @return instancedModels
	 */
	public List<StaticModel> getStaticInstancedModels() {
		return instancedModels;
	}
	
	/**
	 * @param model
	 */
	public void addModel(Model<?, ?> model) {
		switch (model) {
			case StaticModel staticModel -> staticModels.add(staticModel);
			case AnimatedModel animatedModel -> animatedModels.add(animatedModel);
			default -> throw new IllegalArgumentException("Can't add model of type: " + model.getClass());
		}
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
	 * @return planets
	 */
	public List<Planet> getPlanets() {
		return planets;
	}
	
	/**
	 * @param type
	 * @return
	 */
	public List<SpaceEntity> getEntities(EntityType type) {
		return entityMap.get(type);
	}
	
	/**
	 * @param type 
	 * @param entity
	 */
	public void addEntity(EntityType type, SpaceEntity entity) {
		if (type == EntityType.PLAYER) {
			entityMap.put(type, List.of(entity));
			setPlayer(entity);
		} else {
			entityMap.get(type).add(entity);
		}
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
	private void setPlayer(SpaceEntity player) {
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
