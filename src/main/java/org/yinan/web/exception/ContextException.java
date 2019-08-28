package org.yinan.web.exception;

/**
 * @author yinan
 * @date 19-6-11
 */
public class ContextException extends RuntimeException{


    private static final long serialVersionUID = 2883593341954062490L;

    private int code;

    public ContextException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ContextException(int code) {
        super();
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
