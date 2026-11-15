# BOX GAME BRIDGE 1.0

Projeto para usar a TFT conectada ao Arduino como uma pequena tela externa para um jogo executado no computador. O PC faz o processamento e o armazenamento; o Arduino recebe a imagem e lê o joystick.

## Estrutura do repositório

- **[Arduino/](Arduino/)** — local do firmware que será desenvolvido.
- **[PC/](PC/)** — aplicativo Java para Arch Linux.
- **[Bibliotecas/](Bibliotecas/)** — pacote ZIP das bibliotecas necessárias para a TFT.
- **[GUIA_HARDWARE_E_LIGACOES.md](GUIA_HARDWARE_E_LIGACOES.md)** — componentes necessários, ligação fio por fio, bibliotecas e instalação.

## Bibliotecas

Baixe o pacote completo em [Bibliotecas/BOX_GAME_BRIDGE_LIBRARIES.zip](Bibliotecas/BOX_GAME_BRIDGE_LIBRARIES.zip). Ele contém Adafruit GFX 1.12.6, Adafruit ST7735 1.12.0 e Adafruit BusIO 1.17.4.

## Objetivo inicial

Meta de até 10 FPS. Isso é uma meta, não uma garantia: a velocidade real depende da quantidade de blocos que mudam, da captura da janela e da transmissão serial.

Consulte primeiro o [README do PC](PC/README.md), o [README do Arduino](Arduino/README.md) e o guia de hardware.
