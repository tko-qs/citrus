package org.citrusframework.citrus.db.server.util;

/**
 * @author Christoph Deppisch
 */
public class DeamonThread extends Thread {

    public DeamonThread(Runnable runnable) {
        super(runnable);
        setDaemon(true);
    }
}
