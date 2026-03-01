package com.example.payment.authorizer;

import com.example.payment.domain.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthorizerSelector {

    private final List<Authorizer> authorizers;

    public AuthorizerSelector(List<Authorizer> authorizers) {
        this.authorizers = authorizers;
    }

    public Authorizer select(Transaction transaction) {
        return authorizers.stream()
                .filter(a -> a.supports(transaction))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No authorizer found for transaction: " + transaction.getId()));
    }

    public Authorizer.AuthorizerType selectType(Transaction transaction) {
        return select(transaction).getType();
    }
}
