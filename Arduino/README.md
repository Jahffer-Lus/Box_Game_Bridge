# BOX GAME BRIDGE — Firmware do Arduino

O código que será copiado para a Arduino IDE está no arquivo:

**[BOX_GAME_BRIDGE_ARDUINO.txt](BOX_GAME_BRIDGE_ARDUINO.txt)**

## Como usar

1. Abra `Arduino/BOX_GAME_BRIDGE_ARDUINO.txt` no GitHub.
2. Copie todo o conteúdo.
3. Na Arduino IDE, crie um sketch novo.
4. Cole o código e salve como `BOX_GAME_BRIDGE_ARDUINO`.
5. Confira se a placa selecionada é **Arduino Uno**.
6. Instale as bibliotecas listadas em [Bibliotecas/](../Bibliotecas/README.md).
7. Compile primeiro. Só conecte e teste a TFT depois de confirmar a pinagem e a tensão do seu módulo.

## Funções do firmware

- Lê o joystick nos pinos A0, A1 e D2.
- Envia os controles ao PC como `J:HH\n`.
- Recebe os pacotes `BX` do aplicativo Java pela USB Serial a 500000 baud.
- Atualiza a TFT ST7735 em blocos de 8×8 pixels.
- Usa somente um buffer de tile de 128 bytes, em vez de guardar a tela inteira na SRAM.
- Não usa o microSD.

## Pinagem definida no código

### TFT ST7735
- CS → D10
- DC/A0 → D8
- RST/RES → D7
- SDA/MOSI → D11
- SCL/SCK → D13
- GND → GND
- VCC → tensão indicada no módulo (não assumir 5 V)
- SD_CS, se existir e estiver ligado → D9 (mantido em HIGH)

### Joystick
- VRx → A0
- VRy → A1
- SW → D2
- VCC → 5V (para módulo joystick KY-023 comum)
- GND → GND

**Importante:** o firmware foi escrito para o protocolo atual do aplicativo Java em `PC/BoxGameBridge.java`. Não abra o Monitor Serial enquanto o aplicativo Java estiver conectado, porque ambos precisam usar a mesma porta.
