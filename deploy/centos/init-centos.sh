#!/usr/bin/env bash
# 2026/10/29 gongml [需求 待补充] CentOS 8.2 初始化脚本（修源/装依赖/建目录）
set -euo pipefail

if [ "$(id -u)" -ne 0 ]; then
  echo "请用 root 执行：bash init-centos.sh"
  exit 1
fi

echo "== 1. 修复 CentOS 8.2 vault 源 =="
mkdir -p /etc/yum.repos.d/backup-$(date +%F)
cp -a /etc/yum.repos.d/CentOS-Linux-*.repo /etc/yum.repos.d/backup-$(date +%F)/ 2>/dev/null || true

sed -i 's|^mirrorlist=|#mirrorlist=|g' /etc/yum.repos.d/CentOS-Linux-*.repo
sed -i 's|^#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-Linux-*.repo
sed -i 's|\$releasever|8.2.2004|g' /etc/yum.repos.d/CentOS-Linux-*.repo
dnf clean all
dnf makecache

echo "== 2. 安装基础组件 =="
dnf install -y epel-release
dnf install -y curl tar vim git sudo rsync firewalld fail2ban policycoreutils-python-utils
dnf install -y nginx certbot python3-certbot-nginx

dnf module enable -y mysql:8.0
dnf install -y @mysql:8.0/server
dnf install -y redis

echo "== 3. 创建 deploy 用户与目录 =="
if ! id deploy >/dev/null 2>&1; then
  useradd -m -s /bin/bash deploy
fi
usermod -aG systemd-journal deploy || true

if [ -f /opt/demo/sudoers/demo-deploy ]; then
  install -m 440 /opt/demo/sudoers/demo-deploy /etc/sudoers.d/demo-deploy
  visudo -c
fi

mkdir -p /opt/demo /opt/demo/backups /var/www/demo/releases /var/www/certbot /etc/nginx/snippets
chown -R deploy:deploy /opt/demo /var/www/demo
chmod 700 /opt/demo

echo "== 4. 安装 Java 17（Temurin） =="
if [ ! -x /opt/java/bin/java ]; then
  mkdir -p /opt/java
  tmp_jdk=/tmp/jdk17.tar.gz
  curl -fL --retry 3 -o "$tmp_jdk" \
    "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse"
  tar -xzf "$tmp_jdk" -C /opt/java --strip-components=1
  rm -f "$tmp_jdk"
fi
/opt/java/bin/java -version

echo "== 5. 时区、Swap、防火墙、SELinux =="
timedatectl set-timezone Asia/Shanghai

if ! swapon --show | grep -q .; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' >> /etc/fstab
  echo 'vm.swappiness=10' > /etc/sysctl.d/99-swap.conf
  sysctl --system
fi

systemctl enable --now firewalld
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https
firewall-cmd --reload

if command -v getenforce >/dev/null 2>&1 && [ "$(getenforce)" = "Enforcing" ]; then
  setsebool -P httpd_can_network_connect 1
  semanage fcontext -a -t httpd_sys_content_t "/var/www/demo(/.*)?" || true
  restorecon -Rv /var/www/demo
fi

echo "== 6. 启动服务 =="
systemctl enable --now mysqld redis nginx fail2ban

echo
echo "基础环境完成。下一步："
echo "1. mysql_secure_installation 设置 root 密码"
echo "2. 创建 demo 库和应用账号（README 有 SQL）"
echo "3. 配置 /opt/demo/.env、demo.service、Nginx"
