# BOX GAME BRIDGE — Aplicativo do PC

Esta pasta contém o programa que roda no computador com Arch Linux.

## Arquivos

- `BoxGameBridge.java`: código-fonte Java.
- `LEIA-ME_BOX_GAME_BRIDGE.txt`: instruções de instalação e uso.

## Aplicativo JAR

O repositório gera automaticamente o aplicativo executável `BoxGameBridge.jar` sempre que o código Java é atualizado. Para baixar:

1. Abra a aba **Actions** do repositório no GitHub.
2. Entre na execução mais recente de **Gerar BOX GAME BRIDGE JAR**.
3. Na seção **Artifacts**, baixe `BoxGameBridge` e extraia o ZIP. Dentro estará `BoxGameBridge.jar`.
4. Execute no Arch Linux:

```bash
sudo pacman -S jre-openjdk wmctrl xdotool
java -jar BoxGameBridge.jar
```

O arquivo `.java` continua no repositório como código-fonte para futuras atualizações; o `.jar` é o aplicativo compilado.

Ao clicar em **Conectar Arduino**, o aplicativo envia o aviso `W` ao firmware, e a TFT mostra **Conectado ao PC — Aguardando a transmissão...**. A imagem só começa a aparecer quando você clicar em **Iniciar transmissão**. A aplicação também procura a janela do Undertale, captura a janela, reduz a imagem para 160×128 e transmite blocos alterados. Os comandos do joystick são recebidos pela serial.

A meta inicial é até 10 FPS; a taxa real depende da quantidade de blocos alterados e da velocidade serial.

**Importante:** o firmware correspondente precisa estar instalado no Arduino. Veja a pasta `../Arduino/`.
