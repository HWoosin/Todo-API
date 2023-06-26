package com.example.todo.todoapi.repository;

import com.example.todo.todoapi.entity.Todo;
import com.example.todo.userapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, String> {

    //특정 회원의 할 일 목록 리턴
    //select * from tbl_todo where user_id = ?
    @Query("Select t from Todo t where t.user = :user")
    List<Todo> findAllByUser(@Param("user") User user);
}
