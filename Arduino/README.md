# BOX GAME BRIDGE — Firmware do Arduino

O código que será copiado para a Arduino IDE está no arquivo:

**[BOX_GAME_BRIDGE_ARDUINO.txt](BOX_GAME_BRIDGE_ARDUINO.txt)**

## 🔌 Nova pinagem

### TFT ST7735

| Pino da TFT | Arduino Uno |
|---|---:|
| CS | D2 |
| RESET / RST | D3 |
| A0 / DC | D4 |
| SDA / MOSI | D5 |
| SCK / SCL / CLK | D6 |
| VCC | 3,3 V ou a tensão especificada no seu módulo |
| GND | GND |

### Joystick

| Pino do joystick | Arduino Uno |
|---|---:|
| SW | D7 |
| VRX | A5 |
| VRY | A4 |
| VCC | 5 V (para módulo KY-023 comum) |
| GND | GND |

**Atenção:** como MOSI e SCK da tela foram ligados a D5 e D6, o firmware usa SPI por software. A biblioteca Adafruit_ST7735 oferece um construtor para MOSI/SCK definidos por pinos. Isso pode ser mais lento do que o SPI de hardware; os 10 FPS continuam sendo uma meta para medir.

## Orientação da tela

A TFT fica em modo paisagem (160×128), girada em **180°** usando `tft.setRotation(3)`. Para voltar à orientação anterior, use `tft.setRotation(1)`.

## Como usar

1. Abra `Arduino/BOX_GAME_BRIDGE_ARDUINO.txt` no GitHub.
2. Copie todo o conteúdo.
3. Na Arduino IDE, crie um sketch novo.
4. Cole o código e salve como `BOX_GAME_BRIDGE_ARDUINO`.
5. Confira se a placa selecionada é **Arduino Uno**.
6. Instale as bibliotecas listadas em [Bibliotecas/](../Bibliotecas/README.md).
7. Compile primeiro e só depois faça o teste na placa.

## Funções do firmware

- Lê o joystick em A5, A4 e D7.
- Envia os controles ao PC como `J:HH\n`.
- Recebe os pacotes `BX` do aplicativo Java pela USB Serial a 500000 baud.
- Atualiza a TFT ST7735 em blocos de 8×8 pixels.
- Usa somente um buffer de tile de 128 bytes, em vez de guardar a tela inteira na SRAM.
- Não usa o microSD.

**Importante:** deixe D0/RX e D1/TX livres. Não abra o Monitor Serial enquanto o aplicativo Java estiver conectado, porque ambos precisam usar a mesma porta.
