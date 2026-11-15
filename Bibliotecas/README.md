# Bibliotecas do BOX GAME BRIDGE

O pacote completo está em **[BOX_GAME_BRIDGE_LIBRARIES.zip](BOX_GAME_BRIDGE_LIBRARIES.zip)**.

Ele contém três pastas de biblioteca prontas para extrair:
- `Adafruit_GFX` — versão 1.12.6.
- `Adafruit_ST7735` — versão 1.12.0.
- `Adafruit_BusIO` — versão 1.17.4.

Extraia o pacote e copie essas três pastas para `~/Arduino/libraries/`. Depois, reinicie a Arduino IDE.

As bibliotecas nativas `SPI.h`, `analogRead()` e `digitalRead()` já fazem parte do ambiente Arduino Uno e não precisam de ZIP separado.

O aplicativo Java do PC usa somente as bibliotecas padrão do JDK. No Arch Linux, instale `jdk-openjdk`, `wmctrl`, `xdotool` e `coreutils`.

O firmware Arduino ainda será criado. As versões incluídas foram verificadas nos repositórios oficiais da Adafruit em 15/11/2026.
