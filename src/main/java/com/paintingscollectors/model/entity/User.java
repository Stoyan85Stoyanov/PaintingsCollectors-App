package com.paintingscollectors.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.Email;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @Type(type = "uuid-char")
    private UUID id;

    @Length(min = 3, max = 20)
    @Column(unique = true, nullable = false)
    private String username;


    @Column(nullable = false)
    private String password;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @OneToMany( fetch = FetchType.EAGER)
    private Set<Painting> paintings;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Painting> favouritePaintings;

    @ManyToMany( fetch = FetchType.EAGER)
    private Set<Painting> ratedPaintings;

}
