# BOX GAME BRIDGE — Aplicativo do PC

Esta pasta contém o programa que roda no computador com Arch Linux.

## Arquivos

- `BoxGameBridge.java`: código-fonte Java.
- `LEIA-ME_BOX_GAME_BRIDGE.txt`: instruções de instalação e uso.

## Compilar e executar

Instale os requisitos:

```bash
sudo pacman -S jdk-openjdk wmctrl xdotool
```

Compile e execute:

```bash
javac BoxGameBridge.java
java BoxGameBridge
```

A aplicação procura a janela do Undertale, detecta a porta USB do Arduino, captura a janela, reduz a imagem para 160×128 e transmite blocos alterados. Também recebe os comandos do joystick pela serial.

A meta inicial é até 10 FPS; a taxa real depende da quantidade de blocos alterados e da velocidade serial.

**Importante:** o firmware correspondente precisa estar instalado no Arduino. Veja a pasta `../Arduino/`.
