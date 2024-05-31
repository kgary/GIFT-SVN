package cta.tmt.protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service for handling functions pertaining to math calculations
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.39.0)",
    comments = "Source: proto/mathFunctions.proto")
public final class MathServiceGrpc {

  private MathServiceGrpc() {}

  public static final String SERVICE_NAME = "math.MathService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest,
      cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse> getIsVisiblePointMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "IsVisiblePoint",
      requestType = cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest.class,
      responseType = cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest,
      cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse> getIsVisiblePointMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest, cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse> getIsVisiblePointMethod;
    if ((getIsVisiblePointMethod = MathServiceGrpc.getIsVisiblePointMethod) == null) {
      synchronized (MathServiceGrpc.class) {
        if ((getIsVisiblePointMethod = MathServiceGrpc.getIsVisiblePointMethod) == null) {
          MathServiceGrpc.getIsVisiblePointMethod = getIsVisiblePointMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest, cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "IsVisiblePoint"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MathServiceMethodDescriptorSupplier("IsVisiblePoint"))
              .build();
        }
      }
    }
    return getIsVisiblePointMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest,
      cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse> getIsVisibleEntityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "IsVisibleEntity",
      requestType = cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest.class,
      responseType = cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest,
      cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse> getIsVisibleEntityMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest, cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse> getIsVisibleEntityMethod;
    if ((getIsVisibleEntityMethod = MathServiceGrpc.getIsVisibleEntityMethod) == null) {
      synchronized (MathServiceGrpc.class) {
        if ((getIsVisibleEntityMethod = MathServiceGrpc.getIsVisibleEntityMethod) == null) {
          MathServiceGrpc.getIsVisibleEntityMethod = getIsVisibleEntityMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest, cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "IsVisibleEntity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse.getDefaultInstance()))
              .setSchemaDescriptor(new MathServiceMethodDescriptorSupplier("IsVisibleEntity"))
              .build();
        }
      }
    }
    return getIsVisibleEntityMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static MathServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MathServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MathServiceStub>() {
        @java.lang.Override
        public MathServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MathServiceStub(channel, callOptions);
        }
      };
    return MathServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static MathServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MathServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MathServiceBlockingStub>() {
        @java.lang.Override
        public MathServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MathServiceBlockingStub(channel, callOptions);
        }
      };
    return MathServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static MathServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MathServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<MathServiceFutureStub>() {
        @java.lang.Override
        public MathServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new MathServiceFutureStub(channel, callOptions);
        }
      };
    return MathServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service for handling functions pertaining to math calculations
   * </pre>
   */
  public static abstract class MathServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * tests if a point is visible from an entity's location
     * </pre>
     */
    public void isVisiblePoint(cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getIsVisiblePointMethod(), responseObserver);
    }

    /**
     * <pre>
     * tests if an entity is viewable from an entity's location
     * </pre>
     */
    public void isVisibleEntity(cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getIsVisibleEntityMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getIsVisiblePointMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest,
                cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse>(
                  this, METHODID_IS_VISIBLE_POINT)))
          .addMethod(
            getIsVisibleEntityMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest,
                cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse>(
                  this, METHODID_IS_VISIBLE_ENTITY)))
          .build();
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to math calculations
   * </pre>
   */
  public static final class MathServiceStub extends io.grpc.stub.AbstractAsyncStub<MathServiceStub> {
    private MathServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MathServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MathServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * tests if a point is visible from an entity's location
     * </pre>
     */
    public void isVisiblePoint(cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getIsVisiblePointMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * tests if an entity is viewable from an entity's location
     * </pre>
     */
    public void isVisibleEntity(cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getIsVisibleEntityMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to math calculations
   * </pre>
   */
  public static final class MathServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<MathServiceBlockingStub> {
    private MathServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MathServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MathServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * tests if a point is visible from an entity's location
     * </pre>
     */
    public cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse isVisiblePoint(cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getIsVisiblePointMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * tests if an entity is viewable from an entity's location
     * </pre>
     */
    public cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse isVisibleEntity(cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getIsVisibleEntityMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to math calculations
   * </pre>
   */
  public static final class MathServiceFutureStub extends io.grpc.stub.AbstractFutureStub<MathServiceFutureStub> {
    private MathServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MathServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MathServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * tests if a point is visible from an entity's location
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse> isVisiblePoint(
        cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getIsVisiblePointMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * tests if an entity is viewable from an entity's location
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse> isVisibleEntity(
        cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getIsVisibleEntityMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_IS_VISIBLE_POINT = 0;
  private static final int METHODID_IS_VISIBLE_ENTITY = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final MathServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(MathServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_IS_VISIBLE_POINT:
          serviceImpl.isVisiblePoint((cta.tmt.protobuf.MathFunctions.IsVisiblePointRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.MathFunctions.IsVisiblePointResponse>) responseObserver);
          break;
        case METHODID_IS_VISIBLE_ENTITY:
          serviceImpl.isVisibleEntity((cta.tmt.protobuf.MathFunctions.IsVisibleEntityRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.MathFunctions.IsVisibleEntityResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class MathServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MathServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return cta.tmt.protobuf.MathFunctions.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MathService");
    }
  }

  private static final class MathServiceFileDescriptorSupplier
      extends MathServiceBaseDescriptorSupplier {
    MathServiceFileDescriptorSupplier() {}
  }

  private static final class MathServiceMethodDescriptorSupplier
      extends MathServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    MathServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MathServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new MathServiceFileDescriptorSupplier())
              .addMethod(getIsVisiblePointMethod())
              .addMethod(getIsVisibleEntityMethod())
              .build();
        }
      }
    }
    return result;
  }
}
