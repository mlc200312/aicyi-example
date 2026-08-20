package io.github.aicyi.example.service.impl;

import io.github.aicyi.commons.core.id.IdGenerator;
import io.github.aicyi.example.dao.mapper.base.UserMapper;
import io.github.aicyi.example.domain.UserQuery;
import io.github.aicyi.example.domain.entity.base.User;
import io.github.aicyi.example.domain.entity.base.UserExample;
import io.github.aicyi.example.service.UserService;
import io.github.aicyi.midware.db.commons.BaseEntityUtils;
import io.github.aicyi.midware.db.commons.PageUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Mr.Min
 * @description 用户服务实现
 * @date 15:06
 **/
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final IdGenerator idGenerator;
    private final UserMapper userMapper;

    @Override
    public void save(User user) {
        BaseEntityUtils.setDefaultValue(user, idGenerator);
        userMapper.insertSelective(user);
    }

    @Override
    public void update(User user) {
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public User getById(Long id) {
        long nextId = idGenerator.nextId();
        System.out.println(nextId);
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    public User getByUsername(String username) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        criteria.andUsernameEqualTo(username);
        List<User> userList = userMapper.selectByExample(userExample);
        if (CollectionUtils.isEmpty(userList)) {
            return null;
        }
        return CollectionUtils.extractSingleton(userList);
    }

    @Override
    public List<User> list(UserQuery query) {
        UserExample userExample = new UserExample();
        UserExample.Criteria criteria = userExample.createCriteria();
        if (Objects.isNull(query)) {
            return Collections.emptyList();
        }
        if (StringUtils.isNotBlank(query.getMobileEq())) {
            criteria.andMobileEqualTo(query.getMobileEq());
        }
        if (StringUtils.isNotBlank(query.getIdCardEq())) {
            criteria.andIdCardEqualTo(query.getIdCardEq());
        }
        if (Objects.nonNull(query.getBirthdayStart())) {
            criteria.andBirthdayGreaterThanOrEqualTo(query.getBirthdayStart());
        }
        if (Objects.nonNull(query.getBirthdayEnd())) {
            criteria.andBirthdayLessThanOrEqualTo(query.getBirthdayEnd());
        }
        if (CollectionUtils.isNotEmpty(query.getIdListIn())) {
            criteria.andIdIn(query.getIdListIn());
        }
        return userMapper.selectByExample(userExample);
    }

    @Override
    public List<User> list(Pageable pageable, UserQuery query) {
        Page<User> page = PageUtils.getPage(pageable, () -> list(query), false);
        return page.getContent();
    }

    @Override
    public Page<User> pagedList(Pageable pageable, UserQuery query) {
        return PageUtils.getPage(query, () -> list(query));
    }
}
