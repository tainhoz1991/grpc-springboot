package com.grpc.mapper;

import com.grpc.dto.MovieDTO;
import com.proto.common.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

    public MovieDTO toDTO(Movie movie){
        MovieDTO dto = new MovieDTO();
        dto.setTitle(movie.getTitle());
        dto.setRating(movie.getRating());
        dto.setDescription(movie.getDescription());
        dto.setGenre(movie.getGenre().toString());
        return dto;
    }
}
