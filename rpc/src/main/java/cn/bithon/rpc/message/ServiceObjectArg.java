package cn.bithon.rpc.message;

public class ServiceObjectArg {
    private long objectId;

    public ServiceObjectArg() {

    }
    public ServiceObjectArg(long objectId) {
        this.objectId = objectId;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }
}
