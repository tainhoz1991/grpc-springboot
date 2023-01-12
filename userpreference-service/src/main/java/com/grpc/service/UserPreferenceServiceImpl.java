package com.grpc.service;

import com.proto.common.Movie;
import com.proto.userpreference.UserPreferencesRequest;
import com.proto.userpreference.UserPreferencesResponse;
import com.proto.userpreference.UserPreferencesServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.security.SecureRandom;

@GrpcService
public class UserPreferenceServiceImpl extends UserPreferencesServiceGrpc.UserPreferencesServiceImplBase {

    @Override
    public StreamObserver<UserPreferencesRequest> getShortlistedMovies(StreamObserver<UserPreferencesResponse> responseObserver) {
        return new StreamObserver<UserPreferencesRequest>() {
            @Override
            public void onNext(UserPreferencesRequest userPreferencesRequest) {
                if (isEligible(userPreferencesRequest.getMovie())){
                    responseObserver.onNext(UserPreferencesResponse.newBuilder()
                            .setMovie(userPreferencesRequest.getMovie())
                            .build());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Internal server error")
                        .asRuntimeException());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();

            }
        };
    }
    private boolean isEligible(Movie movie) {
        return (new SecureRandom().nextInt() % 4 != 0);
    }

}
