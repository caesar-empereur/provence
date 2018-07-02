package com.app.core;

/**
 * Created by leon on 2018/4/19.
 */
public enum InitializingAction {
                                
    NONE("none"),

    CREATE_ONLY("create_only"),

    DROP_CREATE("drop-and-create"),

    DROP("drop"),

    UPDATE("update");
    
    private final String hbaseddlName;
    
    InitializingAction(String hbaseddlName) {
        this.hbaseddlName = hbaseddlName;
    }
}
