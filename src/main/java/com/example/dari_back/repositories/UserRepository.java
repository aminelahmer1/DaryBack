package com.example.dari_back.repositories;

import com.example.dari_back.entities.RoleType;
import com.example.dari_back.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    //    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles")
//    List<User> findAllWithRole();
    List<User> findByFirstnameAndLastname(String firstname, String lastname);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("SELECT c FROM User c WHERE c.email = ?1")
    public User findByEmail1(String email);

    public User findByResetPasswordToken(String token);
    List<User> findByRolesName(String role);
    @Query("SELECT r.name, COUNT(u) FROM User u JOIN u.roles r GROUP BY r.name")
    Map<RoleType, Long> getUserCountByRole();

    Optional<User> findById(Integer userId);

}
