-- 2026/10/29 gongml [需求 待补充] 初始化管理员、角色、权限
INSERT INTO sys_user (id, username, password, nickname, status)
VALUES (1, 'admin', '$2a$10$kZhBhicMGth87lxsSO0cNejy0aoUhRyQn2V4MxZPVmjnKjUfVqT7G', '管理员', 1);

INSERT INTO sys_role (id, code, name) VALUES
    (1, 'ADMIN', '管理员'),
    (2, 'USER', '普通用户');

INSERT INTO sys_permission (id, code, name) VALUES
    (1, 'user:list', '查看用户'),
    (2, 'user:add', '新增用户'),
    (3, 'dashboard:view', '查看首页');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
    (1, 1), (1, 2), (1, 3),
    (2, 3);
