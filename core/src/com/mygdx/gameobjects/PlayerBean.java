package com.mygdx.gameobjects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

/**
 * @author guilh
 */
public class PlayerBean {

    private ModelInstance playerInstance;

    private Vector3 playerSize = new Vector3(2f, 2f, 2f);

    // Bullet
    private btCollisionShape playerShape;
    private btCollisionObject playerObject;

    public ModelInstance getPlayerInstance() {
        return playerInstance;
    }

    public void setPlayerInstance(ModelInstance playerInstance) {
        this.playerInstance = playerInstance;
    }

    public Vector3 getPlayerSize() {
        return playerSize;
    }

    public void setPlayerSize(Vector3 playerSize) {
        this.playerSize = playerSize;
    }

    public btCollisionShape getPlayerShape() {
        return playerShape;
    }

    public void setPlayerShape(btCollisionShape playerShape) {
        this.playerShape = playerShape;
    }

    public btCollisionObject getPlayerObject() {
        return playerObject;
    }

    public void setPlayerObject(btCollisionObject playerObject) {
        this.playerObject = playerObject;
    }
}