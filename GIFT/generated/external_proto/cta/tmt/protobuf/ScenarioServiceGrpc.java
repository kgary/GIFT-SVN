package cta.tmt.protobuf;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service for handling functions pertaining to the scenario
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.39.0)",
    comments = "Source: proto/scenarioFunctions.proto")
public final class ScenarioServiceGrpc {

  private ScenarioServiceGrpc() {}

  public static final String SERVICE_NAME = "scenario.ScenarioService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest,
      cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse> getSetOutputCoordinateSystemMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetOutputCoordinateSystem",
      requestType = cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest,
      cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse> getSetOutputCoordinateSystemMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest, cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse> getSetOutputCoordinateSystemMethod;
    if ((getSetOutputCoordinateSystemMethod = ScenarioServiceGrpc.getSetOutputCoordinateSystemMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getSetOutputCoordinateSystemMethod = ScenarioServiceGrpc.getSetOutputCoordinateSystemMethod) == null) {
          ScenarioServiceGrpc.getSetOutputCoordinateSystemMethod = getSetOutputCoordinateSystemMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest, cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetOutputCoordinateSystem"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("SetOutputCoordinateSystem"))
              .build();
        }
      }
    }
    return getSetOutputCoordinateSystemMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest,
      cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse> getProvideFeedbackMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ProvideFeedback",
      requestType = cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest,
      cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse> getProvideFeedbackMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest, cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse> getProvideFeedbackMethod;
    if ((getProvideFeedbackMethod = ScenarioServiceGrpc.getProvideFeedbackMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getProvideFeedbackMethod = ScenarioServiceGrpc.getProvideFeedbackMethod) == null) {
          ScenarioServiceGrpc.getProvideFeedbackMethod = getProvideFeedbackMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest, cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ProvideFeedback"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("ProvideFeedback"))
              .build();
        }
      }
    }
    return getProvideFeedbackMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest,
      cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse> getSetWeatherMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetWeather",
      requestType = cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest,
      cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse> getSetWeatherMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest, cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse> getSetWeatherMethod;
    if ((getSetWeatherMethod = ScenarioServiceGrpc.getSetWeatherMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getSetWeatherMethod = ScenarioServiceGrpc.getSetWeatherMethod) == null) {
          ScenarioServiceGrpc.getSetWeatherMethod = getSetWeatherMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest, cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetWeather"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("SetWeather"))
              .build();
        }
      }
    }
    return getSetWeatherMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest,
      cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse> getSetTimeOfDayMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetTimeOfDay",
      requestType = cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest,
      cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse> getSetTimeOfDayMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest, cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse> getSetTimeOfDayMethod;
    if ((getSetTimeOfDayMethod = ScenarioServiceGrpc.getSetTimeOfDayMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getSetTimeOfDayMethod = ScenarioServiceGrpc.getSetTimeOfDayMethod) == null) {
          ScenarioServiceGrpc.getSetTimeOfDayMethod = getSetTimeOfDayMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest, cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetTimeOfDay"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("SetTimeOfDay"))
              .build();
        }
      }
    }
    return getSetTimeOfDayMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse> getGetTimeOfDayMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTimeOfDay",
      requestType = cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse> getGetTimeOfDayMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest, cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse> getGetTimeOfDayMethod;
    if ((getGetTimeOfDayMethod = ScenarioServiceGrpc.getGetTimeOfDayMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getGetTimeOfDayMethod = ScenarioServiceGrpc.getGetTimeOfDayMethod) == null) {
          ScenarioServiceGrpc.getGetTimeOfDayMethod = getGetTimeOfDayMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest, cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTimeOfDay"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("GetTimeOfDay"))
              .build();
        }
      }
    }
    return getGetTimeOfDayMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse> getGetAudioClipsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAudioClips",
      requestType = cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse> getGetAudioClipsMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest, cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse> getGetAudioClipsMethod;
    if ((getGetAudioClipsMethod = ScenarioServiceGrpc.getGetAudioClipsMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getGetAudioClipsMethod = ScenarioServiceGrpc.getGetAudioClipsMethod) == null) {
          ScenarioServiceGrpc.getGetAudioClipsMethod = getGetAudioClipsMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest, cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAudioClips"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("GetAudioClips"))
              .build();
        }
      }
    }
    return getGetAudioClipsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse> getGetHumanPlayersMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetHumanPlayers",
      requestType = cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse> getGetHumanPlayersMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest, cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse> getGetHumanPlayersMethod;
    if ((getGetHumanPlayersMethod = ScenarioServiceGrpc.getGetHumanPlayersMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getGetHumanPlayersMethod = ScenarioServiceGrpc.getGetHumanPlayersMethod) == null) {
          ScenarioServiceGrpc.getGetHumanPlayersMethod = getGetHumanPlayersMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest, cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetHumanPlayers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("GetHumanPlayers"))
              .build();
        }
      }
    }
    return getGetHumanPlayersMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest,
      cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse> getSetScreenFilterMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SetScreenFilter",
      requestType = cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest,
      cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse> getSetScreenFilterMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest, cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse> getSetScreenFilterMethod;
    if ((getSetScreenFilterMethod = ScenarioServiceGrpc.getSetScreenFilterMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getSetScreenFilterMethod = ScenarioServiceGrpc.getSetScreenFilterMethod) == null) {
          ScenarioServiceGrpc.getSetScreenFilterMethod = getSetScreenFilterMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest, cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SetScreenFilter"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("SetScreenFilter"))
              .build();
        }
      }
    }
    return getSetScreenFilterMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse> getGetAgentTransformsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAgentTransforms",
      requestType = cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse> getGetAgentTransformsMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest, cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse> getGetAgentTransformsMethod;
    if ((getGetAgentTransformsMethod = ScenarioServiceGrpc.getGetAgentTransformsMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getGetAgentTransformsMethod = ScenarioServiceGrpc.getGetAgentTransformsMethod) == null) {
          ScenarioServiceGrpc.getGetAgentTransformsMethod = getGetAgentTransformsMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest, cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAgentTransforms"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("GetAgentTransforms"))
              .build();
        }
      }
    }
    return getGetAgentTransformsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse> getGetCombatEventsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCombatEvents",
      requestType = cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest.class,
      responseType = cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest,
      cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse> getGetCombatEventsMethod() {
    io.grpc.MethodDescriptor<cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest, cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse> getGetCombatEventsMethod;
    if ((getGetCombatEventsMethod = ScenarioServiceGrpc.getGetCombatEventsMethod) == null) {
      synchronized (ScenarioServiceGrpc.class) {
        if ((getGetCombatEventsMethod = ScenarioServiceGrpc.getGetCombatEventsMethod) == null) {
          ScenarioServiceGrpc.getGetCombatEventsMethod = getGetCombatEventsMethod =
              io.grpc.MethodDescriptor.<cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest, cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCombatEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new ScenarioServiceMethodDescriptorSupplier("GetCombatEvents"))
              .build();
        }
      }
    }
    return getGetCombatEventsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static ScenarioServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ScenarioServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ScenarioServiceStub>() {
        @java.lang.Override
        public ScenarioServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ScenarioServiceStub(channel, callOptions);
        }
      };
    return ScenarioServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static ScenarioServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ScenarioServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ScenarioServiceBlockingStub>() {
        @java.lang.Override
        public ScenarioServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ScenarioServiceBlockingStub(channel, callOptions);
        }
      };
    return ScenarioServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static ScenarioServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<ScenarioServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<ScenarioServiceFutureStub>() {
        @java.lang.Override
        public ScenarioServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new ScenarioServiceFutureStub(channel, callOptions);
        }
      };
    return ScenarioServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service for handling functions pertaining to the scenario
   * </pre>
   */
  public static abstract class ScenarioServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * define the coordinate systems that will be used
     * </pre>
     */
    public void setOutputCoordinateSystem(cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetOutputCoordinateSystemMethod(), responseObserver);
    }

    /**
     * <pre>
     * TMT controller can inject feedback to one or more trainees
     * </pre>
     */
    public void provideFeedback(cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getProvideFeedbackMethod(), responseObserver);
    }

    /**
     */
    public void setWeather(cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetWeatherMethod(), responseObserver);
    }

    /**
     */
    public void setTimeOfDay(cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetTimeOfDayMethod(), responseObserver);
    }

    /**
     */
    public void getTimeOfDay(cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTimeOfDayMethod(), responseObserver);
    }

    /**
     * <pre>
     * get the audio clips loaded in the simulation
     * </pre>
     */
    public void getAudioClips(cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetAudioClipsMethod(), responseObserver);
    }

    /**
     */
    public void getHumanPlayers(cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetHumanPlayersMethod(), responseObserver);
    }

    /**
     * <pre>
     * Set the vision filter for a player
     * </pre>
     */
    public void setScreenFilter(cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSetScreenFilterMethod(), responseObserver);
    }

    /**
     * <pre>
     * get the positions and locations of agents in the scenario
     * </pre>
     */
    public void getAgentTransforms(cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetAgentTransformsMethod(), responseObserver);
    }

    /**
     * <pre>
     * get combat events such as weapon firing and effective shooting
     * </pre>
     */
    public void getCombatEvents(cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetCombatEventsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSetOutputCoordinateSystemMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest,
                cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse>(
                  this, METHODID_SET_OUTPUT_COORDINATE_SYSTEM)))
          .addMethod(
            getProvideFeedbackMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest,
                cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse>(
                  this, METHODID_PROVIDE_FEEDBACK)))
          .addMethod(
            getSetWeatherMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest,
                cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse>(
                  this, METHODID_SET_WEATHER)))
          .addMethod(
            getSetTimeOfDayMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest,
                cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse>(
                  this, METHODID_SET_TIME_OF_DAY)))
          .addMethod(
            getGetTimeOfDayMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest,
                cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse>(
                  this, METHODID_GET_TIME_OF_DAY)))
          .addMethod(
            getGetAudioClipsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest,
                cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse>(
                  this, METHODID_GET_AUDIO_CLIPS)))
          .addMethod(
            getGetHumanPlayersMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest,
                cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse>(
                  this, METHODID_GET_HUMAN_PLAYERS)))
          .addMethod(
            getSetScreenFilterMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest,
                cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse>(
                  this, METHODID_SET_SCREEN_FILTER)))
          .addMethod(
            getGetAgentTransformsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest,
                cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse>(
                  this, METHODID_GET_AGENT_TRANSFORMS)))
          .addMethod(
            getGetCombatEventsMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest,
                cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse>(
                  this, METHODID_GET_COMBAT_EVENTS)))
          .build();
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to the scenario
   * </pre>
   */
  public static final class ScenarioServiceStub extends io.grpc.stub.AbstractAsyncStub<ScenarioServiceStub> {
    private ScenarioServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ScenarioServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ScenarioServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * define the coordinate systems that will be used
     * </pre>
     */
    public void setOutputCoordinateSystem(cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetOutputCoordinateSystemMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * TMT controller can inject feedback to one or more trainees
     * </pre>
     */
    public void provideFeedback(cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getProvideFeedbackMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setWeather(cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetWeatherMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void setTimeOfDay(cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetTimeOfDayMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTimeOfDay(cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTimeOfDayMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * get the audio clips loaded in the simulation
     * </pre>
     */
    public void getAudioClips(cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetAudioClipsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getHumanPlayers(cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetHumanPlayersMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Set the vision filter for a player
     * </pre>
     */
    public void setScreenFilter(cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSetScreenFilterMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * get the positions and locations of agents in the scenario
     * </pre>
     */
    public void getAgentTransforms(cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetAgentTransformsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * get combat events such as weapon firing and effective shooting
     * </pre>
     */
    public void getCombatEvents(cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest request,
        io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetCombatEventsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to the scenario
   * </pre>
   */
  public static final class ScenarioServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<ScenarioServiceBlockingStub> {
    private ScenarioServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ScenarioServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ScenarioServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * define the coordinate systems that will be used
     * </pre>
     */
    public cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse setOutputCoordinateSystem(cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetOutputCoordinateSystemMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * TMT controller can inject feedback to one or more trainees
     * </pre>
     */
    public cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse provideFeedback(cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getProvideFeedbackMethod(), getCallOptions(), request);
    }

    /**
     */
    public cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse setWeather(cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetWeatherMethod(), getCallOptions(), request);
    }

    /**
     */
    public cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse setTimeOfDay(cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetTimeOfDayMethod(), getCallOptions(), request);
    }

    /**
     */
    public cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse getTimeOfDay(cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTimeOfDayMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * get the audio clips loaded in the simulation
     * </pre>
     */
    public cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse getAudioClips(cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetAudioClipsMethod(), getCallOptions(), request);
    }

    /**
     */
    public cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse getHumanPlayers(cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetHumanPlayersMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Set the vision filter for a player
     * </pre>
     */
    public cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse setScreenFilter(cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSetScreenFilterMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * get the positions and locations of agents in the scenario
     * </pre>
     */
    public cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse getAgentTransforms(cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetAgentTransformsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * get combat events such as weapon firing and effective shooting
     * </pre>
     */
    public cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse getCombatEvents(cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetCombatEventsMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * Service for handling functions pertaining to the scenario
   * </pre>
   */
  public static final class ScenarioServiceFutureStub extends io.grpc.stub.AbstractFutureStub<ScenarioServiceFutureStub> {
    private ScenarioServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected ScenarioServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new ScenarioServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * define the coordinate systems that will be used
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse> setOutputCoordinateSystem(
        cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetOutputCoordinateSystemMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * TMT controller can inject feedback to one or more trainees
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse> provideFeedback(
        cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getProvideFeedbackMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse> setWeather(
        cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetWeatherMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse> setTimeOfDay(
        cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetTimeOfDayMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse> getTimeOfDay(
        cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTimeOfDayMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * get the audio clips loaded in the simulation
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse> getAudioClips(
        cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetAudioClipsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse> getHumanPlayers(
        cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetHumanPlayersMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Set the vision filter for a player
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse> setScreenFilter(
        cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSetScreenFilterMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * get the positions and locations of agents in the scenario
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse> getAgentTransforms(
        cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetAgentTransformsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * get combat events such as weapon firing and effective shooting
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse> getCombatEvents(
        cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetCombatEventsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SET_OUTPUT_COORDINATE_SYSTEM = 0;
  private static final int METHODID_PROVIDE_FEEDBACK = 1;
  private static final int METHODID_SET_WEATHER = 2;
  private static final int METHODID_SET_TIME_OF_DAY = 3;
  private static final int METHODID_GET_TIME_OF_DAY = 4;
  private static final int METHODID_GET_AUDIO_CLIPS = 5;
  private static final int METHODID_GET_HUMAN_PLAYERS = 6;
  private static final int METHODID_SET_SCREEN_FILTER = 7;
  private static final int METHODID_GET_AGENT_TRANSFORMS = 8;
  private static final int METHODID_GET_COMBAT_EVENTS = 9;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final ScenarioServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(ScenarioServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SET_OUTPUT_COORDINATE_SYSTEM:
          serviceImpl.setOutputCoordinateSystem((cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetCoordinateSystemResponse>) responseObserver);
          break;
        case METHODID_PROVIDE_FEEDBACK:
          serviceImpl.provideFeedback((cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.ProvideFeedbackResponse>) responseObserver);
          break;
        case METHODID_SET_WEATHER:
          serviceImpl.setWeather((cta.tmt.protobuf.ScenarioFunctions.SetWeatherRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetWeatherResponse>) responseObserver);
          break;
        case METHODID_SET_TIME_OF_DAY:
          serviceImpl.setTimeOfDay((cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetTimeOfDayResponse>) responseObserver);
          break;
        case METHODID_GET_TIME_OF_DAY:
          serviceImpl.getTimeOfDay((cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetTimeOfDayResponse>) responseObserver);
          break;
        case METHODID_GET_AUDIO_CLIPS:
          serviceImpl.getAudioClips((cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetAudioClipsResponse>) responseObserver);
          break;
        case METHODID_GET_HUMAN_PLAYERS:
          serviceImpl.getHumanPlayers((cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetHumanPlayersResponse>) responseObserver);
          break;
        case METHODID_SET_SCREEN_FILTER:
          serviceImpl.setScreenFilter((cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.SetScreenFilterResponse>) responseObserver);
          break;
        case METHODID_GET_AGENT_TRANSFORMS:
          serviceImpl.getAgentTransforms((cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetAgentTransformsResponse>) responseObserver);
          break;
        case METHODID_GET_COMBAT_EVENTS:
          serviceImpl.getCombatEvents((cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsRequest) request,
              (io.grpc.stub.StreamObserver<cta.tmt.protobuf.ScenarioFunctions.GetCombatEventsResponse>) responseObserver);
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

  private static abstract class ScenarioServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    ScenarioServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return cta.tmt.protobuf.ScenarioFunctions.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("ScenarioService");
    }
  }

  private static final class ScenarioServiceFileDescriptorSupplier
      extends ScenarioServiceBaseDescriptorSupplier {
    ScenarioServiceFileDescriptorSupplier() {}
  }

  private static final class ScenarioServiceMethodDescriptorSupplier
      extends ScenarioServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    ScenarioServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (ScenarioServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new ScenarioServiceFileDescriptorSupplier())
              .addMethod(getSetOutputCoordinateSystemMethod())
              .addMethod(getProvideFeedbackMethod())
              .addMethod(getSetWeatherMethod())
              .addMethod(getSetTimeOfDayMethod())
              .addMethod(getGetTimeOfDayMethod())
              .addMethod(getGetAudioClipsMethod())
              .addMethod(getGetHumanPlayersMethod())
              .addMethod(getSetScreenFilterMethod())
              .addMethod(getGetAgentTransformsMethod())
              .addMethod(getGetCombatEventsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
