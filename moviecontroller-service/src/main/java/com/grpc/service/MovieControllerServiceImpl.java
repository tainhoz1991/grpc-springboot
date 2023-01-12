package com.grpc.service;

import com.proto.moviecontroller.MovieControllerServiceGrpc;
import com.proto.moviecontroller.MovieRequest;
import com.proto.moviecontroller.MovieResponse;
import com.proto.moviestore.MovieStoreRequest;
import com.proto.moviestore.MovieStoreServiceGrpc;
import com.proto.recommender.RecommenderRequest;
import com.proto.recommender.RecommenderResponse;
import com.proto.recommender.RecommenderServiceGrpc;
import com.proto.userpreference.UserPreferencesRequest;
import com.proto.userpreference.UserPreferencesResponse;
import com.proto.userpreference.UserPreferencesServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@GrpcService
public class MovieControllerServiceImpl extends MovieControllerServiceGrpc.MovieControllerServiceImplBase {
    public static final int MOVIES_STORE_SERVICE_PORT = 8080;
    public static final int USER_PREFERENCES_SERVICE_PORT = 8082;
    public static final int RECOMMENDER_SERVICE_PORT = 8081;

    @Override
    public void getMovie(MovieRequest request, StreamObserver<MovieResponse> responseObserver) {
        String userId = request.getUserid();
        MovieStoreServiceGrpc.MovieStoreServiceBlockingStub movieStoreClient = MovieStoreServiceGrpc.newBlockingStub(getChannel(MOVIES_STORE_SERVICE_PORT));
        UserPreferencesServiceGrpc.UserPreferencesServiceStub userPreferencesClient = UserPreferencesServiceGrpc.newStub(getChannel(USER_PREFERENCES_SERVICE_PORT));
        RecommenderServiceGrpc.RecommenderServiceStub recommenderClient = RecommenderServiceGrpc.newStub(getChannel(RECOMMENDER_SERVICE_PORT));

        CountDownLatch latch = new CountDownLatch(1);

        // the first of all, create the connection from UsePreferenceService to RecommenderService to recommend one of the movies
        // and send back to client by responseObserver
        StreamObserver<RecommenderRequest> streamRecommender = recommenderClient.getRecommendedMovie(new StreamObserver<RecommenderResponse>() {
            public void onNext(RecommenderResponse value) {
                responseObserver.onNext(MovieResponse.newBuilder().setMovie(value.getMovie()).build());
                System.out.println("Recommended movie " + value.getMovie());
            }
            public void onError(Throwable t) {
                responseObserver.onError(t);
                latch.countDown();
            }
            public void onCompleted() {
                responseObserver.onCompleted();
                latch.countDown();
            }
        });

        // send stream movies to RecommenderService to get one movie that is recommended
        StreamObserver<UserPreferencesRequest> streamUserPreference = userPreferencesClient.getShortlistedMovies(new StreamObserver<UserPreferencesResponse>() {
            public void onNext(UserPreferencesResponse value) {
                streamRecommender.onNext(RecommenderRequest.newBuilder().setUserid(userId).setMovie(value.getMovie()).build());
            }
            public void onError(Throwable t) {
                streamRecommender.onError(t);
                latch.countDown();
            }
            @Override
            public void onCompleted() {
                streamRecommender.onCompleted();
            }
        });

        // get list movies from MovieStoreService and send stream movies to UserPreferenceService
        movieStoreClient.getMovies(MovieStoreRequest.newBuilder().setGenre(request.getGenre()).build()).forEachRemaining(response -> {
            streamUserPreference.onNext(UserPreferencesRequest.newBuilder().setUserid(userId).setMovie(response.getMovie()).build());
        });

        streamUserPreference.onCompleted();

        try {
            latch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ManagedChannel getChannel(int port) {
        return ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build();
    }
}
