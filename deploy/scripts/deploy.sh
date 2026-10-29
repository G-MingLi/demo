#!/usr/bin/env bash
# 2026/10/29 gongml [需求 待补充] 原生部署脚本：替换 jar、重启、健康检查、回滚
set -euo pipefail

SHA="${1:?usage: deploy.sh <git-sha>}"
APP_DIR="/opt/demo"
WEB_ROOT="/var/www/demo"
cd "$APP_DIR"

# 1. 备份当前 jar，替换为新上传的 jar
if [ -f app.jar ]; then
  cp -f app.jar app.jar.prev
fi
install -m 644 app.jar.new app.jar

# 2. 重启（sudoers 只允许这一条 systemctl）
sudo /usr/bin/systemctl restart demo

# 3. 健康检查，失败自动回滚
HEALTHY=0
for i in $(seq 1 30); do
  if RESP="$(curl -fsS http://127.0.0.1:8080/api/actuator/health 2>/dev/null)" \
     && [[ "$RESP" == *'"status":"UP"'* ]]; then
    HEALTHY=1
    break
  fi
  sleep 2
done

if [ "$HEALTHY" -ne 1 ]; then
  echo "[deploy] health check failed, rollback"
  if [ -f app.jar.prev ]; then
    install -m 644 app.jar.prev app.jar
    sudo /usr/bin/systemctl restart demo
  fi
  exit 1
fi

# 4. 前端原子切换（CI 已把 dist 传到 releases/<sha>）
RELEASE="$WEB_ROOT/releases/$SHA"
if [ -f "$RELEASE/index.html" ]; then
  ln -sfn "$RELEASE" "$WEB_ROOT/current.next"
  mv -Tf "$WEB_ROOT/current.next" "$WEB_ROOT/current"
  echo "[deploy] frontend switched to $RELEASE"
fi

# 5. 保留最近 5 个前端 release
if compgen -G "$WEB_ROOT/releases/*" > /dev/null; then
  ls -1dt "$WEB_ROOT"/releases/* | tail -n +6 | xargs -r rm -rf
fi

echo "[deploy] ok: $SHA"
