package com.grpc.controller.controller;

import com.google.protobuf.Descriptors;
import com.grpc.controller.service.BookAuthorClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/book-author")
public class BookAuthorController {
    @Autowired
    private BookAuthorClientService bookAuthorClientService;

    @GetMapping("/{id}")
    public Map<Descriptors.FieldDescriptor, Object> getAuthor(@PathVariable(name = "id") String author){
        return bookAuthorClientService.getAuthor(Integer.parseInt(author));
    }

    @GetMapping("/books/{id}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthor(@PathVariable(name = "id") String author) throws InterruptedException {
        return bookAuthorClientService.getBooksByAuthor(Integer.parseInt(author));
    }

    @GetMapping("/expensive-book")
    public List<Map<Descriptors.FieldDescriptor, Object>> getExpensiveBook() throws InterruptedException {
        return bookAuthorClientService.getExpensiveBook();
    }

    @GetMapping("/gender/{gender}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthorGender(@PathVariable(name = "gender") String author) throws InterruptedException {
        return bookAuthorClientService.getBooksByAuthorGender(author);
    }
}
