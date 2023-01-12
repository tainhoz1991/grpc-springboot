package com.grpc.controller;

import com.grpc.dto.MovieDTO;
import com.grpc.service.MovieService;
import com.proto.common.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1.0/movies")
public class MovieController {
    @Autowired
    private MovieService movieService;

    @GetMapping("/genre/{genre}")
    public ResponseEntity<MovieDTO> getMovie(@PathVariable(name = "genre") String genre){
        MovieDTO movie = movieService.getMovie(genre);
        return ResponseEntity.ok(movie);
    }
}
