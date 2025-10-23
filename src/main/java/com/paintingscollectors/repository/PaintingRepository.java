package com.paintingscollectors.repository;

import com.paintingscollectors.model.entity.Painting;
import com.paintingscollectors.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaintingRepository extends JpaRepository<Painting, UUID> {

    List<Painting> findAllByOwner(User owner);
    void deleteById(UUID id);

}
