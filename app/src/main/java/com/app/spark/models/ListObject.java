package com.app.spark.models;

public abstract class ListObject {
        public static final int TYPE_ADD_FRIENDS = 0;
        public static final int TYPE_FRIENDS = 1;

        abstract public int getType();
    }