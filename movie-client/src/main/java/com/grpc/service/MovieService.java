package com.grpc.service;

import com.grpc.controller.MovieController;
import com.grpc.dto.MovieDTO;
import com.grpc.mapper.MovieMapper;
import com.proto.common.Genre;
import com.proto.common.Movie;
import com.proto.moviecontroller.MovieControllerServiceGrpc;
import com.proto.moviecontroller.MovieRequest;
import com.proto.moviecontroller.MovieResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovieService {
    @Autowired
    private MovieMapper mapper;
    @GrpcClient("movie-controller-server")
    MovieControllerServiceGrpc.MovieControllerServiceBlockingStub synchronousClient;

    public MovieDTO getMovie(String genre) {
        MovieRequest movieRequest = MovieRequest.newBuilder().setGenre(getGenre(genre)).setUserid("tai_than").build();
        MovieResponse movieResponse = synchronousClient.getMovie(movieRequest);

        return mapper.toDTO(movieResponse.getMovie());
    }

    public Genre getGenre(String genre){
        switch (genre.toUpperCase()){
            case "COMEDY":
                return Genre.COMEDY;
            case "ACTION":
                return Genre.ACTION;
            case "DRAMA":
                return Genre.DRAMA;
            case "THRILLER":
                return Genre.THRILLER;
            default: return Genre.UNRECOGNIZED;

        }
    }
}
