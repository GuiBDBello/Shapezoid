package com.mygdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.Shapezoid;

/**
 * @author guilh
 */
public class ScreenGame implements Screen {

    // ##### CONSTANTES #####
    final static short PLAYER_FLAG = 1 << 1;
    final static short GROUND_FLAG = 1 << 8;
    final static short OBJECT_FLAG = 1 << 9;
    final static short ALL_FLAG = -1;

    // ##### CLASSES ESTÁTICAS GameObject E Constructor #####
    static class GameObject extends ModelInstance implements Disposable {

        public final btRigidBody body;
        public final MyMotionState motionState;

        public GameObject(Model model, String node, btRigidBody.btRigidBodyConstructionInfo constructionInfo) {
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
            public final btRigidBody.btRigidBodyConstructionInfo constructionInfo;
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
                setGameOver(true);
            }
            if (match1) {
                //((ColorAttribute) instances.get(userValue1).materials.get(0).get(ColorAttribute.Diffuse)).color.set(Color.RED);
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

    final Shapezoid game;

    public ScreenGame(final Shapezoid game) {
        this.game = game;

        this.setGameOver(false);

        Bullet.init();

        // Lote de modelos;
        modelBatch = new ModelBatch();

        // ##### Ambiente #####
        environment = new Environment();
        // Define a luz ambiente (cor RGB e intensidade);
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        // Adiciona uma luz direcional (cor RGB e direção XYZ);
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.2f, -0.4f, 0.4f));

        // ##### Câmera #####;
        perspectiveCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Define a posição da câmera (3 afastado em Z da origem);
        perspectiveCamera.position.set(0, 100f, 0);
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
        modelBuilder.node().id = "player";
        modelBuilder.part("player", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
                .sphere(playerSize.x, playerSize.y, playerSize.z, (int) playerSize.x * 5, (int) playerSize.x * 5);
        modelBuilder.node().id = "enemyBox";
        modelBuilder.part("enemyBox", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.RED)))
                .box(enemyBoxSize.x, enemyBoxSize.y, enemyBoxSize.z);
        modelBuilder.node().id = "enemyPyramid";
        modelBuilder.part("enemyPyramid", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.YELLOW)))
                .cone(enemyPyramidSize.x, enemyPyramidSize.y, enemyPyramidSize.z, 10);
        modelBuilder.node().id = "ground";
        modelBuilder.part("ground", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, new Material(ColorAttribute.createDiffuse(0.2f, 0.5f, 0.2f, 0.75f)))
                .box(groundSize.x, groundSize.y, groundSize.z);
        model = modelBuilder.end();

        spriteBatch = new SpriteBatch();
        bitmapFont = new BitmapFont();

        // ##### Bullet (física) #####
        constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
        constructors.put("ground", new GameObject.Constructor(model, "ground",
                new btBoxShape(new Vector3(groundSize.x / 2, groundSize.y / 2, groundSize.z / 2)), 0f));

        constructors.put("enemyBox", new GameObject.Constructor(model, "enemyBox",
                new btBoxShape(new Vector3(enemyBoxSize.x / 2, enemyBoxSize.y / 2, enemyBoxSize.z / 2)), 2f));
        constructors.put("enemyPyramid", new GameObject.Constructor(model, "enemyPyramid",
                new btBoxShape(new Vector3(enemyPyramidSize.x / 2, enemyPyramidSize.y / 2, enemyPyramidSize.z / 2)), 1f));

        playerConstructor = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
        playerConstructor.put("player", new GameObject.Constructor(model, "player",
                new btSphereShape(playerSize.x / 2), 1f));

        // ##### Áudio #####
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("data/cnupoc__main-theme.mp3"));
        // Define se a música tocará em loop;
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        // Configurações da Bullet;
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);

        contactListener = new MyContactListener();

        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        constraintSolver = new btSequentialImpulseConstraintSolver();
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, -5f, 0));
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

        prefs = Gdx.app.getPreferences("shapezoid");
        this.highscore = prefs.getFloat("highscore", 0);
        this.spawnPlayer();
    }

    PerspectiveCamera perspectiveCamera;
    Environment environment;

    // Modelos;
    Model model;
    ModelBatch modelBatch;
    ModelBuilder modelBuilder;

    // Sons do jogo;
    private Music backgroundMusic;

    public SpriteBatch spriteBatch;
    public BitmapFont bitmapFont;

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
    float score, highscore;
    Preferences prefs;

    Array<GameObject> instances;
    ArrayMap<String, GameObject.Constructor> constructors;

    GameObject playerGameObject;
    ArrayMap<String, GameObject.Constructor> playerConstructor;

    // Bullet;
    btCollisionConfiguration collisionConfig;
    btDispatcher dispatcher;

    MyContactListener contactListener;

    btBroadphaseInterface broadphase;

    btDynamicsWorld dynamicsWorld;
    btConstraintSolver constraintSolver;

    private boolean gameOver;

    public boolean isGameOver() {
        return this.gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    void checkInput() {
        // Verifica o clique do mouse (botão esquerdo) ou o touch na tela (touchscreen);
        if (Gdx.input.isTouched()) {
            System.out.println("Clicou no pixel X =" + Gdx.input.getX() + " e Y =" + Gdx.input.getY());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            gameOver();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        if (!isGameOver()) {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                playerGameObject.body.applyForce(new Vector3(-playerMovementSpeed, 0, 0), playerGameObject.transform.getTranslation(playerSize));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                playerGameObject.body.applyForce(new Vector3(playerMovementSpeed, 0, 0), playerGameObject.transform.getTranslation(playerSize));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                playerGameObject.body.applyForce(new Vector3(0, 0, -playerMovementSpeed), playerGameObject.transform.getTranslation(playerSize));
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                playerGameObject.body.applyForce(new Vector3(0, 0, playerMovementSpeed), playerGameObject.transform.getTranslation(playerSize));
            }
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
        playerGameObject.body.setContactCallbackFilter(OBJECT_FLAG);
    }

    public void gameOver() {
        if (score > highscore) {
            prefs.putFloat("highscore", score);
            prefs.flush();
        }

        dispose();
        game.setScreen(new ScreenGame(game));
    }

    // ##### INTERFACE Screen #####
    @Override
    public void render(float f) {
        System.out.println("FPS: " + Gdx.graphics.getFramesPerSecond());

        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        angle = (angle + delta * speed) % 360f;
        instances.get(0).transform.setTranslation(0, MathUtils.sinDeg(angle) * 2.5f, 0f);

        if ((spawnTimer -= delta) < 0) {
            spawnEnemy();
            spawnTimer = 0.1f;
        }

        dynamicsWorld.stepSimulation(delta, 5, 1 / 60f);

        //Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Limpa a tela;
        Gdx.gl.glClearColor(0.25f, 0.25f, 0.25f, 0.5f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(perspectiveCamera);
        modelBatch.render(instances, environment);
        modelBatch.render(playerGameObject, environment);
        modelBatch.end();

        this.checkInput();

        if (!isGameOver()) {
            this.score += delta;

            spriteBatch.begin();
            bitmapFont.draw(spriteBatch, "Avoid sharp objects!", Gdx.graphics.getWidth() / 10, Gdx.graphics.getHeight() - 50);
            bitmapFont.draw(spriteBatch, "Score: " + Math.round(this.score * 100.0) / 100.0, Gdx.graphics.getWidth() / 2.2f, Gdx.graphics.getHeight() - 50);
            bitmapFont.draw(spriteBatch, "HighScore: " + Math.round(this.prefs.getFloat("highscore") * 100.0) / 100.0, Gdx.graphics.getWidth() / 1.33f, Gdx.graphics.getHeight() - 50);
            spriteBatch.end();
        } else {
            game.spriteBatch.begin();
            spriteBatch.begin();
            bitmapFont.draw(spriteBatch, "You died! Press 'R' to Restart", Gdx.graphics.getWidth() / 2 - 90, Gdx.graphics.getHeight() / 1.8f);
            bitmapFont.draw(spriteBatch, "Score: " + Math.round(this.score * 100.0) / 100.0, Gdx.graphics.getWidth() / 2 - 37.5f, Gdx.graphics.getHeight() / 2);
            bitmapFont.draw(spriteBatch, "HighScore: " + Math.round(this.highscore * 100.0) / 100.0, Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() / 2.2f);
            spriteBatch.end();
            game.spriteBatch.end();
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        for (GameObject gameObject : instances) {
            gameObject.dispose();
        }
        instances.clear();

        for (GameObject.Constructor constructor : constructors.values()) {
            constructor.dispose();
        }
        constructors.clear();
        //environment.clear();
        playerGameObject.dispose();
        playerConstructor.clear();

        dispatcher.dispose();
        collisionConfig.dispose();

        modelBatch.dispose();
        model.dispose();

        backgroundMusic.dispose();

        contactListener.dispose();
        broadphase.dispose();

        bitmapFont.dispose();

        constraintSolver.dispose();

        /*
        spriteBatch.dispose();

        dynamicsWorld.dispose();
         */
    }
}
