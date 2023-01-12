package com.grpc;

import com.grpc.dto.AuthorTemp;
import com.schema.Author;
import com.schema.Book;
import com.schema.BookAuthorServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@GrpcService
public class BookAuthorServerService extends BookAuthorServiceGrpc.BookAuthorServiceImplBase {

    @Override
    public void getAuthor(Author request, StreamObserver<Author> responseObserver) {
        AuthorTemp.getAuthorsFromTempDb().stream()
                .filter(author -> Objects.equals(author.getAuthorId(), request.getAuthorId()))
                .findFirst()
                .ifPresent(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public void getBooksByAuthor(Author request, StreamObserver<Book> responseObserver) {
        AuthorTemp.getBooksFromTempDb().stream()
                .filter(book -> Objects.equals(book.getAuthorId(), request.getAuthorId()))
                .forEach(responseObserver::onNext);
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Book> getExpensiveBook(StreamObserver<Book> responseObserver) {
        return new StreamObserver<Book>() {
            Book expensive;
            float price = 0;
            @Override
            public void onNext(Book book) {
                if (book.getPrice() > price){
                    expensive = book;
                    price = book.getPrice();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(expensive);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<Book> getBooksByAuthorGender(StreamObserver<Book> responseObserver) {
        return new StreamObserver<Book>() {
            List<Book> bookList = new ArrayList<>();
            @Override
            public void onNext(Book book) {
                AuthorTemp.getBooksFromTempDb().stream()
                        .filter(b -> Objects.equals(b.getAuthorId(), book.getAuthorId()))
                        .forEach(bookList::add);
            }

            @Override
            public void onError(Throwable throwable) {
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                bookList.forEach(responseObserver::onNext);
                responseObserver.onCompleted();
            }
        };
    }
}
