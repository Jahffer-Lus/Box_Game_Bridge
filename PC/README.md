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

Ao clicar em **Conectar Arduino**, o aplicativo envia o aviso `W` ao firmware, e a TFT mostra **Conectado ao PC — Aguardando a transmissão...**. A imagem só começa a aparecer quando você clicar em **Iniciar transmissão**. A aplicação também procura a janela do Undertale, captura a janela, reduz a imagem para 160×128 e transmite blocos alterados. Agora envia **um bloco 8×8 por vez** e aguarda a confirmação `R` do Arduino antes do próximo, evitando sobrecarregar a recepção serial enquanto a TFT é atualizada. Os comandos do joystick são recebidos pela serial.

A meta inicial é até 10 FPS; a taxa real depende da quantidade de blocos alterados e da velocidade serial.

**Importante:** o firmware correspondente precisa estar instalado no Arduino. Veja a pasta `../Arduino/`.


### Recompilar manualmente depois de atualizar o código

Na pasta principal do repositório, execute:

```bash
mkdir -p build
javac -encoding UTF-8 -d build PC/BoxGameBridge.java
jar --create --file PC/BoxGameBridge.jar --main-class BoxGameBridge -C build .
java -jar PC/BoxGameBridge.jar
```

Use o JAR recompilado: uma cópia antiga não terá o controle de fluxo com confirmação `R`.


### Controle direcional contínuo

O joystick envia eventos de pressionar (`keydown`) e soltar (`keyup`) para as setas e Enter. O aplicativo foca a janela do Undertale antes de cada evento e usa eventos de teclado XTEST, para que a tecla permaneça pressionada enquanto o joystick ficar inclinado e seja solta quando voltar ao centro.
