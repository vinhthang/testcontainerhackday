package com.github.vinhthang.boot.admin;

import jakarta.annotation.Generated;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.AUTO;


@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = AUTO)
    private long id;

    @Column(name = "user_name")
    private String userName;

    @Column
    @Nonnull
    private LocalDateTime created;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
