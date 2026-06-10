package com.p2pdomicilios.P2pDomicilios.repositories;

import com.p2pdomicilios.P2pDomicilios.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query(value = """
        SELECT * FROM users u
        WHERE (cast(:role AS text) IS NULL OR u.role = cast(:role AS text))
          AND (cast(:estado AS text) IS NULL OR UPPER(COALESCE(u.estado, '')) = cast(:estado AS text))
          AND (cast(:enabled AS text) IS NULL OR u.enabled = cast(:enabled AS boolean))
          AND (
            cast(:q AS text) IS NULL
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', cast(:q AS text), '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', cast(:q AS text), '%'))
            OR LOWER(COALESCE(u.nombre, '')) LIKE LOWER(CONCAT('%', cast(:q AS text), '%'))
          )
        ORDER BY u.fecha_registro DESC
        """, nativeQuery = true)
    List<User> searchForAdmin(
        @Param("role") String role,
        @Param("estado") String estado,
        @Param("enabled") Boolean enabled,
        @Param("q") String q
    );
}
