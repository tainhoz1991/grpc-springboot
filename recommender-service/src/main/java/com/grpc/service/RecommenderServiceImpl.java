package com.grpc.service;

import com.proto.common.Movie;
import com.proto.recommender.RecommenderRequest;
import com.proto.recommender.RecommenderResponse;
import com.proto.recommender.RecommenderServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@GrpcService
public class RecommenderServiceImpl extends RecommenderServiceGrpc.RecommenderServiceImplBase {

    @Override
    public StreamObserver<RecommenderRequest> getRecommendedMovie(StreamObserver<RecommenderResponse> responseObserver) {
        return new StreamObserver<RecommenderRequest>() {
            List<Movie> movies = new ArrayList<>();
            @Override
            public void onNext(RecommenderRequest recommenderRequest) {
                movies.add(recommenderRequest.getMovie());
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(Status.INTERNAL
                        .withDescription("Internal server error")
                        .asRuntimeException());
            }

            @Override
            public void onCompleted() {
                if (movies.size() > 0){
                    Movie movie = findMovieForRecommendation(movies);
                    responseObserver.onNext(RecommenderResponse.newBuilder().setMovie(movie).build());
                    responseObserver.onCompleted();
                } else {
                    responseObserver
                            .onError(Status.NOT_FOUND
                                    .withDescription("Sorry, found no movies to recommend!").asRuntimeException());
                }

            }
        };
    }
    private Movie findMovieForRecommendation(List<Movie> movies) {
        int random = new SecureRandom().nextInt(movies.size());
        return movies.stream().skip(random).findAny().get();
    }
}
