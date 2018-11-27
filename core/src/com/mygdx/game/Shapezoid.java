package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.CollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithmConstructionInfo;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDispatcherInfo;
import com.badlogic.gdx.physics.bullet.collision.btManifoldResult;
import com.badlogic.gdx.physics.bullet.collision.btPersistentManifold;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author GuiDB
 */
public class Shapezoid implements ApplicationListener, Screen {
    
    // ##### CONSTANTES #####
    final static short GROUND_FLAG = 1<<8;
    final static short OBJECT_FLAG = 1<<9;
    final static short ALL_FLAG = -1;

    // ##### CLASSES ESTÁTICAS GameObject E Constructor #####
    static class GameObject extends ModelInstance implements Disposable {

        public final btCollisionObject body;
        public boolean moving;

        public GameObject(Model model, String node, btCollisionShape shape) {
            super(model, node);
            body = new btCollisionObject();
            body.setCollisionShape(shape);
        }

        @Override
        public void dispose() {
            body.dispose();
        }

        static class Constructor implements Disposable {

            public final Model model;
            public final String node;
            public final btCollisionShape shape;

            public Constructor(Model model, String node, btCollisionShape shape) {
                this.model = model;
                this.node = node;
                this.shape = shape;
            }

            public GameObject construct() {
                return new GameObject(model, node, shape);
            }

            @Override
            public void dispose() {
                shape.dispose();
            }
        }
    }

    class MyContactListener extends ContactListener {

        @Override
        public boolean onContactAdded(int userValue0, int partId0, int index0,
                int userValue1, int partId1, int index1) {
            if (userValue1 == 0)
                instances.get(userValue0).moving = false;
            else if (userValue0 == 0)
                instances.get(userValue1).moving = false;
            return true;
        }
    }

    PerspectiveCamera perspectiveCamera;
    Environment environment;

    // Modelos;
    Model model;
    ModelBatch modelBatch;
    ModelBuilder modelBuilder;

    /*
    ModelInstance playerInstance;
    ModelInstance groundInstance;
    ModelInstance wallNorthInstance;
    ModelInstance wallSouthInstance;
    ModelInstance wallEastInstance;
    ModelInstance wallWestInstance;
     */
    // Sons do jogo;
    //private Music backgroundMusic;
    Vector3 playerSize = new Vector3(2f, 2f, 2f);
    Vector3 groundSize = new Vector3(50f, 1f, 50f);
    Vector3 wallNorthSouthSize = new Vector3(50f, 10f, 1f);
    Vector3 wallEastWestSize = new Vector3(1f, 10f, 50f);

    //boolean collision;
    boolean input;
    float playerMovementSpeed = 50;
    float spawnTimer;

    Array<GameObject> instances;
    ArrayMap<String, GameObject.Constructor> constructors;

    // Bullet;
    /*
    btCollisionShape playerShape;
    btCollisionShape groundShape;
    btCollisionShape wallNorthSouthShape;
    btCollisionShape wallEastWestShape;

    btCollisionObject playerObject;
    btCollisionObject groundObject;
    btCollisionObject wallNorthObject;
    btCollisionObject wallSouthObject;
    btCollisionObject wallEastObject;
    btCollisionObject wallWestObject;
     */
    btCollisionConfiguration collisionConfig;
    btDispatcher dispatcher;

    CollisionObjectWrapper co0;
    CollisionObjectWrapper co1;

    btCollisionAlgorithmConstructionInfo ci;
    btCollisionAlgorithm algorithm;
    btDispatcherInfo info;
    btManifoldResult result;
    btPersistentManifold manifold;

    MyContactListener contactListener;
    
    btBroadphaseInterface broadphase;
    btCollisionWorld collisionWorld;

    CameraInputController camController;

    /*
    void setInstances() {
        // Instanciando os modelos;
        playerInstance = new ModelInstance(model, "player");
        playerInstance.transform.setToTranslation(0, 10f, 0);

        groundInstance = new ModelInstance(model, "ground");
        wallNorthInstance = new ModelInstance(model, "wallNorthSouth");
        wallNorthInstance.transform.setToTranslation(0, wallNorthSouthSize.y / 2, -wallNorthSouthSize.x / 2);
        wallSouthInstance = new ModelInstance(model, "wallNorthSouth");
        wallSouthInstance.transform.setToTranslation(0, wallNorthSouthSize.y / 2, wallNorthSouthSize.x / 2);
        wallEastInstance = new ModelInstance(model, "wallEastWest");
        wallEastInstance.transform.setToTranslation(wallEastWestSize.z / 2, wallEastWestSize.y / 2, 0);
        wallWestInstance = new ModelInstance(model, "wallEastWest");
        wallWestInstance.transform.setToTranslation(-wallEastWestSize.z / 2, wallEastWestSize.y / 2, 0);
    }

    void setShapes() {
        playerShape = new btSphereShape(playerSize.x / 2);
        groundShape = new btBoxShape(new Vector3(groundSize.x / 2, groundSize.y / 2, groundSize.z / 2));
        wallNorthSouthShape = new btBoxShape(new Vector3(
                wallNorthSouthSize.x / 2, wallNorthSouthSize.y / 2, wallNorthSouthSize.z / 2));
        wallEastWestShape = new btBoxShape(new Vector3(
                wallEastWestSize.x / 2, wallEastWestSize.y / 2, wallEastWestSize.z / 2));
    }

    void setObjects() {
        playerObject = new btCollisionObject();
        playerObject.setCollisionShape(playerShape);
        playerObject.setWorldTransform(playerInstance.transform);

        groundObject = new btCollisionObject();
        groundObject.setCollisionShape(groundShape);
        groundObject.setWorldTransform(groundInstance.transform);

        wallNorthObject = new btCollisionObject();
        wallNorthObject.setCollisionShape(wallNorthSouthShape);
        wallNorthObject.setWorldTransform(wallNorthInstance.transform);

        wallSouthObject = new btCollisionObject();
        wallSouthObject.setCollisionShape(wallNorthSouthShape);
        wallSouthObject.setWorldTransform(wallSouthInstance.transform);

        wallEastObject = new btCollisionObject();
        wallEastObject.setCollisionShape(wallEastWestShape);
        wallEastObject.setWorldTransform(wallEastInstance.transform);

        wallWestObject = new btCollisionObject();
        wallWestObject.setCollisionShape(wallEastWestShape);
        wallWestObject.setWorldTransform(wallWestInstance.transform);
    }
     */

    /*
    void checkInput() {
        // Verifica o clique do mouse (botão esquerdo) ou o touch na tela (touchscreen);
        if (Gdx.input.isTouched()) {
            System.out.println("Clicou no pixel X =" + Gdx.input.getX() + " e Y =" + Gdx.input.getY());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            System.out.println("checkInput left");
            playerInstance.transform.translate(-playerMovementSpeed * Gdx.graphics.getDeltaTime(), 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            System.out.println("checkInput right");
            playerInstance.transform.translate(playerMovementSpeed * Gdx.graphics.getDeltaTime(), 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            System.out.println("checkInput up");
            playerInstance.transform.translate(0, 0, -playerMovementSpeed * Gdx.graphics.getDeltaTime());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            System.out.println("checkInput down");
            playerInstance.transform.translate(0, 0, playerMovementSpeed * Gdx.graphics.getDeltaTime());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            System.out.println("checkInput space");
            playerInstance.transform.translate(0, playerMovementSpeed * Gdx.graphics.getDeltaTime(), 0);
        }
    }
     */
    public void spawn() {
        GameObject obj = constructors.values[1 + MathUtils.random(constructors.size - 2)].construct();
        obj.moving = true;
        obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
        obj.transform.trn(MathUtils.random(-groundSize.x / 2, groundSize.x / 2), 10f, MathUtils.random(-groundSize.z / 2, groundSize.z / 2));
        obj.body.setWorldTransform(obj.transform);
        
        obj.body.setUserValue(instances.size);
        obj.body.setCollisionFlags(obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        
        instances.add(obj);
        
        collisionWorld.addCollisionObject(obj.body, OBJECT_FLAG, GROUND_FLAG);
    }

    // ##### INTERFACE ApplicationListener #####
    @Override
    public void create() {
        Bullet.init();

        // Lote de modelos;
        modelBatch = new ModelBatch();

        // ##### Ambiente #####;
        environment = new Environment();
        // Define a luz ambiente (cor RGB e intensidade);
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        // Adiciona uma luz direcional (cor RGB e direção XYZ);
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        // ##### Câmera #####;
        perspectiveCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Define a posição da câmera (3 afastado em Z da origem);
        perspectiveCamera.position.set(0, 25f, 25f);
        // Destino que a câmera está apontando (origem);
        perspectiveCamera.lookAt(0, 2f, 0);
        // Menor distância que a câmera "captura" (View Distance);
        perspectiveCamera.near = 1f;
        // Maior distância que a câmera "captura" (View Distance);
        perspectiveCamera.far = 300f;
        // Atualiza a câmera;
        perspectiveCamera.update();

        // Construindo os modelos;
        modelBuilder = new ModelBuilder();

        modelBuilder.begin();

        modelBuilder.node().id = "sphere";
        modelBuilder.part("sphere", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.GREEN)))
                .sphere(1f, 1f, 1f, 10, 10);

        modelBuilder.node().id = "player";
        modelBuilder.part("player", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
                .sphere(playerSize.x, playerSize.y, playerSize.z, 10, 10);
        modelBuilder.node().id = "ground";
        modelBuilder.part("ground", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)))
                .box(groundSize.x, groundSize.y, groundSize.z);
        modelBuilder.node().id = "wallNorthSouth";
        modelBuilder.part("wallNorthSouth", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BROWN)))
                .box(wallNorthSouthSize.x, wallNorthSouthSize.y, wallNorthSouthSize.z);
        modelBuilder.node().id = "wallEastWest";
        modelBuilder.part("wallEastWest", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BROWN)))
                .box(wallEastWestSize.x, wallEastWestSize.y, wallEastWestSize.z);

        // ALL BULLET SHAPES;
        /*
        mb.node().id = "ground";
        mb.part("ground", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
            .box(5f, 1f, 5f);
        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.GREEN)))
            .sphere(1f, 1f, 1f, 10, 10);
        modelBuilder.node().id = "box";
        modelBuilder.part("box", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
            .box(1f, 1f, 1f);
        modelBuilder.node().id = "cone";
        modelBuilder.part("cone", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.YELLOW)))
            .cone(1f, 2f, 1f, 10);
        modelBuilder.node().id = "capsule";
        modelBuilder.part("capsule", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.CYAN)))
            .capsule(0.5f, 2f, 10);
        modelBuilder.node().id = "cylinder";
        modelBuilder.part("cylinder", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.MAGENTA)))
            .cylinder(1f, 2f, 1f, 10);
         */
        model = modelBuilder.end();

        // ##### Bullet (física) #####;
        /*
        this.setInstances();
        this.setShapes();
        this.setObjects();
         */
        constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);

        constructors.put("sphere", new GameObject.Constructor(model, "sphere", new btSphereShape(0.5f)));

        constructors.put("player", new GameObject.Constructor(model, "player",
                new btSphereShape(playerSize.x / 2)));
        /*
        constructors.put("ground", new GameObject.Constructor(model, "ground",
                new btBoxShape(new Vector3(groundSize.x / 2, groundSize.y / 2, groundSize.z / 2))));
        constructors.put("wallNorthSouth", new GameObject.Constructor(model, "wallNorthSouth",
                new btBoxShape(new Vector3(wallNorthSouthSize.x / 2, wallNorthSouthSize.y / 2, wallNorthSouthSize.z / 2))));
        constructors.put("wallEastWest", new GameObject.Constructor(model, "wallEastWest",
                new btBoxShape(new Vector3(wallEastWestSize.x / 2, wallEastWestSize.y / 2, wallEastWestSize.z / 2))));
         */
        /*
        instances = new Array<GameObject>();
        //instances.add(constructors.get("player").construct());
        instances.add(constructors.get("ground").construct());
        instances.add(constructors.get("wallNorthSouth").construct());
        instances.add(constructors.get("wallEastWest").construct());
         */
        
        // ALL BULLET OBJECT CONSTRUCTORS;
        /*
        constructors.put("ground", new GameObject.Constructor(model, "ground", new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f))));
        constructors.put("sphere", new GameObject.Constructor(model, "sphere", new btSphereShape(0.5f)));
        constructors.put("box", new GameObject.Constructor(model, "box", new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f))));
        constructors.put("cone", new GameObject.Constructor(model, "cone", new btConeShape(0.5f, 2f)));
        constructors.put("capsule", new GameObject.Constructor(model, "capsule", new btCapsuleShape(.5f, 1f)));
        constructors.put("cylinder", new GameObject.Constructor(model, "cylinder", new btCylinderShape(new Vector3(.5f, 1f, .5f))));
         */
        // Adicionando as instâncias dos modeols aos modelos que serão carregadas;
        /*
        instances = new Array<ModelInstance>();
        instances.add(playerInstance);
        instances.add(groundInstance);
        instances.add(wallNorthInstance);
        instances.add(wallSouthInstance);
        instances.add(wallEastInstance);
        instances.add(wallWestInstance);

        // ##### Áudio #####;
        //backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(""));
        // Define se a música tocará em loop;
        //backgroundMusic.setLooping(true);
         */
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);

        contactListener = new MyContactListener();
        
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        collisionWorld = new btCollisionWorld(dispatcher, broadphase, collisionConfig);
        contactListener = new MyContactListener();

        
        instances = new Array<GameObject>();
        GameObject object = constructors.get("ground").construct();
        instances.add(object);
        
        collisionWorld.addCollisionObject(object.body, GROUND_FLAG, ALL_FLAG);

        camController = new CameraInputController(perspectiveCamera);
        Gdx.input.setInputProcessor(camController);
    }

    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void render() {
        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        for (GameObject obj : instances) {
            if (obj.moving) {
                obj.transform.trn(0f, -delta, 0f);
                obj.body.setWorldTransform(obj.transform);
            }
        }
        
        collisionWorld.performDiscreteCollisionDetection();

        if ((spawnTimer -= delta) < 0) {
            spawn();
            spawnTimer = 1.5f;
        }

        /*
        if (!collision) {
            playerInstance.transform.translate(0f, -delta, 0f);
            playerObject.setWorldTransform(playerInstance.transform);

            collision = checkCollision(playerObject, groundObject);
        }
         */
        //checkInput();
        camController.update();

        //Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Limpa a tela;
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(perspectiveCamera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        for (GameObject obj : instances) {
            obj.dispose();
        }
        instances.clear();

        for (GameObject.Constructor ctor : constructors.values()) {
            ctor.dispose();
        }
        constructors.clear();

        dispatcher.dispose();
        collisionConfig.dispose();

        /*
        playerObject.dispose();
        playerShape.dispose();

        groundObject.dispose();
        groundShape.dispose();

        dispatcher.dispose();
        collisionConfig.dispose();
         */
        modelBatch.dispose();
        model.dispose();

        //instances.clear();
        //backgroundMusic.dispose();
        
        contactListener.dispose();
        
        collisionWorld.dispose();
        broadphase.dispose();
    }

    // ##### INTERFACE Screen #####
    @Override
    public void show() {
    }

    @Override
    public void render(float f) {
    }

    @Override
    public void hide() {
    }
}