package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

public class Shapezoid extends ApplicationAdapter implements InputProcessor {
    // PerspectiveCamera é usada para 3D, possui profundidade;
    private PerspectiveCamera camera;
    // Guarda vários modelos, carrega mais rapidamente que modelos individuais, pois realiza apenas uma chamada;
    private ModelBatch modelBatch;
    // Cria os modelis;
    private ModelBuilder modelBuilder;
    // Objeto 3D;
    private Model box;
    // Instância de um Model;
    private ModelInstance modelInstance;
    // Gerencia shaders, luzes, etc.;
    private Environment environment;
    
    public Vector3 origin = new Vector3(0f, 0f, 0f);
    
    public Vector3 axisX = new Vector3(1f, 0f, 0f);
    public Vector3 axisY = new Vector3(0f, 1f, 0f);
    public Vector3 axisZ = new Vector3(0f, 0f, 1f);
    
    @Override
    public void create() {
        
        // Cria a câmera com um campo de visão e tamanho da tela;
        camera = new PerspectiveCamera(75, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Define a posição da câmera (3 afastado em Z da origem);
        camera.position.set(0f, 0f, 3f);
        // Destino que a câmera está apontando (origem);
        camera.lookAt(0f, 0f, 0f);
        // Menor distância que a câmera "captura" (View Distance);
        camera.near = 0.1f;
        // Maior distância que a câmera "captura" (View Distance);
        camera.far = 300f;
        
        modelBatch = new ModelBatch();
        modelBuilder = new ModelBuilder();
        
        // Cria um material, definindo seu tamanho (X, Y e Z, material e atributos (posição e normal);
        box = modelBuilder.createBox(2f, 2f, 2f, 
                new Material(ColorAttribute.createDiffuse(Color.BLUE)), 
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        // Cria uma instância de um Objeto 3D;
        modelInstance = new ModelInstance(box, 0f, 0f, 0f);
        
        // Cria o ambiente do jogo;
        environment = new Environment();
        // Define a luz ambiente (cor RGB e intensidade);
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        
        // Determina que o processamento de inputs acontece nessa classe;
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT|GL20.GL_DEPTH_BUFFER_BIT);
        
        camera.update();
        modelBatch.begin(camera);
        modelBatch.render(modelInstance, environment);
        modelBatch.end();
    }
    
    // 
    private void disposeAll() {
        
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.LEFT) {
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            camera.rotateAround(this.origin, this.axisY, 1f);
        }
        if (keycode == Input.Keys.RIGHT) {
            // Rotaciona a câmera em um ponto, por um eixo, ângulo de rotação;
            camera.rotateAround(this.origin, this.axisY, -1f);
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