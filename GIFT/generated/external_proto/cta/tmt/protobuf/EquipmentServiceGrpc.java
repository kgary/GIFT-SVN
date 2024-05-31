package cta.tmt.protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service for handling functions pertaining to equipment
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.39.0)",
    comments = "Source: proto/equipmentFunctions.proto")
public final class EquipmentServiceGrpc {

  private EquipmentServiceGrpc() {}

  public static final String SERVICE_NAME = "equipment.EquipmentService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest,
      cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse> getIsWeaponSafetyOnMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "IsWeaponSafetyOn",
      requestType = cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest.class,
      responseType = cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest,
      cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse> getIsWeaponSafetyOnMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest, cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse> getIsWeaponSafetyOnMethod;
    if ((getIsWeaponSafetyOnMethod = EquipmentServiceGrpc.getIsWeaponSafetyOnMethod) == null) {
      synchronized (EquipmentServiceGrpc.class) {
        if ((getIsWeaponSafetyOnMethod = EquipmentServiceGrpc.getIsWeaponSafetyOnMethod) == null) {
          EquipmentServiceGrpc.getIsWeaponSafetyOnMethod = getIsWeaponSafetyOnMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest, cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "IsWeaponSafetyOn"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EquipmentServiceMethodDescriptorSupplier("IsWeaponSafetyOn"))
              .build();
        }
      }
    }
    return getIsWeaponSafetyOnMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest,
      cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse> getIsWeaponUpMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "IsWeaponUp",
      requestType = cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest.class,
      responseType = cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest,
      cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse> getIsWeaponUpMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest, cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse> getIsWeaponUpMethod;
    if ((getIsWeaponUpMethod = EquipmentServiceGrpc.getIsWeaponUpMethod) == null) {
      synchronized (EquipmentServiceGrpc.class) {
        if ((getIsWeaponUpMethod = EquipmentServiceGrpc.getIsWeaponUpMethod) == null) {
          EquipmentServiceGrpc.getIsWeaponUpMethod = getIsWeaponUpMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest, cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "IsWeaponUp"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EquipmentServiceMethodDescriptorSupplier("IsWeaponUp"))
              .build();
        }
      }
    }
    return getIsWeaponUpMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest,
      cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse> getGetPrimaryWeaponMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPrimaryWeapon",
      requestType = cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest.class,
      responseType = cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest,
      cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse> getGetPrimaryWeaponMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest, cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse> getGetPrimaryWeaponMethod;
    if ((getGetPrimaryWeaponMethod = EquipmentServiceGrpc.getGetPrimaryWeaponMethod) == null) {
      synchronized (EquipmentServiceGrpc.class) {
        if ((getGetPrimaryWeaponMethod = EquipmentServiceGrpc.getGetPrimaryWeaponMethod) == null) {
          EquipmentServiceGrpc.getGetPrimaryWeaponMethod = getGetPrimaryWeaponMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest, cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPrimaryWeapon"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EquipmentServiceMethodDescriptorSupplier("GetPrimaryWeapon"))
              .build();
        }
      }
    }
    return getGetPrimaryWeaponMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest,
      cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse> getGetWeaponTypeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWeaponType",
      requestType = cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest.class,
      responseType = cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest,
      cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse> getGetWeaponTypeMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest, cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse> getGetWeaponTypeMethod;
    if ((getGetWeaponTypeMethod = EquipmentServiceGrpc.getGetWeaponTypeMethod) == null) {
      synchronized (EquipmentServiceGrpc.class) {
        if ((getGetWeaponTypeMethod = EquipmentServiceGrpc.getGetWeaponTypeMethod) == null) {
          EquipmentServiceGrpc.getGetWeaponTypeMethod = getGetWeaponTypeMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest, cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWeaponType"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EquipmentServiceMethodDescriptorSupplier("GetWeaponType"))
              .build();
        }
      }
    }
    return getGetWeaponTypeMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest,
      cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse> getMalfunctionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Malfunction",
      requestType = cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest.class,
      responseType = cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest,
      cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse> getMalfunctionMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest, cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse> getMalfunctionMethod;
    if ((getMalfunctionMethod = EquipmentServiceGrpc.getMalfunctionMethod) == null) {
      synchronized (EquipmentServiceGrpc.class) {
        if ((getMalfunctionMethod = EquipmentServiceGrpc.getMalfunctionMethod) == null) {
          EquipmentServiceGrpc.getMalfunctionMethod = getMalfunctionMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest, cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Malfunction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EquipmentServiceMethodDescriptorSupplier("Malfunction"))
              .build();
        }
      }
    }
    return getMalfunctionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.RepairRequest,
      cta.tmt.protobuf.EquipmentFunctions.RepairResponse> getRepairMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Repair",
      requestType = cta.tmt.protobuf.EquipmentFunctions.RepairRequest.class,
      responseType = cta.tmt.protobuf.EquipmentFunctions.RepairResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.RepairRequest,
      cta.tmt.protobuf.EquipmentFunctions.RepairResponse> getRepairMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.RepairRequest, cta.tmt.protobuf.EquipmentFunctions.RepairResponse> getRepairMethod;
    if ((getRepairMethod = EquipmentServiceGrpc.getRepairMethod) == null) {
      synchronized (EquipmentServiceGrpc.class) {
        if ((getRepairMethod = EquipmentServiceGrpc.getRepairMethod) == null) {
          EquipmentServiceGrpc.getRepairMethod = getRepairMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EquipmentFunctions.RepairRequest, cta.tmt.protobuf.EquipmentFunctions.RepairResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Repair"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.RepairRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.RepairResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EquipmentServiceMethodDescriptorSupplier("Repair"))
              .build();
        }
      }
    }
    return getRepairMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest,
      cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse> getGetSimulationWeaponsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetSimulationWeapons",
      requestType = cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest.class,
      responseType = cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest,
      cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse> getGetSimulationWeaponsMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest, cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse> getGetSimulationWeaponsMethod;
    if ((getGetSimulationWeaponsMethod = EquipmentServiceGrpc.getGetSimulationWeaponsMethod) == null) {
      synchronized (EquipmentServiceGrpc.class) {
        if ((getGetSimulationWeaponsMethod = EquipmentServiceGrpc.getGetSimulationWeaponsMethod) == null) {
          EquipmentServiceGrpc.getGetSimulationWeaponsMethod = getGetSimulationWeaponsMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest, cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSimulationWeapons"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new EquipmentServiceMethodDescriptorSupplier("GetSimulationWeapons"))
              .build();
        }
      }
    }
    return getGetSimulationWeaponsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static EquipmentServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EquipmentServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EquipmentServiceStub>() {
        @java.lang.Override
        public EquipmentServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EquipmentServiceStub(channel, callOptions);
        }
      };
    return EquipmentServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static EquipmentServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EquipmentServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EquipmentServiceBlockingStub>() {
        @java.lang.Override
        public EquipmentServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EquipmentServiceBlockingStub(channel, callOptions);
        }
      };
    return EquipmentServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static EquipmentServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<EquipmentServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<EquipmentServiceFutureStub>() {
        @java.lang.Override
        public EquipmentServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new EquipmentServiceFutureStub(channel, callOptions);
        }
      };
    return EquipmentServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service for handling functions pertaining to equipment
   * </pre>
   */
  public static abstract class EquipmentServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * tests if the weapon safety is on
     * </pre>
     */
    public void isWeaponSafetyOn(cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getIsWeaponSafetyOnMethod(), responseObserver);
    }

    /**
     * <pre>
     * tests if the weapon is up or down
     * </pre>
     */
    public void isWeaponUp(cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getIsWeaponUpMethod(), responseObserver);
    }

    /**
     * <pre>
     * return the primary weapon of the entity
     * </pre>
     */
    public void getPrimaryWeapon(cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPrimaryWeaponMethod(), responseObserver);
    }

    /**
     * <pre>
     * returns the type of weapon
     * </pre>
     */
    public void getWeaponType(cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWeaponTypeMethod(), responseObserver);
    }

    /**
     * <pre>
     * cause a piece of equipment to malfunction
     * </pre>
     */
    public void malfunction(cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMalfunctionMethod(), responseObserver);
    }

    /**
     * <pre>
     * repair a piece of equipment
     * </pre>
     */
    public void repair(cta.tmt.protobuf.EquipmentFunctions.RepairRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.RepairResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRepairMethod(), responseObserver);
    }

    /**
     * <pre>
     * get all of the weapons with simulation IDs 
     * </pre>
     */
    public void getSimulationWeapons(cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetSimulationWeaponsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getIsWeaponSafetyOnMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest,
                cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse>(
                  this, METHODID_IS_WEAPON_SAFETY_ON)))
          .addMethod(
            getIsWeaponUpMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest,
                cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse>(
                  this, METHODID_IS_WEAPON_UP)))
          .addMethod(
            getGetPrimaryWeaponMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest,
                cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse>(
                  this, METHODID_GET_PRIMARY_WEAPON)))
          .addMethod(
            getGetWeaponTypeMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest,
                cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse>(
                  this, METHODID_GET_WEAPON_TYPE)))
          .addMethod(
            getMalfunctionMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest,
                cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse>(
                  this, METHODID_MALFUNCTION)))
          .addMethod(
            getRepairMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EquipmentFunctions.RepairRequest,
                cta.tmt.protobuf.EquipmentFunctions.RepairResponse>(
                  this, METHODID_REPAIR)))
          .addMethod(
            getGetSimulationWeaponsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest,
                cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse>(
                  this, METHODID_GET_SIMULATION_WEAPONS)))
          .build();
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to equipment
   * </pre>
   */
  public static final class EquipmentServiceStub extends io.grpc.stub.AbstractAsyncStub<EquipmentServiceStub> {
    private EquipmentServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EquipmentServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EquipmentServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * tests if the weapon safety is on
     * </pre>
     */
    public void isWeaponSafetyOn(cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getIsWeaponSafetyOnMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * tests if the weapon is up or down
     * </pre>
     */
    public void isWeaponUp(cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getIsWeaponUpMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * return the primary weapon of the entity
     * </pre>
     */
    public void getPrimaryWeapon(cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPrimaryWeaponMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * returns the type of weapon
     * </pre>
     */
    public void getWeaponType(cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWeaponTypeMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * cause a piece of equipment to malfunction
     * </pre>
     */
    public void malfunction(cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMalfunctionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * repair a piece of equipment
     * </pre>
     */
    public void repair(cta.tmt.protobuf.EquipmentFunctions.RepairRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.RepairResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRepairMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * get all of the weapons with simulation IDs 
     * </pre>
     */
    public void getSimulationWeapons(cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetSimulationWeaponsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to equipment
   * </pre>
   */
  public static final class EquipmentServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<EquipmentServiceBlockingStub> {
    private EquipmentServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EquipmentServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EquipmentServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * tests if the weapon safety is on
     * </pre>
     */
    public cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse isWeaponSafetyOn(cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getIsWeaponSafetyOnMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * tests if the weapon is up or down
     * </pre>
     */
    public cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse isWeaponUp(cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getIsWeaponUpMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * return the primary weapon of the entity
     * </pre>
     */
    public cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse getPrimaryWeapon(cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPrimaryWeaponMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * returns the type of weapon
     * </pre>
     */
    public cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse getWeaponType(cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWeaponTypeMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * cause a piece of equipment to malfunction
     * </pre>
     */
    public cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse malfunction(cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMalfunctionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * repair a piece of equipment
     * </pre>
     */
    public cta.tmt.protobuf.EquipmentFunctions.RepairResponse repair(cta.tmt.protobuf.EquipmentFunctions.RepairRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRepairMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * get all of the weapons with simulation IDs 
     * </pre>
     */
    public cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse getSimulationWeapons(cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetSimulationWeaponsMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to equipment
   * </pre>
   */
  public static final class EquipmentServiceFutureStub extends io.grpc.stub.AbstractFutureStub<EquipmentServiceFutureStub> {
    private EquipmentServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected EquipmentServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new EquipmentServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * tests if the weapon safety is on
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse> isWeaponSafetyOn(
        cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getIsWeaponSafetyOnMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * tests if the weapon is up or down
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse> isWeaponUp(
        cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getIsWeaponUpMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * return the primary weapon of the entity
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse> getPrimaryWeapon(
        cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPrimaryWeaponMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * returns the type of weapon
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse> getWeaponType(
        cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWeaponTypeMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * cause a piece of equipment to malfunction
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse> malfunction(
        cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMalfunctionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * repair a piece of equipment
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EquipmentFunctions.RepairResponse> repair(
        cta.tmt.protobuf.EquipmentFunctions.RepairRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRepairMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * get all of the weapons with simulation IDs 
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse> getSimulationWeapons(
        cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetSimulationWeaponsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_IS_WEAPON_SAFETY_ON = 0;
  private static final int METHODID_IS_WEAPON_UP = 1;
  private static final int METHODID_GET_PRIMARY_WEAPON = 2;
  private static final int METHODID_GET_WEAPON_TYPE = 3;
  private static final int METHODID_MALFUNCTION = 4;
  private static final int METHODID_REPAIR = 5;
  private static final int METHODID_GET_SIMULATION_WEAPONS = 6;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final EquipmentServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(EquipmentServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_IS_WEAPON_SAFETY_ON:
          serviceImpl.isWeaponSafetyOn((cta.tmt.protobuf.EquipmentFunctions.IsWeaponSaftetyOnRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.IsWeaponSafetyOnResponse>) responseObserver);
          break;
        case METHODID_IS_WEAPON_UP:
          serviceImpl.isWeaponUp((cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.IsWeaponUpResponse>) responseObserver);
          break;
        case METHODID_GET_PRIMARY_WEAPON:
          serviceImpl.getPrimaryWeapon((cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.GetPrimaryWeaponResponse>) responseObserver);
          break;
        case METHODID_GET_WEAPON_TYPE:
          serviceImpl.getWeaponType((cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.GetWeaponTypeResponse>) responseObserver);
          break;
        case METHODID_MALFUNCTION:
          serviceImpl.malfunction((cta.tmt.protobuf.EquipmentFunctions.MalfunctionRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.MalfunctionResponse>) responseObserver);
          break;
        case METHODID_REPAIR:
          serviceImpl.repair((cta.tmt.protobuf.EquipmentFunctions.RepairRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.RepairResponse>) responseObserver);
          break;
        case METHODID_GET_SIMULATION_WEAPONS:
          serviceImpl.getSimulationWeapons((cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.EquipmentFunctions.GetSimulationWeaponsResponse>) responseObserver);
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

  private static abstract class EquipmentServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    EquipmentServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return cta.tmt.protobuf.EquipmentFunctions.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("EquipmentService");
    }
  }

  private static final class EquipmentServiceFileDescriptorSupplier
      extends EquipmentServiceBaseDescriptorSupplier {
    EquipmentServiceFileDescriptorSupplier() {}
  }

  private static final class EquipmentServiceMethodDescriptorSupplier
      extends EquipmentServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    EquipmentServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (EquipmentServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new EquipmentServiceFileDescriptorSupplier())
              .addMethod(getIsWeaponSafetyOnMethod())
              .addMethod(getIsWeaponUpMethod())
              .addMethod(getGetPrimaryWeaponMethod())
              .addMethod(getGetWeaponTypeMethod())
              .addMethod(getMalfunctionMethod())
              .addMethod(getRepairMethod())
              .addMethod(getGetSimulationWeaponsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
