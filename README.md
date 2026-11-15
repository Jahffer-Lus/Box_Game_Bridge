# BOX GAME BRIDGE 1.0

Projeto para usar a TFT conectada ao Arduino como uma pequena tela externa para um jogo executado no computador. O PC faz o processamento e o armazenamento; o Arduino recebe a imagem e lê o joystick.

## Estrutura do repositório

```text
Box_Game_Bridge/
├── Arduino/
│   └── README.md
├── PC/
│   ├── BoxGameBridge.java
│   ├── LEIA-ME_BOX_GAME_BRIDGE.txt
│   └── README.md
└── README.md
```

## Pastas

- **PC/** — aplicativo Java para Arch Linux. Procura a janela do Undertale e a porta serial do Arduino, captura a imagem, reduz para 160×128 e envia somente blocos alterados. Também transforma os comandos do joystick em teclas.
- **Arduino/** — local do firmware para ler o joystick, receber os blocos de imagem e atualizar a TFT. O firmware ainda será desenvolvido.

## Objetivo inicial

Meta de até 10 FPS. Isso é uma meta, não uma garantia: a velocidade real depende da quantidade de blocos que mudam, da captura da janela e da transmissão serial.

Consulte primeiro o [README do PC](PC/README.md) e o [README do Arduino](Arduino/README.md).
