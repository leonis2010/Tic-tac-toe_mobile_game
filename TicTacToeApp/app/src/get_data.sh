#!/bin/bash

# ===== Настройки =====

# Директории, которые исключаются точно (указывайте от текущей директории)
EXACT_EXCLUDE_DIRS=("архив" "tests")

# Директории, исключаемые по шаблону (в названии встречается подстрока)
PATTERNS_TO_EXCLUDE=("build" "temp" "vendor" "snake" "cli" "drawable" "mipmap-anydpi" "values" "xml")

# Конкретные файлы, которые нужно исключить (сравнение по имени файла)
EXACT_FILE_EXCLUDES=("main_menu.c" "ExampleInstrumentedTest.kt" "ExampleUnitTest.kt")


# Файлы, которые нужно искать
FILE_PATTERNS=("*.h" "*.c" "*.cpp" "*.pro" "Makefile" "*.java" "*.kt" "*.xml" "*.toml" "*.kts")

# ===== Начало работы =====

echo "Запущено через: $0"

DATE=$(date +"%Y%m%d")
COUNT=1
OUTPUT_FILE="${DATE}_${COUNT}.txt"
while [ -f "$OUTPUT_FILE" ]; do
  ((COUNT++))
  OUTPUT_FILE="${DATE}_${COUNT}.txt"
done

> "$OUTPUT_FILE"

# Список обработанных файлов для логов в конце
INCLUDED_FILES=()

# Собираем исключённые пути
EXCLUDE_PATHS=()

for DIR in "${EXACT_EXCLUDE_DIRS[@]}"; do
  if [ -d "$DIR" ]; then
    EXCLUDE_PATHS+=("-path" "./$DIR" "-o")
  else
    echo "Предупреждение: директория не найдена — пропускаю: $DIR"
  fi
done

for PATTERN in "${PATTERNS_TO_EXCLUDE[@]}"; do
  while read -r DIR; do
    if [ -d "$DIR" ]; then
      EXCLUDE_PATHS+=("-path" "$DIR" "-o")
    fi
  done < <(find . -mindepth 1 -type d -name "*$PATTERN*")
done

# Удаляем последнюю -o
if [ "${#EXCLUDE_PATHS[@]}" -gt 0 ]; then
  unset 'EXCLUDE_PATHS[${#EXCLUDE_PATHS[@]}-1]'
fi

# Собираем шаблоны поиска файлов
PATTERN_EXPR=()
for PATTERN in "${FILE_PATTERNS[@]}"; do
  if [ "${#PATTERN_EXPR[@]}" -gt 0 ]; then
    PATTERN_EXPR+=("-o")
  fi
  PATTERN_EXPR+=("-name" "$PATTERN")
done

# Формируем команду find
FIND_CMD=(find .)

if [ "${#EXCLUDE_PATHS[@]}" -gt 0 ]; then
  FIND_CMD+=("(" "${EXCLUDE_PATHS[@]}" ")" "-prune" "-o")
fi

FIND_CMD+=("(" "${PATTERN_EXPR[@]}" ")" "-type" "f" "-print")

# Логируем команду (опционально)
echo "Выполняется команда: ${FIND_CMD[*]}"

# Запускаем поиск и сохраняем содержимое файлов
while IFS= read -r FILE; do
  BASENAME=$(basename "$FILE")
  SKIP_FILE=false
  for EXCL in "${EXACT_FILE_EXCLUDES[@]}"; do
    if [[ "$BASENAME" == "$EXCL" ]]; then
      SKIP_FILE=true
      break
    fi
  done

  if [ "$SKIP_FILE" = false ] && [ -f "$FILE" ]; then
    INCLUDED_FILES+=("$FILE")
    printf "%s\n\n" "$FILE" >> "$OUTPUT_FILE"
    cat "$FILE" >> "$OUTPUT_FILE"
    printf "\n\n" >> "$OUTPUT_FILE"
  fi

done < <( "${FIND_CMD[@]}" | sort )


# Добавляем лог-футер в конец файла
{
  echo "=========================="
  echo "Итоговое дерево:"
  for FILE in "${INCLUDED_FILES[@]}"; do
    echo "$FILE"
  done
} >> "$OUTPUT_FILE"

echo "Сборка завершена. Результат сохранён в: $OUTPUT_FILE"
