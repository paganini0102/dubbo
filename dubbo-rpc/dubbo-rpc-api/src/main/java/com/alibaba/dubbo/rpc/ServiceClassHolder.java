package com.alibaba.dubbo.rpc;

/**
 * TODO this is just a workaround for rest protocol
 *
 * @author lishen
 */
public class ServiceClassHolder {

    private static final ServiceClassHolder INSTANCE = new ServiceClassHolder();

    private final ThreadLocal<Class> holder  = new ThreadLocal<Class>();

    public static ServiceClassHolder getInstance() {
        return INSTANCE;
    }

    private ServiceClassHolder() {
    }

    public Class popServiceClass() {
        Class clazz = holder.get();
        holder.remove();
        return clazz;
    }

    public void pushServiceClass(Class clazz) {
        holder.set(clazz);
    }
}
