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

Ao clicar em **Conectar Arduino**, o aplicativo envia o aviso `W` ao firmware, e a TFT mostra **Conectado ao PC — Aguardando a transmissão...**. A imagem só começa a aparecer quando você clicar em **Iniciar transmissão**. A aplicação também procura a janela do Undertale, captura a janela, reduz a imagem para 160×128 e transmite blocos alterados. Os comandos do joystick são recebidos pela serial.

A meta inicial é até 10 FPS; a taxa real depende da quantidade de blocos alterados e da velocidade serial.

**Importante:** o firmware correspondente precisa estar instalado no Arduino. Veja a pasta `../Arduino/`.
