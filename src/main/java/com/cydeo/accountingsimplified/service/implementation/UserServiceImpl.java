package com.cydeo.accountingsimplified.service.implementation;

import com.cydeo.accountingsimplified.dto.RoleDto;
import com.cydeo.accountingsimplified.dto.UserDto;
import com.cydeo.accountingsimplified.entity.Role;
import com.cydeo.accountingsimplified.entity.User;
import com.cydeo.accountingsimplified.mapper.MapperUtil;
import com.cydeo.accountingsimplified.repository.UserRepository;
import com.cydeo.accountingsimplified.service.RoleService;
import com.cydeo.accountingsimplified.service.SecurityService;
import com.cydeo.accountingsimplified.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final SecurityService securityService;
    private final MapperUtil mapperUtil;

    public UserServiceImpl(UserRepository userRepository, RoleService roleService,
                           @Lazy SecurityService securityService, MapperUtil mapperUtil) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.securityService = securityService;
        this.mapperUtil = mapperUtil;
    }

    @Override
    public UserDto findUserById(Long id) {
        User user = userRepository.findUserById(id);
        return mapperUtil.convert(user, new UserDto());
    }

    @Override
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return mapperUtil.convert(user, new UserDto());
    }

    @Override
    public List<UserDto> getAllUsers() {
        if (getCurrentUserRoleDescription().equals("Root User")) {
            Role role1 = mapperUtil.convert(roleService.findByDescription("Root User"), new Role());
            Role role2 = mapperUtil.convert(roleService.findByDescription("Admin"), new Role());
            return userRepository.findAllByRoleOrRole(role1, role2)
                    .stream()
                    .map(each -> mapperUtil.convert(each, new UserDto()))
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAllByCompany(getCurrentUser().getCompany())
                    .stream()
                    .map(each -> mapperUtil.convert(each, new UserDto()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = mapperUtil.convert(userDto, new User());
        userRepository.save(user);
        return mapperUtil.convert(user, userDto);
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId).get();
        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());
        user.setUsername(userDto.getUsername());
        user.setPhone(userDto.getPhone());
        user.setPassword(userDto.getPassword());
        RoleDto roleDto = roleService.findRoleById(userDto.getRole().getId());
        user.setRole(mapperUtil.convert(roleDto, new Role()));
        userRepository.save(user);
        return mapperUtil.convert(user, userDto);
    }

    @Override
    public void delete(Long userId) {
        User user = userRepository.findUserById(userId);
        user.setIsDeleted(true);
        userRepository.save(user);
    }


    private User getCurrentUser() {
        String currentUserName = securityService.getCurrentUserUsername();
        return userRepository.findByUsername(currentUserName);
    }

    @Override
    public String getCurrentUserRoleDescription() {
        return getCurrentUser().getRole().getDescription();
    }

    @Override
    public Boolean validateIfEmailUnique(String email) {
        return userRepository.existsByUsername(email);
    }

    @Override
    public UserDto getCurrentUserDto() {
        return mapperUtil.convert(getCurrentUser(), new UserDto());
    }

}
