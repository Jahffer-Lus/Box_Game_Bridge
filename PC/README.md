# BOX GAME BRIDGE — Aplicativo do PC

Esta pasta contém o programa que roda no computador com Arch Linux.

## Arquivos

- `BoxGameBridge.java`: código-fonte Java.
- `LEIA-ME_BOX_GAME_BRIDGE.txt`: instruções de instalação e uso.

## Aplicativo JAR

O GitHub Actions compila o código Java e, quando a compilação termina com sucesso, grava `BoxGameBridge.jar` diretamente nesta pasta `PC/`. Também disponibiliza uma cópia na seção **Artifacts** da execução. Aguarde a execução **Gerar BOX GAME BRIDGE JAR** terminar em verde antes de atualizar a página do repositório.

Para executar no Arch Linux:

```bash
sudo pacman -S jre-openjdk wmctrl xdotool
java -jar BoxGameBridge.jar
```

O arquivo `.java` continua no repositório como código-fonte para futuras atualizações; o `.jar` é o aplicativo compilado.

Ao clicar em **Conectar Arduino**, o aplicativo envia o aviso `W` ao firmware, e a TFT mostra **Conectado ao PC — Aguardando a transmissão...**. A imagem só começa a aparecer quando você clicar em **Iniciar transmissão**. A aplicação também procura a janela do Undertale, captura a janela, reduz a imagem para 160×128 e transmite blocos alterados. Os comandos do joystick são recebidos pela serial.

A meta inicial é até 10 FPS; a taxa real depende da quantidade de blocos alterados e da velocidade serial.

**Importante:** o firmware correspondente precisa estar instalado no Arduino. Veja a pasta `../Arduino/`.
