package cn.bithon.rpc.invocation;

import cn.bithon.rpc.ServiceRegistry;
import cn.bithon.rpc.exception.BadRequestException;
import cn.bithon.rpc.exception.ServiceInvocationException;
import cn.bithon.rpc.message.ServiceRequestMessageIn;
import cn.bithon.rpc.message.ServiceResponseMessageOut;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.Channel;

import java.lang.reflect.InvocationTargetException;

public class ServiceInvocationRunnable implements Runnable {
    private final ServiceRegistry serviceRegistry;
    private final Channel channel;
    private final ServiceRequestMessageIn serviceRequest;

    public ServiceInvocationRunnable(ObjectMapper om,
                                     ServiceRegistry serviceRegistry,
                                     Channel channel,
                                     ServiceRequestMessageIn serviceRequest) {
        this.serviceRegistry = serviceRegistry;
        this.channel = channel;
        this.serviceRequest = serviceRequest;
    }

    public Channel getChannel() {
        return channel;
    }

    public ServiceRequestMessageIn getServiceRequest() {
        return serviceRequest;
    }

    @Override
    public void run() {

        try {
            if (serviceRequest.getServiceName() == null) {
                throw new BadRequestException("serviceName is null");
            }

            if (serviceRequest.getMethodName() == null) {
                throw new BadRequestException("methodName is null");
            }

            ServiceRegistry.RegistryItem serviceProvider = serviceRegistry.findServiceProvider(
                serviceRequest.getServiceName(),
                serviceRequest.getMethodName());
            if (serviceProvider == null) {
                throw new BadRequestException("Can't find service provider %s#%s",
                                              serviceRequest.getServiceName(),
                                              serviceRequest.getMethodName());
            }

            Object[] inputArgs = serviceRequest.getArgs(serviceProvider.getParameterTypes());

            Object ret;
            try {
                ret = serviceProvider.invoke(inputArgs);
            } catch (IllegalAccessException e) {
                throw new ServiceInvocationException("Service[%s#%s] exception: %s",
                                                     serviceRequest.getServiceName(),
                                                     serviceRequest.getMethodName(),
                                                     e.getMessage());
            } catch (InvocationTargetException e) {
                throw new ServiceInvocationException("Service[%s#%s] invocation exception: %s",
                                                     serviceRequest.getServiceName(),
                                                     serviceRequest.getMethodName(),
                                                     e.getTargetException().toString());
            }

            if (!serviceProvider.isOneway()) {
                sendResponse(ServiceResponseMessageOut.builder()
                                                      .serverResponseAt(System.currentTimeMillis())
                                                      .transactionId(serviceRequest.getTransactionId())
                                                      .returning(ret)
                                                      .build());
            }
        } catch (ServiceInvocationException e) {
            sendResponse(ServiceResponseMessageOut.builder()
                                                  .serverResponseAt(System.currentTimeMillis())
                                                  .transactionId(serviceRequest.getTransactionId())
                                                  .exception(e.getMessage())
                                                  .build());
        }
    }

    private void sendResponse(ServiceResponseMessageOut serviceResponse) {
        channel.writeAndFlush(serviceResponse);
    }

}
