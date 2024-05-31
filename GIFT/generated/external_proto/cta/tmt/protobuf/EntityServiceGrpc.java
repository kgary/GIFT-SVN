package cta.tmt.protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service for handling functions pertaining to entities
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.39.0)",
    comments = "Source: proto/entityFunctions.proto")
public final class EntityServiceGrpc {

  private EntityServiceGrpc() {}

  public static final String SERVICE_NAME = "entity.EntityService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest,
      cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse> getGetDisTypeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDisType",
      requestType = cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest.class,
      responseType = cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest,
      cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse> getGetDisTypeMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest, cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse> getGetDisTypeMethod;
    if ((getGetDisTypeMethod = EntityServiceGrpc.getGetDisTypeMethod) == null) {
      synchronized (EntityServiceGrpc.class) {
        if ((getGetDisTypeMethod = EntityServiceGrpc.getGetDisTypeMethod) == null) {
          EntityServiceGrpc.getGetDisTypeMethod = getGetDisTypeMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest, cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDisType"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EntityServiceMethodDescriptorSupplier("GetDisType"))
              .build();
        }
      }
    }
    return getGetDisTypeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.CreateEntityRequest,
      cta.tmt.protobuf.EntityFunctions.CreateEntityResponse> getCreateEntityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateEntity",
      requestType = cta.tmt.protobuf.EntityFunctions.CreateEntityRequest.class,
      responseType = cta.tmt.protobuf.EntityFunctions.CreateEntityResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.CreateEntityRequest,
      cta.tmt.protobuf.EntityFunctions.CreateEntityResponse> getCreateEntityMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.CreateEntityRequest, cta.tmt.protobuf.EntityFunctions.CreateEntityResponse> getCreateEntityMethod;
    if ((getCreateEntityMethod = EntityServiceGrpc.getCreateEntityMethod) == null) {
      synchronized (EntityServiceGrpc.class) {
        if ((getCreateEntityMethod = EntityServiceGrpc.getCreateEntityMethod) == null) {
          EntityServiceGrpc.getCreateEntityMethod = getCreateEntityMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EntityFunctions.CreateEntityRequest, cta.tmt.protobuf.EntityFunctions.CreateEntityResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CreateEntity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.CreateEntityRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.CreateEntityResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EntityServiceMethodDescriptorSupplier("CreateEntity"))
              .build();
        }
      }
    }
    return getCreateEntityMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.SetPositionRequest,
      cta.tmt.protobuf.EntityFunctions.SetPositionResponse> getSetPositionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetPosition",
      requestType = cta.tmt.protobuf.EntityFunctions.SetPositionRequest.class,
      responseType = cta.tmt.protobuf.EntityFunctions.SetPositionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.SetPositionRequest,
      cta.tmt.protobuf.EntityFunctions.SetPositionResponse> getSetPositionMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.SetPositionRequest, cta.tmt.protobuf.EntityFunctions.SetPositionResponse> getSetPositionMethod;
    if ((getSetPositionMethod = EntityServiceGrpc.getSetPositionMethod) == null) {
      synchronized (EntityServiceGrpc.class) {
        if ((getSetPositionMethod = EntityServiceGrpc.getSetPositionMethod) == null) {
          EntityServiceGrpc.getSetPositionMethod = getSetPositionMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EntityFunctions.SetPositionRequest, cta.tmt.protobuf.EntityFunctions.SetPositionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetPosition"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.SetPositionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.SetPositionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EntityServiceMethodDescriptorSupplier("SetPosition"))
              .build();
        }
      }
    }
    return getSetPositionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.SetHighlightRequest,
      cta.tmt.protobuf.EntityFunctions.SetHighlightResponse> getSetHighlightMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetHighlight",
      requestType = cta.tmt.protobuf.EntityFunctions.SetHighlightRequest.class,
      responseType = cta.tmt.protobuf.EntityFunctions.SetHighlightResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.SetHighlightRequest,
      cta.tmt.protobuf.EntityFunctions.SetHighlightResponse> getSetHighlightMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.SetHighlightRequest, cta.tmt.protobuf.EntityFunctions.SetHighlightResponse> getSetHighlightMethod;
    if ((getSetHighlightMethod = EntityServiceGrpc.getSetHighlightMethod) == null) {
      synchronized (EntityServiceGrpc.class) {
        if ((getSetHighlightMethod = EntityServiceGrpc.getSetHighlightMethod) == null) {
          EntityServiceGrpc.getSetHighlightMethod = getSetHighlightMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EntityFunctions.SetHighlightRequest, cta.tmt.protobuf.EntityFunctions.SetHighlightResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetHighlight"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.SetHighlightRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.SetHighlightResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EntityServiceMethodDescriptorSupplier("SetHighlight"))
              .build();
        }
      }
    }
    return getSetHighlightMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest,
      cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse> getGetSimulationEntitiesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSimulationEntities",
      requestType = cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest.class,
      responseType = cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest,
      cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse> getGetSimulationEntitiesMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest, cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse> getGetSimulationEntitiesMethod;
    if ((getGetSimulationEntitiesMethod = EntityServiceGrpc.getGetSimulationEntitiesMethod) == null) {
      synchronized (EntityServiceGrpc.class) {
        if ((getGetSimulationEntitiesMethod = EntityServiceGrpc.getGetSimulationEntitiesMethod) == null) {
          EntityServiceGrpc.getGetSimulationEntitiesMethod = getGetSimulationEntitiesMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest, cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSimulationEntities"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EntityServiceMethodDescriptorSupplier("GetSimulationEntities"))
              .build();
        }
      }
    }
    return getGetSimulationEntitiesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EntityServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EntityServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EntityServiceStub>() {
        @java.lang.Override
        public EntityServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EntityServiceStub(channel, callOptions);
        }
      };
    return EntityServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EntityServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EntityServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EntityServiceBlockingStub>() {
        @java.lang.Override
        public EntityServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EntityServiceBlockingStub(channel, callOptions);
        }
      };
    return EntityServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EntityServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EntityServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EntityServiceFutureStub>() {
        @java.lang.Override
        public EntityServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EntityServiceFutureStub(channel, callOptions);
        }
      };
    return EntityServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service for handling functions pertaining to entities
   * </pre>
   */
  public static abstract class EntityServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * get the DIS type of entity
     * </pre>
     */
    public void getDisType(cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetDisTypeMethod(), responseObserver);
    }

    /**
     * <pre>
     * Create an entity in the simulation
     * </pre>
     */
    public void createEntity(cta.tmt.protobuf.EntityFunctions.CreateEntityRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.CreateEntityResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCreateEntityMethod(), responseObserver);
    }

    /**
     * <pre>
     * Sets the position of the entity
     * </pre>
     */
    public void setPosition(cta.tmt.protobuf.EntityFunctions.SetPositionRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.SetPositionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetPositionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Sets the entity as highlighted or not
     * </pre>
     */
    public void setHighlight(cta.tmt.protobuf.EntityFunctions.SetHighlightRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.SetHighlightResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetHighlightMethod(), responseObserver);
    }

    /**
     * <pre>
     * get the entities in the simulation that have a simulation ID
     * </pre>
     */
    public void getSimulationEntities(cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetSimulationEntitiesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetDisTypeMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest,
                cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse>(
                  this, METHODID_GET_DIS_TYPE)))
          .addMethod(
            getCreateEntityMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EntityFunctions.CreateEntityRequest,
                cta.tmt.protobuf.EntityFunctions.CreateEntityResponse>(
                  this, METHODID_CREATE_ENTITY)))
          .addMethod(
            getSetPositionMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EntityFunctions.SetPositionRequest,
                cta.tmt.protobuf.EntityFunctions.SetPositionResponse>(
                  this, METHODID_SET_POSITION)))
          .addMethod(
            getSetHighlightMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EntityFunctions.SetHighlightRequest,
                cta.tmt.protobuf.EntityFunctions.SetHighlightResponse>(
                  this, METHODID_SET_HIGHLIGHT)))
          .addMethod(
            getGetSimulationEntitiesMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest,
                cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse>(
                  this, METHODID_GET_SIMULATION_ENTITIES)))
          .build();
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to entities
   * </pre>
   */
  public static final class EntityServiceStub extends io.grpc.stub.AbstractAsyncStub<EntityServiceStub> {
    private EntityServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EntityServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EntityServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * get the DIS type of entity
     * </pre>
     */
    public void getDisType(cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetDisTypeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Create an entity in the simulation
     * </pre>
     */
    public void createEntity(cta.tmt.protobuf.EntityFunctions.CreateEntityRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.CreateEntityResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCreateEntityMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Sets the position of the entity
     * </pre>
     */
    public void setPosition(cta.tmt.protobuf.EntityFunctions.SetPositionRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.SetPositionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetPositionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Sets the entity as highlighted or not
     * </pre>
     */
    public void setHighlight(cta.tmt.protobuf.EntityFunctions.SetHighlightRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.SetHighlightResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetHighlightMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * get the entities in the simulation that have a simulation ID
     * </pre>
     */
    public void getSimulationEntities(cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetSimulationEntitiesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to entities
   * </pre>
   */
  public static final class EntityServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<EntityServiceBlockingStub> {
    private EntityServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EntityServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EntityServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * get the DIS type of entity
     * </pre>
     */
    public cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse getDisType(cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetDisTypeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Create an entity in the simulation
     * </pre>
     */
    public cta.tmt.protobuf.EntityFunctions.CreateEntityResponse createEntity(cta.tmt.protobuf.EntityFunctions.CreateEntityRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCreateEntityMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Sets the position of the entity
     * </pre>
     */
    public cta.tmt.protobuf.EntityFunctions.SetPositionResponse setPosition(cta.tmt.protobuf.EntityFunctions.SetPositionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetPositionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Sets the entity as highlighted or not
     * </pre>
     */
    public cta.tmt.protobuf.EntityFunctions.SetHighlightResponse setHighlight(cta.tmt.protobuf.EntityFunctions.SetHighlightRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetHighlightMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * get the entities in the simulation that have a simulation ID
     * </pre>
     */
    public cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse getSimulationEntities(cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetSimulationEntitiesMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to entities
   * </pre>
   */
  public static final class EntityServiceFutureStub extends io.grpc.stub.AbstractFutureStub<EntityServiceFutureStub> {
    private EntityServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EntityServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EntityServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * get the DIS type of entity
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse> getDisType(
        cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetDisTypeMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Create an entity in the simulation
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EntityFunctions.CreateEntityResponse> createEntity(
        cta.tmt.protobuf.EntityFunctions.CreateEntityRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCreateEntityMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Sets the position of the entity
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EntityFunctions.SetPositionResponse> setPosition(
        cta.tmt.protobuf.EntityFunctions.SetPositionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetPositionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Sets the entity as highlighted or not
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EntityFunctions.SetHighlightResponse> setHighlight(
        cta.tmt.protobuf.EntityFunctions.SetHighlightRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetHighlightMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * get the entities in the simulation that have a simulation ID
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse> getSimulationEntities(
        cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetSimulationEntitiesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_DIS_TYPE = 0;
  private static final int METHODID_CREATE_ENTITY = 1;
  private static final int METHODID_SET_POSITION = 2;
  private static final int METHODID_SET_HIGHLIGHT = 3;
  private static final int METHODID_GET_SIMULATION_ENTITIES = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final EntityServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(EntityServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_DIS_TYPE:
          serviceImpl.getDisType((cta.tmt.protobuf.EntityFunctions.GetDisTypeRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.GetDisTypeResponse>) responseObserver);
          break;
        case METHODID_CREATE_ENTITY:
          serviceImpl.createEntity((cta.tmt.protobuf.EntityFunctions.CreateEntityRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.CreateEntityResponse>) responseObserver);
          break;
        case METHODID_SET_POSITION:
          serviceImpl.setPosition((cta.tmt.protobuf.EntityFunctions.SetPositionRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.SetPositionResponse>) responseObserver);
          break;
        case METHODID_SET_HIGHLIGHT:
          serviceImpl.setHighlight((cta.tmt.protobuf.EntityFunctions.SetHighlightRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.SetHighlightResponse>) responseObserver);
          break;
        case METHODID_GET_SIMULATION_ENTITIES:
          serviceImpl.getSimulationEntities((cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EntityFunctions.GetSimulationEntitiesResponse>) responseObserver);
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

  private static abstract class EntityServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EntityServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return cta.tmt.protobuf.EntityFunctions.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("EntityService");
    }
  }

  private static final class EntityServiceFileDescriptorSupplier
      extends EntityServiceBaseDescriptorSupplier {
    EntityServiceFileDescriptorSupplier() {}
  }

  private static final class EntityServiceMethodDescriptorSupplier
      extends EntityServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    EntityServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (EntityServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EntityServiceFileDescriptorSupplier())
              .addMethod(getGetDisTypeMethod())
              .addMethod(getCreateEntityMethod())
              .addMethod(getSetPositionMethod())
              .addMethod(getSetHighlightMethod())
              .addMethod(getGetSimulationEntitiesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
