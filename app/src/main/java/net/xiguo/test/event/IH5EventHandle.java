package net.xiguo.test.event;

/**
 * Created by army on 2017/3/20.
 */

public interface IH5EventHandle {
    void handle();
    void handle(String action);
}
