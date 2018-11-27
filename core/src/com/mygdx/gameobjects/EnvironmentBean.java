package com.mygdx.gameobjects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;

/**
 * @author guilh
 */
public class EnvironmentBean {

    private ModelInstance groundInstance;
    private ModelInstance wallNorthInstance;
    private ModelInstance wallSouthInstance;
    private ModelInstance wallEastInstance;
    private ModelInstance wallWestInstance;

    private Vector3 groundSize = new Vector3(50f, 1f, 50f);
    private Vector3 wallNorthSouthSize = new Vector3(50f, 10f, 1f);
    private Vector3 wallEastWestSize = new Vector3(1f, 10f, 50f);

    // Bullet
    private btCollisionShape groundShape;
    private btCollisionShape wallNorthSouthShape;
    private btCollisionShape wallEastWestShape;

    private btCollisionObject groundObject;
    private btCollisionObject wallNorthObject;
    private btCollisionObject wallSouthObject;
    private btCollisionObject wallEastObject;
    private btCollisionObject wallWestObject;

    public ModelInstance getGroundInstance() {
        return groundInstance;
    }

    public void setGroundInstance(ModelInstance groundInstance) {
        this.groundInstance = groundInstance;
    }

    public ModelInstance getWallNorthInstance() {
        return wallNorthInstance;
    }

    public void setWallNorthInstance(ModelInstance wallNorthInstance) {
        this.wallNorthInstance = wallNorthInstance;
    }

    public ModelInstance getWallSouthInstance() {
        return wallSouthInstance;
    }

    public void setWallSouthInstance(ModelInstance wallSouthInstance) {
        this.wallSouthInstance = wallSouthInstance;
    }

    public ModelInstance getWallEastInstance() {
        return wallEastInstance;
    }

    public void setWallEastInstance(ModelInstance wallEastInstance) {
        this.wallEastInstance = wallEastInstance;
    }

    public ModelInstance getWallWestInstance() {
        return wallWestInstance;
    }

    public void setWallWestInstance(ModelInstance wallWestInstance) {
        this.wallWestInstance = wallWestInstance;
    }

    public Vector3 getGroundSize() {
        return groundSize;
    }

    public void setGroundSize(Vector3 groundSize) {
        this.groundSize = groundSize;
    }

    public Vector3 getWallNorthSouthSize() {
        return wallNorthSouthSize;
    }

    public void setWallNorthSouthSize(Vector3 wallNorthSouthSize) {
        this.wallNorthSouthSize = wallNorthSouthSize;
    }

    public Vector3 getWallEastWestSize() {
        return wallEastWestSize;
    }

    public void setWallEastWestSize(Vector3 wallEastWestSize) {
        this.wallEastWestSize = wallEastWestSize;
    }

    public btCollisionShape getGroundShape() {
        return groundShape;
    }

    public void setGroundShape(btCollisionShape groundShape) {
        this.groundShape = groundShape;
    }

    public btCollisionShape getWallNorthSouthShape() {
        return wallNorthSouthShape;
    }

    public void setWallNorthSouthShape(btCollisionShape wallNorthSouthShape) {
        this.wallNorthSouthShape = wallNorthSouthShape;
    }

    public btCollisionShape getWallEastWestShape() {
        return wallEastWestShape;
    }

    public void setWallEastWestShape(btCollisionShape wallEastWestShape) {
        this.wallEastWestShape = wallEastWestShape;
    }

    public btCollisionObject getGroundObject() {
        return groundObject;
    }

    public void setGroundObject(btCollisionObject groundObject) {
        this.groundObject = groundObject;
    }

    public btCollisionObject getWallNorthObject() {
        return wallNorthObject;
    }

    public void setWallNorthObject(btCollisionObject wallNorthObject) {
        this.wallNorthObject = wallNorthObject;
    }

    public btCollisionObject getWallSouthObject() {
        return wallSouthObject;
    }

    public void setWallSouthObject(btCollisionObject wallSouthObject) {
        this.wallSouthObject = wallSouthObject;
    }

    public btCollisionObject getWallEastObject() {
        return wallEastObject;
    }

    public void setWallEastObject(btCollisionObject wallEastObject) {
        this.wallEastObject = wallEastObject;
    }

    public btCollisionObject getWallWestObject() {
        return wallWestObject;
    }

    public void setWallWestObject(btCollisionObject wallWestObject) {
        this.wallWestObject = wallWestObject;
    }
}
