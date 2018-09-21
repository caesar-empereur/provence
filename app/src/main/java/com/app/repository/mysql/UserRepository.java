package com.app.repository.mysql;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.mysql.User;

/**
 * @Description
 * @author: yangyingyang
 * @date: 2018/9/17.
 */
public interface UserRepository extends JpaRepository<User, String> {
}
