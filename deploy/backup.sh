#!/usr/bin/env bash
# Нічний бекап бази Smartum (запускає cron на сервері, див. етап 4 плану деплою):
#   1) pg_dump з контейнера db -> ~/backups/school-РРРР-ММ-ДД_ГГХХ.dump
#   2) локальні копії, старші за LOCAL_DAYS, видалити (щоб не заповнити диск)
#   3) копія поза сервером: rclone -> Google Drive, зашифровано (remote REMOTE)
# Відновлення: pg_restore (інструкція — у довіднику "Docker для Smartum").
set -euo pipefail   # будь-яка помилка зупиняє скрипт, а не йде далі мовчки
umask 077           # нові файли бекапу — лише для власника (персональні дані)

PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"   # папка з compose.yaml (на сервері ~/smart)
BACKUP_DIR="$HOME/backups"
LOCAL_DAYS=14
REMOTE_DAYS=60
REMOTE="smartum-backup:"   # ім'я з rclone config (шифрований шар над Google Drive)

mkdir -p "$BACKUP_DIR"
FILE="$BACKUP_DIR/school-$(date +%F_%H%M).dump"

echo "$(date '+%F %T') старт бекапу -> $FILE"
cd "$PROJECT_DIR"
# -T: без терміналу (cron його не має); -Fc: стиснений формат для pg_restore.
# Пишемо спершу в .part: якщо pg_dump впаде, напівфайл не виглядатиме як справжній бекап
docker compose exec -T db pg_dump -U smart -d school -Fc > "$FILE.part"
mv "$FILE.part" "$FILE"
echo "$(date '+%F %T') готово, розмір $(du -h "$FILE" | cut -f1)"

find "$BACKUP_DIR" -name 'school-*.dump' -mtime +"$LOCAL_DAYS" -delete

# копія поза сервером — лише коли rclone вже налаштований (етап 4.4)
if command -v rclone >/dev/null && rclone listremotes | grep -qx "$REMOTE"; then
  rclone copy "$FILE" "$REMOTE"
  rclone delete "$REMOTE" --min-age "${REMOTE_DAYS}d"
  echo "$(date '+%F %T') скопійовано в $REMOTE"
else
  echo "$(date '+%F %T') УВАГА: rclone не налаштований — копії поза сервером НЕМАЄ"
fi
