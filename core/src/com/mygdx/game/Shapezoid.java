package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
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
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;

/**
 * @author GuiDB
 */
public class Shapezoid implements ApplicationListener, Screen {

    // ##### CONSTANTES #####
    final static short PLAYER_FLAG = 1 << 1;
    final static short GROUND_FLAG = 1 << 8;
    final static short OBJECT_FLAG = 1 << 9;
    final static short ALL_FLAG = -1;

    // ##### CLASSES ESTÁTICAS GameObject E Constructor #####
    static class GameObject extends ModelInstance implements Disposable {

        public final btRigidBody body;
        public final MyMotionState motionState;

        public GameObject(Model model, String node, btRigidBodyConstructionInfo constructionInfo) {
            super(model, node);
            motionState = new MyMotionState();
            motionState.transform = transform;
            body = new btRigidBody(constructionInfo);
            body.setMotionState(motionState);
        }

        @Override
        public void dispose() {
            body.dispose();
            motionState.dispose();
        }

        static class Constructor implements Disposable {

            public final Model model;
            public final String node;
            public final btCollisionShape shape;
            public final btRigidBodyConstructionInfo constructionInfo;
            private static Vector3 localInertia = new Vector3();

            public Constructor(Model model, String node, btCollisionShape shape, float mass) {
                this.model = model;
                this.node = node;
                this.shape = shape;
                if (mass > 0f) {
                    shape.calculateLocalInertia(mass, localInertia);
                } else {
                    localInertia.set(0, 0, 0);
                }
                this.constructionInfo = new btRigidBody.btRigidBodyConstructionInfo(mass, null, shape, localInertia);
            }

            public GameObject construct() {
                return new GameObject(model, node, constructionInfo);
            }

            @Override
            public void dispose() {
                shape.dispose();
                constructionInfo.dispose();
            }
        }
    }

    class MyContactListener extends ContactListener {

        @Override
        public boolean onContactAdded(int userValue0, int partId0, int index0, boolean match0,
                int userValue1, int partId1, int index1, boolean match1) {
            // O primeiro objeto é o chão;
            if (match0) {
                ((ColorAttribute) instances.get(userValue0).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.BLACK);
            }
            if (match1) {
                ((ColorAttribute) instances.get(userValue1).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.BLACK);
            }
            return true;
        }
    }

    static class MyMotionState extends btMotionState {

        Matrix4 transform;

        @Override
        public void getWorldTransform(Matrix4 worldTrans) {
            worldTrans.set(transform);
        }

        @Override
        public void setWorldTransform(Matrix4 worldTrans) {
            transform.set(worldTrans);
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
    Vector3 enemyBoxSize = new Vector3(2f, 2f, 2f);
    Vector3 enemyPyramidSize = new Vector3(1f, 2f, 1f);
    Vector3 groundSize = new Vector3(100f, 1f, 100f);
    Vector3 wallNorthSouthSize = new Vector3(50f, 10f, 1f);
    Vector3 wallEastWestSize = new Vector3(1f, 10f, 50f);

    //boolean collision;
    boolean input;
    float playerMovementSpeed = 50f;
    float spawnTimer;

    float angle, speed = 90f;
    float score;

    Array<GameObject> instances;
    ArrayMap<String, GameObject.Constructor> constructors;

    GameObject playerGameObject;
    ArrayMap<String, GameObject.Constructor> playerConstructor;

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

    MyContactListener contactListener;

    btBroadphaseInterface broadphase;

    btDynamicsWorld dynamicsWorld;
    btConstraintSolver constraintSolver;

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
    void checkInput() {
        // Verifica o clique do mouse (botão esquerdo) ou o touch na tela (touchscreen);
        if (Gdx.input.isTouched()) {
            System.out.println("Clicou no pixel X =" + Gdx.input.getX() + " e Y =" + Gdx.input.getY());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            //playerGameObject.transform.translate(-playerMovementSpeed * Gdx.graphics.getDeltaTime(), 0, 0);
            playerGameObject.body.applyForce(new Vector3(-playerMovementSpeed, 0, 0), playerGameObject.transform.getTranslation(playerSize));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            //playerGameObject.transform.translate(playerMovementSpeed * Gdx.graphics.getDeltaTime(), 0, 0);
            playerGameObject.body.applyForce(new Vector3(playerMovementSpeed, 0, 0), playerGameObject.transform.getTranslation(playerSize));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            //playerGameObject.transform.translate(0, 0, -playerMovementSpeed * Gdx.graphics.getDeltaTime());
            playerGameObject.body.applyForce(new Vector3(0, 0, -playerMovementSpeed), playerGameObject.transform.getTranslation(playerSize));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            //playerGameObject.transform.translate(0, 0, playerMovementSpeed * Gdx.graphics.getDeltaTime());
            playerGameObject.body.applyForce(new Vector3(0, 0, playerMovementSpeed), playerGameObject.transform.getTranslation(playerSize));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            System.out.println("checkInput space");
            playerGameObject.body.applyForce(new Vector3(0, playerMovementSpeed * 10f, 0), playerGameObject.transform.getTranslation(playerSize));
            //playerGameObject.transform.translate(0, playerMovementSpeed * 10f * Gdx.graphics.getDeltaTime(), 0);
        }
    }

    public void spawnEnemy() {
        GameObject obj = constructors.values[1 + MathUtils.random(constructors.size - 2)].construct();
        obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
        obj.transform.trn(MathUtils.random(-groundSize.x / 2, groundSize.x / 2), 100f, MathUtils.random(-groundSize.z / 2, groundSize.z / 2));
        //obj.body.setWorldTransform(obj.transform);
        obj.body.proceedToTransform(obj.transform);
        obj.body.setUserValue(instances.size);
        obj.body.setCollisionFlags(obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        instances.add(obj);
        dynamicsWorld.addRigidBody(obj.body);
        // Collision callback filtering
        obj.body.setContactCallbackFlag(OBJECT_FLAG);
        obj.body.setContactCallbackFilter(GROUND_FLAG);
    }

    public void spawnPlayer() {
        playerGameObject = playerConstructor.get("player").construct();
        /*
        playerGameObject.body.setCollisionFlags(playerGameObject.body.getCollisionFlags()
                | btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
        dynamicsWorld.addRigidBody(playerGameObject.body);
        // Collision callback filtering;
        playerGameObject.body.setContactCallbackFlag(GROUND_FLAG);
        playerGameObject.body.setContactCallbackFilter(0);
        // Combinando flags pra criar um filtro;
        //obj.body.setContactCallbackFilter(GROUND_FLAG | WALL_FLAG);
        playerGameObject.body.setActivationState(Collision.DISABLE_DEACTIVATION);
         */

        playerGameObject.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
        playerGameObject.transform.trn(MathUtils.random(-playerSize.x / 2, playerSize.x / 2), 5f, MathUtils.random(-playerSize.z / 2, playerSize.z / 2));
        //obj.body.setWorldTransform(obj.transform);
        playerGameObject.body.proceedToTransform(playerGameObject.transform);
        playerGameObject.body.setUserValue(instances.size);
        playerGameObject.body.setCollisionFlags(playerGameObject.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        instances.add(playerGameObject);
        dynamicsWorld.addRigidBody(playerGameObject.body);
        // Collision callback filtering
        playerGameObject.body.setContactCallbackFlag(OBJECT_FLAG);
        playerGameObject.body.setContactCallbackFilter(GROUND_FLAG);

        /*
        GameObject obj = constructors.values[0].construct();
        obj.transform.setFromEulerAngles(MathUtils.random(360f), MathUtils.random(360f), MathUtils.random(360f));
        obj.transform.trn(MathUtils.random(-playerSize.x / 2, playerSize.x / 2), 5f, MathUtils.random(-playerSize.z / 2, playerSize.z / 2));
        //obj.body.setWorldTransform(obj.transform);
        obj.body.proceedToTransform(obj.transform);
        obj.body.setUserValue(instances.size);
        obj.body.setCollisionFlags(obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        instances.add(obj);
        dynamicsWorld.addRigidBody(obj.body);
        // Collision callback filtering
        obj.body.setContactCallbackFlag(OBJECT_FLAG);
        obj.body.setContactCallbackFilter(GROUND_FLAG);
         */
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
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.4f, 0.4f));

        // ##### Câmera #####;
        perspectiveCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Define a posição da câmera (3 afastado em Z da origem);
        perspectiveCamera.position.set(0, 50f, 50f);
        // Destino que a câmera está apontando (origem);
        perspectiveCamera.lookAt(0, 0, 0);
        // Menor distância que a câmera "captura" (View Distance);
        perspectiveCamera.near = 1f;
        // Maior distância que a câmera "captura" (View Distance);
        perspectiveCamera.far = 300f;
        // Atualiza a câmera;
        perspectiveCamera.update();

        // Construindo os modelos;
        modelBuilder = new ModelBuilder();
        modelBuilder.begin();

        /*
        modelBuilder.node().id = "sphere";
        modelBuilder.part("sphere", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.GREEN)))
                .sphere(1f, 1f, 1f, 10, 10);
         */
        modelBuilder.node().id = "player";
        modelBuilder.part("player", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
                .sphere(playerSize.x, playerSize.y, playerSize.z, 10, 10);
        modelBuilder.node().id = "enemyBox";
        modelBuilder.part("enemyBox", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
                .box(2f, 2f, 2f);
        modelBuilder.node().id = "enemyPyramid";
        modelBuilder.part("enemyPyramid", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
                .box(1f, 2f, 1f);
        modelBuilder.node().id = "ground";
        modelBuilder.part("ground", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.WHITE)))
                .box(groundSize.x, groundSize.y, groundSize.z);

        /*
        modelBuilder.node().id = "wallNorthSouth";
        modelBuilder.part("wallNorthSouth", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BROWN)))
                .box(wallNorthSouthSize.x, wallNorthSouthSize.y, wallNorthSouthSize.z);
        modelBuilder.node().id = "wallEastWest";
        modelBuilder.part("wallEastWest", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BROWN)))
                .box(wallEastWestSize.x, wallEastWestSize.y, wallEastWestSize.z);
         */
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
        constructors.put("ground", new GameObject.Constructor(model, "ground",
                new btBoxShape(new Vector3(groundSize.x / 2, groundSize.y / 2, groundSize.z / 2)), 0f));

        constructors.put("enemyBox", new GameObject.Constructor(model, "enemyBox",
                new btBoxShape(new Vector3(enemyBoxSize.x / 2, enemyBoxSize.y / 2, enemyBoxSize.z / 2)), 1f));
        constructors.put("enemyPyramid", new GameObject.Constructor(model, "enemyPyramid",
                new btBoxShape(new Vector3(enemyPyramidSize.x / 2, enemyPyramidSize.y / 2, enemyPyramidSize.z / 2)), 1f));

        playerConstructor = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
        playerConstructor.put("player", new GameObject.Constructor(model, "player",
                new btSphereShape(playerSize.x / 2), 1f));

        /*
        constructors.put("player", new GameObject.Constructor(model, "player", 
                new btSphereShape(playerSize.x / 2), 1f));
        constructors.put("ground", new GameObject.Constructor(model, "ground", 
                new btBoxShape(new Vector3(groundSize.x / 2, groundSize.y / 2, groundSize.z / 2)), 0f));
         */
 /*
        constructors.put("box", new GameObject.Constructor(model, "box", new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f)), 1f));
        constructors.put("cone", new GameObject.Constructor(model, "cone", new btConeShape(0.5f, 2f), 1f));
        constructors.put("capsule", new GameObject.Constructor(model, "capsule", new btCapsuleShape(.5f, 1f), 1f));
        constructors.put("cylinder", new GameObject.Constructor(model, "cylinder", new btCylinderShape(new Vector3(.5f, 1f, .5f)), 1f));
         */

 /*
        constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
        constructors.put("sphere", new GameObject.Constructor(model, "sphere", new btSphereShape(0.5f)));
        constructors.put("player", new GameObject.Constructor(model, "player",
                new btSphereShape(playerSize.x / 2)));
        constructors.put("ground", new GameObject.Constructor(model, "ground",
                new btBoxShape(new Vector3(groundSize.x / 2, groundSize.y / 2, groundSize.z / 2))));
        constructors.put("wallNorthSouth", new GameObject.Constructor(model, "wallNorthSouth",
                new btBoxShape(new Vector3(wallNorthSouthSize.x / 2, wallNorthSouthSize.y / 2, wallNorthSouthSize.z / 2))));
        constructors.put("wallEastWest", new GameObject.Constructor(model, "wallEastWest",
                new btBoxShape(new Vector3(wallEastWestSize.x / 2, wallEastWestSize.y / 2, wallEastWestSize.z / 2))));
         */
 /*
        instances = new Array<GameObject>();
        instances.add(constructors.get("player").construct());
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
        constraintSolver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -10f, 0));
        contactListener = new MyContactListener();

        instances = new Array<GameObject>();
        GameObject object = constructors.get("ground").construct();
        object.body.setCollisionFlags(object.body.getCollisionFlags()
                | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        instances.add(object);
        dynamicsWorld.addRigidBody(object.body);

        // Collision callback filtering;
        object.body.setContactCallbackFlag(GROUND_FLAG);
        object.body.setContactCallbackFilter(0);
        // Combinando flags pra criar um filtro;
        //obj.body.setContactCallbackFilter(GROUND_FLAG | WALL_FLAG);
        object.body.setActivationState(Collision.DISABLE_DEACTIVATION);

        this.spawnPlayer();
    }

    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void render() {

        System.out.println("FPS: " + Gdx.graphics.getFramesPerSecond());

        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        angle = (angle + delta * speed) % 360f;
        instances.get(0).transform.setTranslation(0, MathUtils.sinDeg(angle) * 2.5f, 0f);

        dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);

        if ((spawnTimer -= delta) < 0) {
            spawnEnemy();
            spawnTimer = 1f;
        }

        this.checkInput();

        //Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Limpa a tela;
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(perspectiveCamera);
        modelBatch.render(instances, environment);
        modelBatch.render(playerGameObject, environment);
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

        modelBatch.dispose();
        model.dispose();

        //backgroundMusic.dispose();
        contactListener.dispose();

        broadphase.dispose();

        dynamicsWorld.dispose();
        constraintSolver.dispose();
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
