package com.grpc.controller.service;

import com.google.protobuf.Descriptors;
import com.grpc.dto.AuthorTemp;
import com.schema.Author;
import com.schema.Book;
import com.schema.BookAuthorServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class BookAuthorClientService {

    @GrpcClient("grpc-book-author-service")
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient;

    @GrpcClient("grpc-book-author-service")
    BookAuthorServiceGrpc.BookAuthorServiceStub asynchronousClient;

    public Map<Descriptors.FieldDescriptor, Object> getAuthor(int authorId){
        Author authorRequest = Author.newBuilder().setAuthorId(authorId).build();
        Author authorResponse = synchronousClient.getAuthor(authorRequest);
        return authorResponse.getAllFields();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthor(int authorId) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Author authorRequest = Author.newBuilder().setAuthorId(authorId).build();
        List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        asynchronousClient.getBooksByAuthor(authorRequest, new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        boolean await = latch.await(1, TimeUnit.MINUTES);

        return await ? response : Collections.EMPTY_LIST;
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getExpensiveBook() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        StreamObserver<Book> responseObserver = asynchronousClient.getExpensiveBook(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });
        AuthorTemp.getBooksFromTempDb().forEach(responseObserver::onNext);
        responseObserver.onCompleted();

        boolean await = latch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.EMPTY_LIST;
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthorGender(String gender) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        // create connection to gRPC server by invoke getBooksByAuthorGender method and wait receive data from server
        StreamObserver<Book> streamObserver = asynchronousClient.getBooksByAuthorGender(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                // implement handle each fame(data) from stream that server send back.
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        // after create connection to gRPC server and send message to server by invoke onNext method
        AuthorTemp.getAuthorsFromTempDb().stream()
                .filter(author -> author.getGender().equalsIgnoreCase(gender))
                .forEach(author -> streamObserver.onNext(Book.newBuilder().setAuthorId(author.getAuthorId()).build()));
        streamObserver.onCompleted();
        boolean await = latch.await(1, TimeUnit.MINUTES);

        return await ? response : Collections.EMPTY_LIST;
    }
}
