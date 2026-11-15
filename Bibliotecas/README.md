# Bibliotecas do BOX GAME BRIDGE

Esta pasta reúne as bibliotecas necessárias para compilar o firmware do Arduino que controla a TFT ST7735.

## ZIPs incluídos

- `Adafruit_GFX_1.12.6.zip` — núcleo gráfico Adafruit.
- `Adafruit_ST7735_1.12.0.zip` — driver da tela ST7735.
- `Adafruit_BusIO_1.17.4.zip` — dependência de comunicação usada pelo GFX.

Os três pacotes contêm os arquivos de código-fonte, metadados e avisos/licenças de seus projetos originais.

## Instalação

No Arduino IDE, use **Sketch → Include Library → Add .ZIP Library…** e selecione cada um dos três ZIPs, um de cada vez.

Também é possível extrair cada ZIP e copiar as pastas `Adafruit_GFX`, `Adafruit_ST7735` e `Adafruit_BusIO` para `~/Arduino/libraries/`. Reinicie a Arduino IDE depois.

## Outras dependências

- `SPI.h`: já vem com o pacote de placas Arduino AVR usado pelo Uno; não precisa baixar separadamente.
- Joystick: será lido com `analogRead()` e `digitalRead()`; não precisa de biblioteca.
- PC/Java: o aplicativo usa apenas bibliotecas padrão do JDK. Não há JAR externo para este projeto.
- Arch Linux: o aplicativo do PC também usa `wmctrl`, `xdotool` e `stty` (do pacote `coreutils`).

## Observação

O firmware do Arduino ainda será criado. Essas são as bibliotecas de tela previstas para o projeto atual; caso o código futuro acrescente recursos diferentes, a lista poderá ser atualizada.
