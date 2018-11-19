package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

/**
 * @author GuiDB
 */
public class Shapezoid implements Screen, InputProcessor {

    final MainMenu game;

    // PerspectiveCamera é usada para 3D, possui profundidade;
    private PerspectiveCamera perspectiveCamera;
    // Guarda vários modelos, carrega mais rapidamente que modelos individuais, pois realiza apenas uma chamada;
    private ModelBatch modelBatch;
    // Cria os modelis;
    private ModelBuilder modelBuilder;
    // Objeto 3D;
    private Model player;
    private Model enemyTriangle;
    private Model enemySquare;
    // Instância de um Model;
    private ModelInstance modelInstance;
    // Gerencia shaders, luzes, etc.;
    private Environment environment;

    public Vector3 origin = new Vector3(0f, 0f, 0f);

    public Vector3 axisX = new Vector3(1f, 0f, 0f);
    public Vector3 axisY = new Vector3(0f, 1f, 0f);
    public Vector3 axisZ = new Vector3(0f, 0f, 1f);

    public Shapezoid(final MainMenu game) {
        this.game = game;

        // Cria a câmera com um campo de visão e tamanho da tela;
        perspectiveCamera = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Define a posição da câmera (3 afastado em Z da origem);
        perspectiveCamera.position.set(0f, 0f, 3f);
        // Destino que a câmera está apontando (origem);
        perspectiveCamera.lookAt(0f, 0f, 0f);
        // Menor distância que a câmera "captura" (View Distance);
        perspectiveCamera.near = 0.1f;
        // Maior distância que a câmera "captura" (View Distance);
        perspectiveCamera.far = 300f;

        modelBatch = new ModelBatch();
        modelBuilder = new ModelBuilder();

        // Cria um material, definindo seu tamanho (X, Y e Z, material e atributos (posição e normal);
        player = modelBuilder.createSphere(2f, 2f, 2f, 100, 100,
                new Material(ColorAttribute.createDiffuse(Color.BLUE)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        // Cria uma instância de um Objeto 3D;
        modelInstance = new ModelInstance(player, 0f, 0f, 0f);

        enemyTriangle = modelBuilder.createRect(0f, 1f, 2f, 0f, 1f, 2f, 0f, 1f, 2f, 2f, 2f, 2f, 2f, 2f, 2f,
                new Material(ColorAttribute.createDiffuse(Color.RED)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        //modelInstance = new ModelInstance(enemyTriangle, 0f, 0f, 0f);

        enemySquare = modelBuilder.createBox(2f, 2f, 2f,
                new Material(ColorAttribute.createDiffuse(Color.RED)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        //modelInstance = new ModelInstance(enemySquare, 5f, 0f, -5f);

        // Cria o ambiente do jogo;
        environment = new Environment();
        // Define a luz ambiente (cor RGB e intensidade);
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));

        // Determina que o processamento de inputs acontece nessa classe;
        Gdx.input.setInputProcessor(this);
    }

    // INTERFACE SCREEN;
    @Override
    public void render(float delta) {
        Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        if (Gdx.input.isTouched()) {
            System.out.println("Input occurred at x=" + Gdx.input.getX() + ", y=" + Gdx.input.getY());
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            System.out.println("render Tico");
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            perspectiveCamera.rotateAround(this.origin, this.axisY, 1f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            System.out.println("render Bunda");
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            perspectiveCamera.rotateAround(this.origin, this.axisY, -1f);
        }

        perspectiveCamera.update();
        modelBatch.begin(perspectiveCamera);
        modelBatch.render(modelInstance, environment);
        modelBatch.end();
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
    }

    // INTERFACE INPUTPROCESSOR;
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.LEFT) {
            System.out.println("keyDown Tico");
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            perspectiveCamera.rotateAround(this.origin, this.axisY, 1f);
        }
        if (keycode == Input.Keys.RIGHT) {
            System.out.println("keyDown Bunda");
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            perspectiveCamera.rotateAround(this.origin, this.axisY, -1f);
        }
        return true;
    }

    @Override
    public boolean keyUp(int i) {

        return true;
    }

    @Override
    public boolean keyTyped(char c) {

        return true;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {

        return true;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {

        return true;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {

        return true;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {

        return true;
    }

    @Override
    public boolean scrolled(int i) {

        return true;
    }
}
