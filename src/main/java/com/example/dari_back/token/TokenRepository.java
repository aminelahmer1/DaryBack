package com.example.dari_back.token;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {


    @Query(value =
            "select t from Token t inner join User u "+
                    "on t.user.id = u.id "+
                    "where u.id = :id and (t.expired = false or t.revoked = false) "
    )
    List<Token> findAllValidTokenByUser(Integer id);

    Optional<Token> findByToken(String token);
    @Query(value = "SELECT user_id FROM `token` t WHERE (t.expired = false or t.revoked = false);",
            nativeQuery = true)
    List<Integer> retrieveIdUserConecter();

}