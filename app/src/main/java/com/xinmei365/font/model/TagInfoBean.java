package com.xinmei365.font.model;

public class TagInfoBean {
    private String name;         //标签内容
    private int notesTagType;    //标签type

    private double x;  //圆心x的在父控件位置 %
    private double y;  //圆心y的在父控件位置 %

    private float width;   //控件宽度
    private float height;  //控件高度

    private int notesTagId;  //标签id

    private boolean isLeft = true;  //圆点是否在左边

    private boolean isCanMove = true;  //标签是否可以移动

    private int index;    //用来记录在编辑标签中的index 位置

    public void setCanMove(boolean canMove) {
        isCanMove = canMove;
    }

    public boolean isCanMove() {
        return isCanMove;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getNotesTagType() {
        return notesTagType;
    }

    public void setNotesTagType(int notesTagType) {
        this.notesTagType = notesTagType;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public int getNotesTagId() {
        return notesTagId;
    }

    public void setNotesTagId(int notesTagId) {
        this.notesTagId = notesTagId;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public void setLeft(boolean left) {
        isLeft = left;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "TagInfoBean{" +
                "name='" + name + '\'' +
                ", notesTagType=" + notesTagType +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", notesTagId=" + notesTagId +
                ", isLeft=" + isLeft +
                ", isCanMove=" + isCanMove +
                ", index=" + index +
                '}';
    }
}
