# Demo 项目

Vue 3 + Spring Boot 3.5 + Java 17 的基础项目架构，当前部署目标是 CentOS 8.2 云服务器（原生部署，不使用 Docker）。

## 目录

```text
backend/                 Spring Boot 3.5.16 + Java 17
frontend/                Vue 3 + TypeScript + Vite + Element Plus
deploy/                  CentOS 初始化、Nginx、systemd、部署/备份脚本
.github/workflows/       GitHub Actions 构建与发布
docs/plans/              设计与变更记录
```

## 已实现

- 统一响应 `ApiResponse`、错误码、全局异常处理
- 请求 `X-Request-Id` 与日志 traceId
- 登录 / 刷新 / 登出 / 当前用户
- JWT Access Token + HttpOnly Refresh Cookie
- 用户 / 角色 / 权限表，`@PreAuthorize` 方法级鉴权
- Flyway 初始化表结构与管理员账号
- 用户列表分页接口
- 前端登录页、主布局、用户列表、路由守卫、按钮权限指令

## 本地启动（先看这里）

### 1. 准备 MySQL

本地需要 MySQL 8.0+。创建库和账号：

```sql
CREATE DATABASE demo CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'demo'@'127.0.0.1' IDENTIFIED BY '你的本地密码';
GRANT ALL PRIVILEGES ON demo.* TO 'demo'@'127.0.0.1';
FLUSH PRIVILEGES;
```

### 2. 后端

```bash
cd backend
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export DB_USERNAME=demo
export DB_PASSWORD=你的本地密码
mvn spring-boot:run
```

- 健康检查：`http://localhost:8080/api/actuator/health`
- Swagger：`http://localhost:8080/api/swagger-ui.html`
- 本地 profile 的刷新令牌存在内存里，不需要 Redis；生产 profile 用 Redis。

### 3. 前端

```bash
cd frontend
pnpm install
pnpm dev
```

打开 `http://localhost:5173`，初始账号：

```text
admin / Admin@123456
```

> 这是初始化密码，登录后第一件事是改成自己的密码；当前骨架没有“修改密码”页面，可以在数据库里替换 BCrypt 哈希，或后续加接口。

## 服务器部署（CentOS 8.2）

> 你的服务器已经出现过 **4 万多次 SSH 失败登录**。在部署业务前，先按“安全第一步”换成密钥登录，再关掉密码登录。不要直接把 root 密码长期暴露在公网。

### 1. 上传部署文件

在本地项目根目录执行（把 `root@43.110.27.119` 换成你的实际登录方式）：

```bash
ssh root@43.110.27.119 "mkdir -p /opt/demo-src"
scp -r deploy root@43.110.27.119:/opt/demo-src
ssh root@43.110.27.119
```

登录服务器后：

```bash
mkdir -p /opt/demo
cp -a /opt/demo-src/deploy/. /opt/demo/
bash /opt/demo/centos/init-centos.sh
```

`init-centos.sh` 做这些事：

1. 把已下线的 CentOS 8.2 镜像源切到 `vault.centos.org`，否则 `dnf` 装不了包。
2. 安装 Nginx、MySQL 8.0、Redis 5、Certbot、fail2ban、Java 依赖。
3. 创建 `deploy` 用户和 `/opt/demo`、`/var/www/demo` 目录。
4. 下载 Temurin Java 17 到 `/opt/java`。
5. 配置 2 GiB Swap、firewalld 放行 80/443、SELinux 允许 Nginx 反代。

### 2. 初始化数据库

```bash
mysql_secure_installation
mysql -uroot -p
```

```sql
CREATE DATABASE demo CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'demo'@'127.0.0.1' IDENTIFIED BY '替换为随机密码';
GRANT ALL PRIVILEGES ON demo.* TO 'demo'@'127.0.0.1';
FLUSH PRIVILEGES;
```

Redis 设置密码（`/etc/redis.conf`）：

```conf
bind 127.0.0.1
requirepass 替换为随机密码
maxmemory 128mb
maxmemory-policy allkeys-lru
appendonly yes
```

```bash
systemctl restart redis
```

### 3. 配置后端环境变量

```bash
cd /opt/demo
cp .env.example .env
chmod 600 .env
vim .env
```

生成随机值：

```bash
openssl rand -base64 32   # MySQL 应用密码
openssl rand -base64 32   # Redis 密码
openssl rand -base64 48   # JWT
```

`.env` 里的 `SPRING_DATASOURCE_URL` 含 `&`，在 shell 里要加单引号；systemd `EnvironmentFile` 会去掉引号。

### 4. 安装 systemd 服务

```bash
cp /opt/demo/centos/demo.service /etc/systemd/system/demo.service
systemctl daemon-reload
systemctl enable demo
```

先不要启动，等第一次 jar 上传后再 `systemctl start demo`。

### 5. 配置 Nginx 与证书

首次签发证书时，只启用 HTTP 配置：

```bash
cp /opt/demo/nginx/demo-http.conf /etc/nginx/conf.d/demo.conf
cp /opt/demo/nginx/security-headers.conf /etc/nginx/snippets/security-headers.conf
nginx -t && systemctl reload nginx

certbot certonly --webroot -w /var/www/certbot \
  -d gmingli.top -d www.gmingli.top \
  --email 932062373@qq.com --agree-tos --no-eff-email
```

签发成功后换生产配置：

```bash
cp /opt/demo/nginx/demo.conf /etc/nginx/conf.d/demo.conf
nginx -t && systemctl reload nginx
```

### 6. 首次上传 jar 与前端

在本地构建：

```bash
cd backend
JAVA_HOME=$(/usr/libexec/java_home -v 17) mvn clean package

cd ../frontend
pnpm build
```

上传：

```bash
scp backend/target/*.jar deploy@43.110.27.119:/opt/demo/app.jar.new
ssh deploy@43.110.27.119 "mkdir -p /var/www/demo/releases/manual"
rsync -az --delete frontend/dist/ deploy@43.110.27.119:/var/www/demo/releases/manual/
ssh deploy@43.110.27.119 "bash /opt/demo/deploy.sh manual"
```

> `deploy` 用户需要能读 `/opt/demo`、写 `/opt/demo`，并通过受限 sudo 执行 `systemctl restart demo`。把 `deploy/sudoers/demo-deploy` 放到 `/etc/sudoers.d/demo-deploy`，执行 `chmod 440` 和 `visudo -c`。

### 7. GitHub Actions 自动部署

仓库 → Settings → Secrets and variables → Actions，添加：

| 名称 | 值 |
| --- | --- |
| `SSH_HOST` | `43.110.27.119` |
| `SSH_USER` | `deploy` |
| `SSH_PRIVATE_KEY` | 部署私钥全文 |
| `SSH_KNOWN_HOSTS` | `ssh-keyscan -H 43.110.27.119` 的输出 |

推送到 `main` 后，Actions 会：

1. Java 17 打包 `app.jar`；
2. Node 24 打包前端 `dist`；
3. `scp` jar、`rsync` dist；
4. 执行 `/opt/demo/deploy.sh <sha>`；
5. 健康检查失败自动回滚上一版 jar。

## 安全第一步（建议今天就做）

当前是 root + 密码登录，且已经被大量扫描。不要先关密码，否则容易把自己锁在外面。顺序：

```bash
# 1. 本地生成专用密钥
ssh-keygen -t ed25519 -C "gmingli-deploy" -f ~/.ssh/gmingli_deploy -N ""

# 2. 把公钥装到服务器
ssh-copy-id -i ~/.ssh/gmingli_deploy.pub root@43.110.27.119

# 3. 测试密钥登录（不要关掉当前会话）
ssh -i ~/.ssh/gmingli_deploy root@43.110.27.119

# 4. 创建 deploy 用户并装同一把公钥
id deploy >/dev/null 2>&1 || adduser deploy
mkdir -p /home/deploy/.ssh
cp /root/.ssh/authorized_keys /home/deploy/.ssh/authorized_keys
chown -R deploy:deploy /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
chmod 600 /home/deploy/.ssh/authorized_keys

# 5. 测试 deploy 登录，确认无误后再改 sshd
ssh -i ~/.ssh/gmingli_deploy deploy@43.110.27.119
```

确认两把钥匙都能登录后，再改 `/etc/ssh/sshd_config`：

```conf
PermitRootLogin no
PasswordAuthentication no
MaxAuthTries 3
```

```bash
systemctl restart sshd
systemctl enable --now fail2ban
```

> 改完配置后**不要关闭当前会话**，另开一个终端测试新登录；失败就用云控制台 VNC 救回来。

## 为什么这样设计

- **不用 Docker**：CentOS 8.2 已 EOL，Docker CE 没有官方支持；测试期用系统包 + systemd 更稳。
- **jar + systemd**：服务器不需要 Maven/Node，构建都在 GitHub Actions；2 GiB 内存也不会被构建过程打爆。
- **Nginx + 127.0.0.1:8080**：MySQL/Redis 不暴露公网，只有 Nginx 对外。
- **Flyway**：表结构随代码版本走，换机器/重建库不用手工执行 SQL。
- **JWT + Refresh Cookie**：Access Token 短、放内存；Refresh Token HttpOnly，降低 XSS 窃取风险。
- **@PreAuthorize**：权限判断在后端，前端 `v-perm` 只是隐藏按钮，不能代替后端校验。

## 下一步学习建议

1. 先跑通“登录 → 用户列表 → 退出”。
2. 看 `AuthService` / `JwtService` / `SecurityConfig` 三者的调用关系。
3. 加一个“新增用户”接口，练习 DTO 校验、BCrypt、事务。
4. 加“修改密码”接口，替换初始密码。
5. 再把 `deploy.yml` 跑通，观察一次失败回滚。

> 代码由你确认后自行 `git add/commit/push`；我不执行 Git 提交。
