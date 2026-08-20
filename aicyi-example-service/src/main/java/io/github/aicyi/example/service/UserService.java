package io.github.aicyi.example.service;

import io.github.aicyi.example.domain.UserQuery;
import io.github.aicyi.example.domain.entity.base.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author Mr.Min
 * @description 用户服务
 * @date 14:51
 **/
public interface UserService {

    void save(User user);

    void update(User user);

    User getById(Long id);

    User getByUsername(String username);

    List<User> list(UserQuery query);

    List<User> list(Pageable pageable, UserQuery query);

    Page<User> pagedList(Pageable pageable, UserQuery query);
}
