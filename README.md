# Morse Iron-KMP

Desktop-инструмент, который отправляет текст на Arduino по USB Serial, а Arduino проигрывает его азбукой Морзе. В
железной части используются 2 светодиода: один мигает на точку ".", второй - на тире "-".

Проект включает на данный момент:

- Arduino прошивку с простым протоколом READY/OK/DONE
- KMP модуль shared с кодированием текста в сценарий Морзе и оценкой таймаутов
- macosCli - CLI версия на Kotlin/Native (macOS)
- composeApp - Desktop приложение на Compose Multiplatform (JVM) с RU/EN

## Возможности

- Отправка текста на Arduino
- Авто-транслитерация русского текста (RU -> LATIN -> Morse)
- Протокол: handshake READY, подтверждение OK, завершение DONE
- Таймаут ожидания DONE считается от длины сценария
- Desktop UI:
    - выбор порта из списка
    - connect/disconnect
    - refresh и auto refresh списка устройств
    - консоль-лог
    - переключение языка RU/EN (и UI, и системные сообщения)

## Требования

### Для Arduino

- Arduino Nano (ATmega328P)
- 2 светодиода
- 2 резистора 220-330 Ом
- Breadboard + провода Dupont
- Arduino IDE

### Для Desktop

![](/media/desktop.png)

- JDK 17+
- macOS / Windows / Linux (Desktop часть кроссплатформенная, но CLI на данный момент - только macOS)

### Для Kotlin/Native (macosCli)

- Xcode Command Line Tools

## Железо: схема подключения

![](/media/device.png)

Используем пины:

- D5 -> DOT LED (точка)
- D6 -> DASH LED (тире)
- GND -> общий

Для каждого LED:

- пин -> резистор -> анод LED
- катод LED -> GND

```
D5  -> [220] -> |>| -> GND
D6  -> [220] -> |>| -> GND
```

## Arduino прошивка

Прошивка реализует протокол:
- при старте печатает READY
- PING -> PONG
- PLAY:<my_script> -> OK -> проигрывание -> DONE
- BYE -> BYE

<my_script> - это строка, содержащая:
- "." и "-"
- пробелы между буквами
- " / " между словами

Пример:
```
PLAY:... --- ...
PLAY:.- .-.. .-.. / --- -.-.--
```

### Установка прошивки
1. Открыть Arduino IDE
2. Выбрать:
   - Tools -> Board -> Arduino Nano
   - Tools -> Port -> выбери порт устройства
   - Tools -> Processor -> ATmega328P (Old Bootloader может понадобиться для некоторых Nano-клонов)
3. Вставь скетч в новый проект или файл .ino
4. Upload

Если видишь programmer is not responding:
- закрой Serial Monitor
- проверь правильность порта
- попробуй Processor: ATmega328P (Old Bootloader) (для старых/клонов Nano)

## USB Serial: важные заметки

### macOS: /dev/cu.* vs /dev/tty.*
На macOS одно устройство часто отображается как два:
- /dev/tty.usbserial-...
- /dev/cu.usbserial-...

Для исходящего подключения почти всегда нужен /dev/cu.*.

Если выбран неправильный - возможны ошибки открытия порта или странное поведение.

### Порт занят
Если Arduino Serial Monitor открыт, порт уже занят, и приложение не сможет подключиться.

## Сборка и запуск

Все команды выполняются из корня репозитория.

### Desktop приложение (Compose Multiplatform)
Запуск:
```bash
./gradlew :composeApp:run
```

Сборка дистрибутивов:
```bash
./gradlew :composeApp:packageDistributionForCurrentOS
```

После запуска:
1. Выберите устройство из списка
2. Нажмите "Подключиться"
3. Введите текст и нажмите "Отправить"

### macOS CLI (Kotlin/Native)
Сборка:
```bash
./gradlew :macosCli:linkReleaseExecutableMacosX64
```

Где искать бинарник:
```bash
find macosCli/build -type f -name "*.kexe" -print
```

Запуск:
```bash
./path/to/macosCli.kexe /dev/cu.usbserial-XXXX "SOS"
```

Если порт не передан, CLI может использовать дефолтный.

## Локализация (RU/EN)

Desktop UI поддерживает RU/EN:
- подписи элементов интерфейса
- системные сообщения в консоли

Переключение делается кнопками RU/EN в правом верхнем углу.
