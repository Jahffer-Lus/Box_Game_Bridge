# BOX GAME BRIDGE — Firmware do Arduino

Esta pasta será usada para o código que será carregado no Arduino Uno.

## Responsabilidades do Arduino

- Ler o joystick.
- Enviar os estados dos controles ao PC no formato `J:HH\n`.
- Receber os pacotes de imagem `BX` enviados pelo aplicativo Java.
- Atualizar a tela TFT ST7735 de 128×160.

## Estado atual

O firmware ainda está em desenvolvimento e **não foi adicionado**. Ele precisa corresponder exatamente ao protocolo descrito em `../PC/BoxGameBridge.java`.

Não carregue um firmware genérico nesta etapa. Vamos criar e testar o código do Arduino antes de conectar o sistema completo.
