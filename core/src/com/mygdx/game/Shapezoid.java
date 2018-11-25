package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.CollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithmConstructionInfo;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDispatcherInfo;
import com.badlogic.gdx.physics.bullet.collision.btManifoldResult;
import com.badlogic.gdx.physics.bullet.collision.btSphereBoxCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.Array;

/**
 * @author GuiDB
 */
public class Shapezoid implements ApplicationListener, Screen {

    PerspectiveCamera perspectiveCamera;
    Array<ModelInstance> instances;
    Environment environment;

    // Modelos;
    Model model;
    ModelBatch modelBatch;
    ModelBuilder modelBuilder;
    ModelInstance playerInstance;
    ModelInstance groundInstance;
    ModelInstance wallNorthInstance;
    ModelInstance wallSouthInstance;
    ModelInstance wallEastInstance;
    ModelInstance wallWestInstance;

    // Sons do jogo;
    private Music backgroundMusic;

    boolean collision;

    Vector3 playerSize = new Vector3(2f, 2f, 2f);
    Vector3 groundSize = new Vector3(50f, 1f, 50f);
    Vector3 wallNorthSouthSize = new Vector3(50f, 10f, 1f);
    Vector3 wallEastWestSize = new Vector3(1f, 10f, 50f);

    float movementSpeed = 50;

    // Bullet;
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

    btCollisionConfiguration collisionConfig;
    btDispatcher dispatcher;

    btCollisionAlgorithm algorithm;
    btDispatcherInfo info;
    btManifoldResult result;

    CameraInputController camController;

    public Shapezoid() { }

    boolean checkCollision(btCollisionObject collisionObject0, btCollisionObject collisionObject1) {
        CollisionObjectWrapper co0 = new CollisionObjectWrapper(collisionObject0);
        CollisionObjectWrapper co1 = new CollisionObjectWrapper(collisionObject1);

        btCollisionAlgorithmConstructionInfo ci = new btCollisionAlgorithmConstructionInfo();
        ci.setDispatcher1(dispatcher);

        algorithm = new btSphereBoxCollisionAlgorithm(null, ci, co0.wrapper, co1.wrapper, false);
        info = new btDispatcherInfo();
        result = new btManifoldResult(co0.wrapper, co1.wrapper);
        algorithm.processCollision(co0.wrapper, co1.wrapper, info, result);

        boolean r = result.getPersistentManifold().getNumContacts() > 0;

        result.dispose();
        info.dispose();
        algorithm.dispose();
        ci.dispose();
        co0.dispose();
        co1.dispose();

        return r;
    }

    boolean checkInput() {
        return false;
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
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
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
        modelBuilder.node().id = "player";
        modelBuilder.part("sphere", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BLUE)))
                .sphere(playerSize.x, playerSize.y, playerSize.z, 100, 100);
        modelBuilder.node().id = "ground";
        modelBuilder.part("box", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)))
                .box(groundSize.x, groundSize.y, groundSize.z);
        modelBuilder.node().id = "wallNorthSouth";
        modelBuilder.part("box", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BROWN)))
                .box(wallNorthSouthSize.x, wallNorthSouthSize.y, wallNorthSouthSize.z);
        modelBuilder.node().id = "wallEastWest";
        modelBuilder.part("box", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, new Material(ColorAttribute.createDiffuse(Color.BROWN)))
                .box(wallEastWestSize.x, wallEastWestSize.y, wallEastWestSize.z);
        model = modelBuilder.end();

        // Instanciando os modelos;
        playerInstance = new ModelInstance(model, "player");
        playerInstance.transform.setToTranslation(0, 10f, 0);

        groundInstance = new ModelInstance(model, "ground");
        wallNorthInstance = new ModelInstance(model, "wallNorthSouth");
        wallNorthInstance.transform.setToTranslation(0, wallNorthSouthSize.y / 2, -wallNorthSouthSize.x / 2);
        wallSouthInstance = new ModelInstance(model, "wallNorthSouth");
        wallSouthInstance.transform.setToTranslation(0, wallNorthSouthSize.y / 2, wallNorthSouthSize.x / 2);
        wallEastInstance = new ModelInstance(model, "wallEastWest");
        wallEastInstance.transform.setToTranslation(-wallEastWestSize.z / 2, wallEastWestSize.y / 2, 0);
        wallWestInstance = new ModelInstance(model, "wallEastWest");
        wallWestInstance.transform.setToTranslation(wallEastWestSize.z / 2, wallEastWestSize.y / 2, 0);

        // Adicionando os modelos às instâncias que serão carregadas;
        instances = new Array<ModelInstance>();
        instances.add(groundInstance);
        instances.add(wallNorthInstance);
        instances.add(wallSouthInstance);
        instances.add(wallEastInstance);
        instances.add(wallWestInstance);
        instances.add(playerInstance);

        // ##### Áudio #####;
        //backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(""));
        // Define se a música tocará em loop;
        //backgroundMusic.setLooping(true);
        camController = new CameraInputController(perspectiveCamera);
        Gdx.input.setInputProcessor(camController);

        // ##### Bullet (física) #####;
        playerShape = new btSphereShape(playerSize.x / 2);
        playerObject = new btCollisionObject();
        playerObject.setCollisionShape(playerShape);
        playerObject.setWorldTransform(playerInstance.transform);

        groundShape = new btBoxShape(new Vector3(groundSize.x / 2, groundSize.y / 2, groundSize.z / 2));
        groundObject = new btCollisionObject();
        groundObject.setCollisionShape(groundShape);
        groundObject.setWorldTransform(groundInstance.transform);

        wallNorthSouthShape = new btBoxShape(new Vector3(
                wallNorthSouthSize.x / 2, wallNorthSouthSize.y / 2, wallNorthSouthSize.z / 2));

        wallNorthObject = new btCollisionObject();
        wallNorthObject.setCollisionShape(wallNorthSouthShape);
        wallNorthObject.setWorldTransform(wallNorthInstance.transform);

        wallSouthObject = new btCollisionObject();
        wallSouthObject.setCollisionShape(wallNorthSouthShape);
        wallSouthObject.setWorldTransform(wallSouthInstance.transform);

        wallEastWestShape = new btBoxShape(new Vector3(
                wallEastWestSize.x / 2, wallEastWestSize.y / 2, wallEastWestSize.z / 2));

        wallEastObject = new btCollisionObject();
        wallEastObject.setCollisionShape(wallEastWestShape);
        wallEastObject.setWorldTransform(wallEastInstance.transform);

        wallWestObject = new btCollisionObject();
        wallWestObject.setCollisionShape(wallEastWestShape);
        wallWestObject.setWorldTransform(wallWestInstance.transform);

        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
    }

    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void render() {
        final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        if (!collision) {
            playerInstance.transform.translate(0f, -delta, 0f);
            playerObject.setWorldTransform(playerInstance.transform);

            //collision = checkCollision(playerObject, groundObject);
            collision = checkCollision(playerObject, groundObject);
        }

        // Verifica o clique do mouse (botão esquerdo) ou o touch na tela (touchscreen);
        if (Gdx.input.isTouched()) {
            System.out.println("Clicou no pixel X =" + Gdx.input.getX() + " e Y =" + Gdx.input.getY());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            System.out.println("render left");
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            playerInstance.transform.translate(-movementSpeed * delta, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            System.out.println("render right");
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            playerInstance.transform.translate(movementSpeed * delta, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            System.out.println("render left");
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            playerInstance.transform.translate(0, 0, -movementSpeed * delta);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            System.out.println("render left");
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            playerInstance.transform.translate(0, 0, movementSpeed * delta);
        }

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
        playerObject.dispose();
        playerShape.dispose();

        groundObject.dispose();
        groundShape.dispose();

        dispatcher.dispose();
        collisionConfig.dispose();

        modelBatch.dispose();
        model.dispose();

        instances.clear();

        //backgroundMusic.dispose();
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
